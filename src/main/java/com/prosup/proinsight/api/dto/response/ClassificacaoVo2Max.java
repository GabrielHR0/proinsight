package com.prosup.proinsight.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassificacaoVo2Max {
    
    @JsonProperty("nome")
    private String nome;
    
    @JsonProperty("nome_legivel")
    private String nomeLegivel;
    
    @JsonProperty("descricao")
    private String descricao;
    
    @JsonProperty("valor_vo2max")
    private Double valorVo2Max;

    @JsonProperty("mets_calculado")
    private Double metsCalculado;
    
    public ClassificacaoVo2Max() {}
    

    public ClassificacaoVo2Max(String nome, String descricao, Double valorVo2Max) {
        this.nome = nome;
        this.descricao = descricao;
        this.valorVo2Max = valorVo2Max;
    }

    public ClassificacaoVo2Max(String nome, String descricao, Double valorVo2Max, Double metsCalculado) {
        this.nome = nome;
        this.descricao = descricao;
        this.valorVo2Max = valorVo2Max;
        this.metsCalculado = metsCalculado;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getNomeLegivel() {
        return nomeLegivel;
    }
    
    public void setNomeLegivel(String nomeLegivel) {
        this.nomeLegivel = nomeLegivel;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Double getValorVo2Max() {
        return valorVo2Max;
    }
    
    public void setValorVo2Max(Double valorVo2Max) {
        this.valorVo2Max = valorVo2Max;
    }

    public Double getMetsCalculado() {
        return metsCalculado;
    }

    public void setMetsCalculado(Double metsCalculado) {
        this.metsCalculado = metsCalculado;
    }
}
