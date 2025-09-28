package com.exemplo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MensagemImagem {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("tipo")
    private String tipo; // "face" ou "team"
    
    @JsonProperty("nomeArquivo")
    private String nomeArquivo;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("dadosImagem")
    private byte[] dadosImagem;

    // Construtores
    public MensagemImagem() {}

    public MensagemImagem(String id, String tipo, String nomeArquivo, long timestamp, byte[] dadosImagem) {
        this.id = id;
        this.tipo = tipo;
        this.nomeArquivo = nomeArquivo;
        this.timestamp = timestamp;
        this.dadosImagem = dadosImagem;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getDadosImagem() {
        return dadosImagem;
    }

    public void setDadosImagem(byte[] dadosImagem) {
        this.dadosImagem = dadosImagem;
    }

    @Override
    public String toString() {
        return "MensagemImagem{" +
                "id='" + id + '\'' +
                ", tipo='" + tipo + '\'' +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", timestamp=" + timestamp +
                ", tamanhoImagem=" + (dadosImagem != null ? dadosImagem.length : 0) +
                '}';
    }
}