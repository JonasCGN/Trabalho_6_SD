package com.exemplo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

class Main {
    private static final String EXCHANGE_NAME = "image_analysis_exchange";
    private static final String FACES_DIR = "/app/database_face/";
    private static final String FOOTBALL_DIR = "/app/database_futebol/";

    private Connection connection;
    private Channel channel;
    private ObjectMapper objectMapper;
    private List<Path> faceImages;
    private List<Path> footballImages;
    private Random random;
    private AtomicLong messagesSent;

    public Main() {
        this.objectMapper = new ObjectMapper();
        this.faceImages = new ArrayList<>();
        this.footballImages = new ArrayList<>();
        this.random = new Random();
        this.messagesSent = new AtomicLong(0);
    }

    public void connectRabbitMQ() throws IOException, TimeoutException {
        String host = System.getenv().getOrDefault("RABBITMQ_HOST", "localhost");
        String user = System.getenv().getOrDefault("RABBITMQ_USER", "guest");
        String pass = System.getenv().getOrDefault("RABBITMQ_PASS", "guest");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(pass);
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, true);

        // Queues separadas
        channel.queueDeclare("face_queue", true, false, false, null);
        channel.queueBind("face_queue", EXCHANGE_NAME, "face");

        channel.queueDeclare("football_queue", true, false, false, null);
        channel.queueBind("football_queue", EXCHANGE_NAME, "football");

        System.out.println("Conectado ao RabbitMQ em: " + host);
    }

    public void loadImages() throws IOException {
        loadDirImagesRecursive(FACES_DIR, faceImages, ".jpg");
        loadDirImagesRecursive(FOOTBALL_DIR, footballImages, ".png"); // ou ".jpg" se seu dataset usar jpg

        if (faceImages.isEmpty() && footballImages.isEmpty()) {
            throw new IOException("Nenhuma imagem encontrada nos diretórios.");
        }

        System.out.println("✅ Carregadas " + faceImages.size() + " imagens de rostos");
        System.out.println("✅ Carregadas " + footballImages.size() + " imagens de futebol");
    }

    private void loadDirImagesRecursive(String dir, List<Path> list, String ext) throws IOException {
        Path path = Paths.get(dir);
        if (Files.exists(path) && Files.isDirectory(path)) {
            Files.walk(path)  // percorre recursivamente todas as subpastas
                 .filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().toLowerCase().endsWith(ext))
                 .forEach(list::add);
        }
    }

    public void startSending() {
        System.out.println("Iniciando envio de mensagens...");
        while (true) {
            try {
                MensagemImagem msg;
                if (random.nextBoolean() && !faceImages.isEmpty()) {
                    msg = createMessage(faceImages, "face");
                } else if (!footballImages.isEmpty()) {
                    msg = createMessage(footballImages, "football");
                } else {
                    continue; // nenhuma imagem disponível
                }

                String json = objectMapper.writeValueAsString(msg);
                channel.basicPublish(EXCHANGE_NAME, msg.getType(),
                                     MessageProperties.PERSISTENT_TEXT_PLAIN, json.getBytes("UTF-8"));

                long count = messagesSent.incrementAndGet();
                if (count % 10 == 0) {
                    double tamanhoKB = msg.getImageData().length / 1024.0;
                    System.out.printf("Mensagens enviadas: %d - %s (%.1fKB)\n", count, msg.getFileName(), tamanhoKB);
                }

            } catch (Exception e) {
                System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private MensagemImagem createMessage(List<Path> images, String type) {
        MensagemImagem msg = new MensagemImagem();
        msg.setId(UUID.randomUUID().toString());
        msg.setType(type);
        msg.setTimestamp(System.currentTimeMillis());

        Path imgPath = images.get(random.nextInt(images.size()));
        msg.setFileName(imgPath.getFileName().toString());

        try {
            msg.setImageData(Files.readAllBytes(imgPath));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler imagem: " + imgPath, e);
        }

        return msg;
    }

    public void close() {
        try { if (channel != null && channel.isOpen()) channel.close(); } catch (Exception ignored) {}
        try { if (connection != null && connection.isOpen()) connection.close(); } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        Main sender = new Main();
        Runtime.getRuntime().addShutdownHook(new Thread(sender::close));

        try {
            sender.connectRabbitMQ();
            sender.loadImages();
            sender.startSending();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MensagemImagem {
        @JsonProperty("id") private String id;
        @JsonProperty("tipo") private String type;
        @JsonProperty("nomeArquivo") private String fileName;
        @JsonProperty("timestamp") private long timestamp;
        @JsonProperty("dadosImagem") private byte[] imageData;

        public MensagemImagem() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public byte[] getImageData() { return imageData; }
        public void setImageData(byte[] imageData) { this.imageData = imageData; }

        @Override
        public String toString() {
            return "MensagemImagem{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", timestamp=" + timestamp +
                    ", tamanhoImagem=" + (imageData != null ? imageData.length : 0) +
                    '}';
        }
    }
}
