package com.exemplo;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class AnalisadorSentimento {
    
    private Random random;
    private Map<String, Double> palavrasChaveEmocoes;
    
    public AnalisadorSentimento() {
        this.random = new Random();
        this.palavrasChaveEmocoes = new HashMap<>();
        
        // Mapeamento de palavras-chave para probabilidade de felicidade
        palavrasChaveEmocoes.put("feliz", 0.9);
        palavrasChaveEmocoes.put("alegre", 0.85);
        palavrasChaveEmocoes.put("sorrindo", 0.95);
        palavrasChaveEmocoes.put("radiante", 0.9);
        palavrasChaveEmocoes.put("contente", 0.8);
        palavrasChaveEmocoes.put("happy", 0.9);
        palavrasChaveEmocoes.put("smile", 0.85);
        palavrasChaveEmocoes.put("joy", 0.88);
        
        palavrasChaveEmocoes.put("triste", 0.1);
        palavrasChaveEmocoes.put("chorando", 0.05);
        palavrasChaveEmocoes.put("melancolia", 0.15);
        palavrasChaveEmocoes.put("deprimido", 0.08);
        palavrasChaveEmocoes.put("sad", 0.1);
        palavrasChaveEmocoes.put("cry", 0.05);
        palavrasChaveEmocoes.put("upset", 0.12);
        
        palavrasChaveEmocoes.put("neutra", 0.5);
        palavrasChaveEmocoes.put("neutral", 0.5);
        palavrasChaveEmocoes.put("normal", 0.5);
        
        System.out.println("Analisador de Sentimento inicializado com análise de imagem real usando características visuais");
    }

    public ResultadoSentimento analisarImagem(MensagemImagem mensagem) {
        try {
            // Simular tempo de processamento de IA (2-4 segundos)
            Thread.sleep(2000 + random.nextInt(2000));
            
            String nomeArquivo = mensagem.getNomeArquivo().toLowerCase();
            
            // Analisar imagem real usando características visuais
            BufferedImage imagem = carregarImagem(mensagem.getDadosImagem());
            CaracteristicasVisuais caracteristicas = analisarCaracteristicasVisuais(imagem);
            
            // Determinar probabilidade de felicidade baseada no nome e análise visual
            double probabilidadeFeliz = calcularProbabilidadeFeliz(nomeArquivo, caracteristicas);
            
            // Adicionar ruído para simular incerteza do modelo
            double ruido = random.nextGaussian() * 0.05;
            probabilidadeFeliz = Math.max(0.0, Math.min(1.0, probabilidadeFeliz + ruido));
            
            String sentimento = probabilidadeFeliz > 0.5 ? "FELIZ" : "TRISTE";
            double confianca = Math.abs(probabilidadeFeliz - 0.5) * 2; // 0-1
            
            return new ResultadoSentimento(
                mensagem.getId(),
                sentimento,
                probabilidadeFeliz,
                confianca,
                String.format("Análise Visual Real - Brilho: %.2f, Saturação: %.2f, Contraste: %.2f, Cores quentes: %.2f%%", 
                    caracteristicas.brilhoMedio, caracteristicas.saturacaoMedia, 
                    caracteristicas.contraste, caracteristicas.percentualCoresQuentes * 100)
            );
            
        } catch (Exception e) {
            return new ResultadoSentimento(
                mensagem.getId(),
                "ERRO",
                0.0,
                0.0,
                "Erro no processamento: " + e.getMessage()
            );
        }
    }

    private BufferedImage carregarImagem(byte[] dadosImagem) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(dadosImagem);
        return ImageIO.read(bis);
    }

    private CaracteristicasVisuais analisarCaracteristicasVisuais(BufferedImage imagem) {
        if (imagem == null) {
            return new CaracteristicasVisuais(0.5, 0.5, 0.5, 0.5);
        }

        int width = imagem.getWidth();
        int height = imagem.getHeight();
        int totalPixels = width * height;

        double somaBrilho = 0, somaSaturacao = 0;
        int pixelsCoresQuentes = 0;
        int[] histogramaBrilho = new int[256];

        // Analisar cada pixel da imagem
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagem.getRGB(x, y);
                Color cor = new Color(rgb);
                
                int r = cor.getRed();
                int g = cor.getGreen();
                int b = cor.getBlue();
                
                // Calcular brilho (luminância)
                double brilho = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                somaBrilho += brilho;
                histogramaBrilho[(int)(brilho * 255)]++;
                
                // Calcular saturação
                double max = Math.max(r, Math.max(g, b)) / 255.0;
                double min = Math.min(r, Math.min(g, b)) / 255.0;
                double saturacao = max == 0 ? 0 : (max - min) / max;
                somaSaturacao += saturacao;
                
                // Detectar cores quentes (amarelo, laranja, vermelho)
                if (r > g && r > b && r > 100) { // Tons vermelhos
                    pixelsCoresQuentes++;
                } else if (r > 150 && g > 150 && b < 100) { // Tons amarelos
                    pixelsCoresQuentes++;
                }
            }
        }

        double brilhoMedio = somaBrilho / totalPixels;
        double saturacaoMedia = somaSaturacao / totalPixels;
        double percentualCoresQuentes = (double) pixelsCoresQuentes / totalPixels;
        
        // Calcular contraste usando histograma
        double contraste = calcularContraste(histogramaBrilho, totalPixels);

        return new CaracteristicasVisuais(brilhoMedio, saturacaoMedia, contraste, percentualCoresQuentes);
    }

    private double calcularContraste(int[] histograma, int totalPixels) {
        // Calcular desvio padrão do histograma de brilho
        double media = 0;
        for (int i = 0; i < histograma.length; i++) {
            media += i * histograma[i];
        }
        media /= totalPixels;

        double variancia = 0;
        for (int i = 0; i < histograma.length; i++) {
            double diff = i - media;
            variancia += diff * diff * histograma[i];
        }
        variancia /= totalPixels;

        return Math.sqrt(variancia) / 255.0; // Normalizar para 0-1
    }

    private double calcularProbabilidadeFeliz(String nomeArquivo, CaracteristicasVisuais caracteristicas) {
        // ===== ANÁLISE PRIMARIAMENTE VISUAL =====
        // 80% peso para análise visual, 20% para nome do arquivo
        
        // Análise baseada em características visuais (PESO ALTO)
        double scoreBrilho = caracteristicas.brilhoMedio > 0.6 ? 0.3 : -0.2;
        double scoreSaturacao = caracteristicas.saturacaoMedia > 0.5 ? 0.25 : -0.15;
        double scoreContraste = caracteristicas.contraste > 0.3 ? 0.15 : -0.1;
        double scoreCoresQuentes = caracteristicas.percentualCoresQuentes > 0.3 ? 0.3 : -0.2;

        double scoreVisual = scoreBrilho + scoreSaturacao + scoreContraste + scoreCoresQuentes;
        
        // Verificar palavras-chave no nome do arquivo (PESO BAIXO)
        double scoreNome = 0.0;
        for (Map.Entry<String, Double> entry : palavrasChaveEmocoes.entrySet()) {
            if (nomeArquivo.toLowerCase().contains(entry.getKey())) {
                scoreNome = (entry.getValue() - 0.5) * 0.3; // Reduzir influência do nome
                break;
            }
        }
        
        // Combinar scores: 80% visual + 20% nome + baseline neutro
        double resultado = 0.5 + (scoreVisual * 0.8) + (scoreNome * 0.2);
        
        return Math.max(0.0, Math.min(1.0, resultado));
    }

    // Classe para armazenar características visuais
    private static class CaracteristicasVisuais {
        public final double brilhoMedio;
        public final double saturacaoMedia;
        public final double contraste;
        public final double percentualCoresQuentes;

        public CaracteristicasVisuais(double brilhoMedio, double saturacaoMedia, 
                                    double contraste, double percentualCoresQuentes) {
            this.brilhoMedio = brilhoMedio;
            this.saturacaoMedia = saturacaoMedia;
            this.contraste = contraste;
            this.percentualCoresQuentes = percentualCoresQuentes;
        }
    }

    public static class ResultadoSentimento {
        private String idMensagem;
        private String sentimento;
        private double probabilidadeFeliz;
        private double confianca;
        private String detalhes;

        public ResultadoSentimento(String idMensagem, String sentimento, double probabilidadeFeliz, 
                                 double confianca, String detalhes) {
            this.idMensagem = idMensagem;
            this.sentimento = sentimento;
            this.probabilidadeFeliz = probabilidadeFeliz;
            this.confianca = confianca;
            this.detalhes = detalhes;
        }

        // Getters
        public String getIdMensagem() { return idMensagem; }
        public String getSentimento() { return sentimento; }
        public double getProbabilidadeFeliz() { return probabilidadeFeliz; }
        public double getConfianca() { return confianca; }
        public String getDetalhes() { return detalhes; }

        @Override
        public String toString() {
            return String.format("Resultado{id=%s, sentimento=%s, prob=%.2f, conf=%.2f, detalhes='%s'}", 
                idMensagem, sentimento, probabilidadeFeliz, confianca, detalhes);
        }
    }
}