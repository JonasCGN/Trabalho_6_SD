package com.exemplo;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class GeradorMensagens {
    private static final String EXCHANGE_NAME = "image_analysis_exchange";
    private static final String FACES_ROUTING_KEY = "face";
    private static final String TEAMS_ROUTING_KEY = "team";
    private static final int MESSAGES_PER_SECOND = 6;
    private static final long DELAY_MS = 1000 / MESSAGES_PER_SECOND;

    private Connection connection;
    private Channel channel;
    private ObjectMapper objectMapper;
    private List<Path> faceImages;
    private List<Path> teamImages;
    private Random random;
    private static final String FACES_DIR = "/app/imagens-teste/faces";
    private static final String TEAMS_DIR = "/app/imagens-teste/times";

    public GeradorMensagens() {
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
        this.faceImages = new ArrayList<>();
        this.teamImages = new ArrayList<>();
    }

    public void conectarRabbitMQ() throws IOException, TimeoutException {
        String rabbitmqHost = System.getenv("RABBITMQ_HOST");
        if (rabbitmqHost == null) rabbitmqHost = "localhost";
        
        String rabbitmqUser = System.getenv("RABBITMQ_USER");
        if (rabbitmqUser == null) rabbitmqUser = "guest";
        
        String rabbitmqPass = System.getenv("RABBITMQ_PASS");
        if (rabbitmqPass == null) rabbitmqPass = "guest";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setUsername(rabbitmqUser);
        factory.setPassword(rabbitmqPass);
        factory.setPort(5672);

        // Configurar reconexão automática
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);

        System.out.println("Conectando ao RabbitMQ em: " + rabbitmqHost);
        
        int maxTentativas = 10;
        for (int i = 0; i < maxTentativas; i++) {
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                
                // Declarar o exchange topic
                channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);
                
                // Declarar filas
                channel.queueDeclare("face_queue", true, false, false, null);
                channel.queueDeclare("team_queue", true, false, false, null);
                
                // Bind das filas ao exchange
                channel.queueBind("face_queue", EXCHANGE_NAME, FACES_ROUTING_KEY);
                channel.queueBind("team_queue", EXCHANGE_NAME, TEAMS_ROUTING_KEY);
                
                System.out.println("Conectado ao RabbitMQ com sucesso!");
                return;
            } catch (Exception e) {
                System.out.println("Tentativa " + (i + 1) + " falhou: " + e.getMessage());
                if (i < maxTentativas - 1) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new IOException("Não foi possível conectar ao RabbitMQ após " + maxTentativas + " tentativas");
    }

    public void carregarImagens() {
        try {
            // Carregar imagens de rostos
            Path facesPath = Paths.get(FACES_DIR);
            if (Files.exists(facesPath) && Files.isDirectory(facesPath)) {
                Files.list(facesPath)
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                    })
                    .forEach(faceImages::add);
            }
            
            // Carregar imagens de times
            Path teamsPath = Paths.get(TEAMS_DIR);
            if (Files.exists(teamsPath) && Files.isDirectory(teamsPath)) {
                Files.list(teamsPath)
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                    })
                    .forEach(teamImages::add);
            }
            
            // Se não encontrou imagens reais, usar dados simulados como fallback
            if (faceImages.isEmpty()) {
                System.out.println("⚠️  Nenhuma imagem de rosto encontrada em " + FACES_DIR + ", usando simulação");
                adicionarImagensSimuladas();
            } else {
                System.out.println("✅ Carregadas " + faceImages.size() + " imagens reais de rostos");
            }
            
            if (teamImages.isEmpty()) {
                System.out.println("⚠️  Nenhuma imagem de time encontrada em " + TEAMS_DIR + ", usando simulação");
                adicionarImagensSimuladas();
            } else {
                System.out.println("✅ Carregadas " + teamImages.size() + " imagens reais de times");
            }
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar imagens: " + e.getMessage());
            System.out.println("Usando dados simulados como fallback");
            adicionarImagensSimuladas();
        }
    }

    public void iniciarGeracao() throws IOException {
        System.out.println("Iniciando geração de mensagens...");
        System.out.println("Taxa: " + MESSAGES_PER_SECOND + " mensagens por segundo");
        
        long contador = 0;
        
        while (true) {
            try {
                // Alterna entre tipos de mensagem (60% rostos, 40% times)
                boolean enviarRosto = random.nextDouble() < 0.6;
                
                MensagemImagem mensagem = criarMensagem(enviarRosto);
                String routingKey = enviarRosto ? FACES_ROUTING_KEY : TEAMS_ROUTING_KEY;
                
                String jsonMensagem = objectMapper.writeValueAsString(mensagem);
                
                channel.basicPublish(
                    EXCHANGE_NAME,
                    routingKey,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    jsonMensagem.getBytes("UTF-8")
                );
                
                contador++;
                
                if (contador % 10 == 0) {
                    double tamanhoKB = mensagem.getDadosImagem().length / 1024.0;
                    System.out.println("Mensagens enviadas: " + contador + 
                                     " (Última: " + mensagem.getTipo() + " - " + mensagem.getNomeArquivo() + 
                                     String.format(" - %.1fKB)", tamanhoKB));
                }
                
                Thread.sleep(DELAY_MS);
                
            } catch (InterruptedException e) {
                System.out.println("Geração interrompida");
                break;
            } catch (Exception e) {
                System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }

    private MensagemImagem criarMensagem(boolean isRosto) {
        MensagemImagem mensagem = new MensagemImagem();
        mensagem.setId(UUID.randomUUID().toString());
        mensagem.setTimestamp(System.currentTimeMillis());
        
        Path imagemPath;
        if (isRosto) {
            mensagem.setTipo("face");
            imagemPath = faceImages.get(random.nextInt(faceImages.size()));
        } else {
            mensagem.setTipo("team");
            imagemPath = teamImages.get(random.nextInt(teamImages.size()));
        }
        
        mensagem.setNomeArquivo(imagemPath.getFileName().toString());
        
        // Carregar dados reais da imagem
        try {
            mensagem.setDadosImagem(Files.readAllBytes(imagemPath));
        } catch (IOException e) {
            System.err.println("Erro ao ler imagem " + imagemPath + ": " + e.getMessage());
            // Fallback para dados simulados
            mensagem.setDadosImagem(gerarDadosSimulados(mensagem.getNomeArquivo()));
        }
        
        return mensagem;
    }

    private void adicionarImagensSimuladas() {
        // Adicionar paths simulados se não houver imagens reais
        if (faceImages.isEmpty()) {
            String[] faces = {
                "pessoa_feliz_1.jpg", "pessoa_triste_1.jpg", "rosto_sorrindo.jpg",
                "happy_face.jpg", "sad_face.jpg", "alegre_jovem.jpg",
                "rosto_chorando.jpg", "melancolia.jpg"
            };
            for (String face : faces) {
                faceImages.add(Paths.get("/simulado/" + face));
            }
        }
        
        if (teamImages.isEmpty()) {
            String[] teams = {
                "logo_flamengo_1.jpg", "logo_palmeiras_1.jpg", "logo_corinthians_1.jpg",
                "logo_santos_1.jpg", "logo_gremio_1.jpg", "logo_internacional_1.jpg",
                "logo_vasco_1.jpg", "logo_botafogo_1.jpg"
            };
            for (String team : teams) {
                teamImages.add(Paths.get("/simulado/" + team));
            }
        }
    }
    
    private byte[] gerarDadosSimulados(String nomeArquivo) {
        // Simula dados de uma imagem pequena baseada no nome
        byte[] dados = new byte[1024 + random.nextInt(2048)]; // 1-3KB
        
        // Usar hash do nome para dados determinísticos
        Random rnd = new Random(nomeArquivo.hashCode());
        rnd.nextBytes(dados);
        
        return dados;
    }

    public void fechar() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao fechar conexões: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GeradorMensagens gerador = new GeradorMensagens();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nFinalizando gerador...");
            gerador.fechar();
        }));
        
        try {
            gerador.conectarRabbitMQ();
            gerador.carregarImagens();
            gerador.iniciarGeracao();
        } catch (Exception e) {
            System.err.println("Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}