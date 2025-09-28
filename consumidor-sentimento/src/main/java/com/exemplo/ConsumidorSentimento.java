package com.exemplo;

import com.rabbitmq.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class ConsumidorSentimento {
    private static final String QUEUE_NAME = "face_queue";
    private Connection connection;
    private Channel channel;
    private ObjectMapper objectMapper;
    private AnalisadorSentimento analisador;
    private AtomicLong mensagensProcessadas;

    public ConsumidorSentimento() {
        this.objectMapper = new ObjectMapper();
        this.analisador = new AnalisadorSentimento();
        this.mensagensProcessadas = new AtomicLong(0);
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
                
                // Garantir que a fila existe
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                
                // Configurar QoS para processar uma mensagem por vez
                channel.basicQos(1);
                
                System.out.println("Conectado ao RabbitMQ - Consumidor de Análise de Sentimento");
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

    public void iniciarConsumo() throws IOException {
        System.out.println("Iniciando consumo de mensagens da fila: " + QUEUE_NAME);
        System.out.println("Análise de sentimento com biblioteca Smile ML");
        
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String mensagemJson = new String(delivery.getBody(), "UTF-8");
                MensagemImagem mensagem = objectMapper.readValue(mensagemJson, MensagemImagem.class);
                
                long inicio = System.currentTimeMillis();
                
                System.out.println("\n📁 IMAGEM: " + mensagem.getNomeArquivo());
                System.out.println("🔍 Analisando características visuais da imagem...");
                
                // Processar com IA VISUAL
                AnalisadorSentimento.ResultadoSentimento resultado = analisador.analisarImagem(mensagem);
                
                long tempoProcessamento = System.currentTimeMillis() - inicio;
                long count = mensagensProcessadas.incrementAndGet();
                
                String emoji = resultado.getSentimento().equals("FELIZ") ? "😊" : 
                              resultado.getSentimento().equals("TRISTE") ? "😢" : "❓";
                
                System.out.println(String.format("%s RESULTADO: %s", emoji, resultado.getSentimento()));
                System.out.println(String.format("⏱️  TEMPO: %.1fs | 📊 CONFIANÇA: %.1f%%", 
                    tempoProcessamento / 1000.0, resultado.getConfianca() * 100));
                System.out.println("📈 ANÁLISE: " + resultado.getDetalhes());
                System.out.println("=" + "=".repeat(80));
                
                // Confirmar processamento
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                
                // Log estatísticas a cada 10 mensagens
                if (count % 10 == 0) {
                    System.out.println(String.format("=== Estatísticas: %d mensagens processadas ===", count));
                }
                
            } catch (Exception e) {
                System.err.println("Erro ao processar mensagem: " + e.getMessage());
                e.printStackTrace();
                
                // Rejeitar mensagem e colocar de volta na fila para retry
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };

        CancelCallback cancelCallback = consumerTag -> {
            System.out.println("Consumo cancelado: " + consumerTag);
        };

        // Iniciar consumo com confirmação manual
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, cancelCallback);
        
        System.out.println("Aguardando mensagens. Para sair pressione CTRL+C");
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
        ConsumidorSentimento consumidor = new ConsumidorSentimento();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nFinalizando consumidor de sentimento...");
            consumidor.fechar();
        }));
        
        try {
            consumidor.conectarRabbitMQ();
            consumidor.iniciarConsumo();
            
            // Manter o programa rodando
            while (true) {
                Thread.sleep(1000);
            }
            
        } catch (Exception e) {
            System.err.println("Erro fatal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}