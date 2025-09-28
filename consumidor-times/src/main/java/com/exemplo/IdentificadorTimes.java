package com.exemplo;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class IdentificadorTimes {
    private Random random;
    private Map<String, String> timesConhecidos;
    private Map<String, double[]> caracteristicasTimes;

    public IdentificadorTimes() {
        this.random = new Random();
        this.timesConhecidos = new HashMap<>();
        this.caracteristicasTimes = new HashMap<>();
        
        // Base de conhecimento de times brasileiros
        timesConhecidos.put("flamengo", "Flamengo - RJ");
        timesConhecidos.put("corinthians", "Corinthians - SP");  
        timesConhecidos.put("palmeiras", "Palmeiras - SP");
        timesConhecidos.put("santos", "Santos - SP");
        timesConhecidos.put("saopaulo", "São Paulo - SP");
        timesConhecidos.put("vasco", "Vasco da Gama - RJ");
        timesConhecidos.put("botafogo", "Botafogo - RJ");
        timesConhecidos.put("fluminense", "Fluminense - RJ");
        timesConhecidos.put("gremio", "Grêmio - RS");
        timesConhecidos.put("internacional", "Internacional - RS");
        
        // Inicializar características base dos times (simulação)
        inicializarCaracteristicasTimes();
    }

    private void inicializarCaracteristicasTimes() {
        // Simular características visuais dos brasões (cores dominantes, formas, etc.)
        caracteristicasTimes.put("flamengo", new double[]{0.9, 0.1, 0.1, 0.8, 0.2}); // Vermelho dominante
        caracteristicasTimes.put("corinthians", new double[]{0.9, 0.9, 0.9, 0.1, 0.1}); // Preto e branco
        caracteristicasTimes.put("palmeiras", new double[]{0.1, 0.8, 0.1, 0.9, 0.3}); // Verde dominante
        caracteristicasTimes.put("santos", new double[]{0.9, 0.9, 0.9, 0.1, 0.1}); // Preto e branco
        caracteristicasTimes.put("saopaulo", new double[]{0.9, 0.1, 0.1, 0.9, 0.9}); // Vermelho, preto e branco
        caracteristicasTimes.put("vasco", new double[]{0.1, 0.1, 0.1, 0.9, 0.9}); // Preto e branco
        caracteristicasTimes.put("botafogo", new double[]{0.1, 0.1, 0.1, 0.9, 0.9}); // Preto e branco
        caracteristicasTimes.put("fluminense", new double[]{0.8, 0.1, 0.1, 0.1, 0.8}); // Grená, verde e branco
        caracteristicasTimes.put("gremio", new double[]{0.1, 0.1, 0.9, 0.1, 0.1}); // Azul dominante
        caracteristicasTimes.put("internacional", new double[]{0.9, 0.1, 0.1, 0.9, 0.1}); // Vermelho dominante
    }

    public ResultadoIdentificacao identificarTime(MensagemImagem mensagem) {
        try {
            // Simular tempo de processamento de IA (3-5 segundos - mais lento que sentimento)
            Thread.sleep(3000 + random.nextInt(2000));
            
            String nomeArquivo = mensagem.getNomeArquivo().toLowerCase();
            
            // Analisar imagem real
            BufferedImage imagem = carregarImagem(mensagem.getDadosImagem());
            CaracteristicasLogo caracteristicas = analisarLogo(imagem);
            
            // Identificar o time baseado no nome e análise visual
            ResultadoIdentificacao resultado = classificarTime(nomeArquivo, caracteristicas);
            
            return resultado;
            
        } catch (Exception e) {
            return new ResultadoIdentificacao(
                mensagem.getId(),
                "ERRO",
                "Erro no processamento: " + e.getMessage(),
                0.0,
                "Falha na análise da imagem"
            );
        }
    }

    private BufferedImage carregarImagem(byte[] dadosImagem) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(dadosImagem);
        return ImageIO.read(bis);
    }

    private CaracteristicasLogo analisarLogo(BufferedImage imagem) {
        if (imagem == null) {
            return new CaracteristicasLogo(0.33, 0.33, 0.33, 0.5, 0.5, 0.5);
        }

        int width = imagem.getWidth();
        int height = imagem.getHeight();
        int totalPixels = width * height;

        // Contadores para análise de cores
        int pixelsVermelhos = 0, pixelsVerdes = 0, pixelsAzuis = 0;
        int pixelsPretos = 0, pixelsBrancos = 0;
        double somaComplexidade = 0;

        // Analisar distribuição de cores
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagem.getRGB(x, y);
                Color cor = new Color(rgb);
                
                int r = cor.getRed();
                int g = cor.getGreen();
                int b = cor.getBlue();
                
                // Classificar cores dominantes
                if (r > g + 50 && r > b + 50 && r > 100) {
                    pixelsVermelhos++;
                } else if (g > r + 30 && g > b + 30 && g > 80) {
                    pixelsVerdes++;
                } else if (b > r + 30 && b > g + 30 && b > 100) {
                    pixelsAzuis++;
                } else if (r < 50 && g < 50 && b < 50) {
                    pixelsPretos++;
                } else if (r > 200 && g > 200 && b > 200) {
                    pixelsBrancos++;
                }
                
                // Calcular complexidade local (variação de cor)
                if (x > 0 && y > 0) {
                    int rgbAnterior = imagem.getRGB(x-1, y);
                    Color corAnterior = new Color(rgbAnterior);
                    double diff = Math.abs(r - corAnterior.getRed()) + 
                                 Math.abs(g - corAnterior.getGreen()) + 
                                 Math.abs(b - corAnterior.getBlue());
                    somaComplexidade += diff / (3 * 255.0);
                }
            }
        }

        double percentualVermelho = (double) pixelsVermelhos / totalPixels;
        double percentualVerde = (double) pixelsVerdes / totalPixels;
        double percentualAzul = (double) pixelsAzuis / totalPixels;
        double percentualPreto = (double) pixelsPretos / totalPixels;
        double percentualBranco = (double) pixelsBrancos / totalPixels;
        double complexidade = somaComplexidade / totalPixels;

        return new CaracteristicasLogo(percentualVermelho, percentualVerde, percentualAzul, 
                                     percentualPreto, percentualBranco, complexidade);
    }

    private ResultadoIdentificacao classificarTime(String nomeArquivo, CaracteristicasLogo caracteristicas) {
        // ===== ANÁLISE PURAMENTE VISUAL - SEM USAR NOME DO ARQUIVO =====
        // O nome do arquivo é IGNORADO completamente - apenas análise de cores!
        
        String melhorTime = classificarPorCores(caracteristicas);
        String nomeCompleto = "Time não identificado";
        double melhorScore = 0.0;
        
        if (!melhorTime.equals("DESCONHECIDO")) {
            nomeCompleto = timesConhecidos.get(melhorTime);
            melhorScore = calcularScoreCor(melhorTime, caracteristicas);
        }
        
        // Se a classificação por cor é confiável o suficiente
        if (melhorScore > 0.3) {
            double confianca = melhorScore * 0.8; // Confiança baseada em análise visual
            
            return new ResultadoIdentificacao(
                UUID.randomUUID().toString(),
                melhorTime.toUpperCase(),
                nomeCompleto,
                confianca,
                String.format("Classificação visual por cores - Score: %.2f (R:%.1f%%, V:%.1f%%, A:%.1f%%)", 
                            melhorScore, 
                            caracteristicas.percentualVermelho * 100,
                            caracteristicas.percentualVerde * 100,
                            caracteristicas.percentualAzul * 100)
            );
        }
        
        // Time não identificado
        return new ResultadoIdentificacao(
            UUID.randomUUID().toString(),
            "DESCONHECIDO",
            "Time não identificado - características visuais insuficientes",
            melhorScore,
            String.format("Análise visual sem correspondência (melhor score=%.2f)", melhorScore)
        );
    }

    private String classificarPorCores(CaracteristicasLogo caracteristicas) {
        // ===== CLASSIFICAÇÃO RIGOROSA POR ANÁLISE VISUAL =====
        
        // Times com dominância vermelha clara (threshold mais alto)
        if (caracteristicas.percentualVermelho > 0.5) {
            if (caracteristicas.percentualPreto > 0.25) {
                return "flamengo"; // Vermelho + preto = Flamengo
            }
            if (caracteristicas.percentualBranco > 0.2) {
                return "internacional"; // Vermelho + branco = Internacional  
            }
            return "internacional"; // Vermelho puro = Internacional
        }
        
        // Times com dominância verde (threshold específico)
        if (caracteristicas.percentualVerde > 0.4) {
            return "palmeiras"; // Verde dominante = Palmeiras
        }
        
        // Times com dominância azul (threshold alto)
        if (caracteristicas.percentualAzul > 0.45) {
            return "gremio"; // Azul dominante = Grêmio
        }
        
        // Times preto e branco (análise mais refinada)
        double pretoBranco = caracteristicas.percentualPreto + caracteristicas.percentualBranco;
        if (pretoBranco > 0.6) {
            if (caracteristicas.complexidade > 0.4) {
                return "corinthians"; // Preto/branco complexo = Corinthians
            }
            return "santos"; // Preto/branco simples = Santos
        }
        
        // Análise secundária para São Paulo (tricolor)
        if (caracteristicas.percentualVermelho > 0.3 && 
            caracteristicas.percentualPreto > 0.2 && 
            caracteristicas.percentualBranco > 0.2) {
            return "saopaulo"; // Tricolor = São Paulo
        }
        
        return "DESCONHECIDO"; // Não atende critérios visuais
    }

    private double calcularScoreCor(String time, CaracteristicasLogo caracteristicas) {
        // Cálculo de score baseado na correspondência visual específica
        switch (time) {
            case "flamengo":
                return caracteristicas.percentualVermelho * 0.7 + 
                       caracteristicas.percentualPreto * 0.3;
                       
            case "palmeiras":
                return caracteristicas.percentualVerde * 0.9 + 
                       (1.0 - caracteristicas.percentualVermelho - caracteristicas.percentualAzul) * 0.1;
                       
            case "corinthians":
                return (caracteristicas.percentualPreto + caracteristicas.percentualBranco) * 0.5 + 
                       caracteristicas.complexidade * 0.4;
                       
            case "santos":
                return (caracteristicas.percentualPreto + caracteristicas.percentualBranco) * 0.6 + 
                       (1.0 - caracteristicas.complexidade) * 0.2;
                       
            case "gremio":
                return caracteristicas.percentualAzul * 0.8 + 
                       caracteristicas.percentualBranco * 0.2;
                       
            case "internacional":
                return caracteristicas.percentualVermelho * 0.8 + 
                       caracteristicas.percentualBranco * 0.2;
                       
            case "saopaulo":
                return (caracteristicas.percentualVermelho + caracteristicas.percentualPreto + caracteristicas.percentualBranco) * 0.4;
                
            default:
                return 0.0;
        }
    }

    // Classe para armazenar características do logo
    private static class CaracteristicasLogo {
        public final double percentualVermelho;
        public final double percentualVerde;
        public final double percentualAzul;
        public final double percentualPreto;
        public final double percentualBranco;
        public final double complexidade;

        public CaracteristicasLogo(double percentualVermelho, double percentualVerde, double percentualAzul,
                                 double percentualPreto, double percentualBranco, double complexidade) {
            this.percentualVermelho = percentualVermelho;
            this.percentualVerde = percentualVerde;
            this.percentualAzul = percentualAzul;
            this.percentualPreto = percentualPreto;
            this.percentualBranco = percentualBranco;
            this.complexidade = complexidade;
        }
    }

    public static class ResultadoIdentificacao {
        private String idMensagem;
        private String codigoTime;
        private String nomeCompleto;
        private double confianca;
        private String detalhes;

        public ResultadoIdentificacao(String idMensagem, String codigoTime, String nomeCompleto, 
                                    double confianca, String detalhes) {
            this.idMensagem = idMensagem;
            this.codigoTime = codigoTime;
            this.nomeCompleto = nomeCompleto;
            this.confianca = confianca;
            this.detalhes = detalhes;
        }

        // Getters
        public String getIdMensagem() { return idMensagem; }
        public String getCodigoTime() { return codigoTime; }
        public String getNomeCompleto() { return nomeCompleto; }
        public double getConfianca() { return confianca; }
        public String getDetalhes() { return detalhes; }

        @Override
        public String toString() {
            return String.format("Resultado{id=%s, time=%s, nome='%s', conf=%.2f, detalhes='%s'}", 
                idMensagem, codigoTime, nomeCompleto, confianca, detalhes);
        }
    }
}