package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.Protocolo;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "protocolos")
public class ProtocoloAvaliacaoDocument {

    @Id
    private String id;

    private String nome;

    @Indexed
    private String categoria;

    private Boolean padrao;

    private Protocolo protocolo;
    private String strategyKey;
    private String tabelaClassificacaoId;

    private String descricao;
    private String comoRealizar;
    private String calculadora;
    private String referenciaBibliografica;
    private String unidadeMedida;
    private Integer tempoMinimoSegundos;
    private Integer tempoMaximoSegundos;
    private String equipamentoNecessario;
    private String criteriosExclusao;
    private String observacoes;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ProtocoloAvaliacaoDocument() {
    }

    public ProtocoloAvaliacaoDocument(String id, String nome, String categoria, Boolean padrao,
                                      Protocolo protocolo, String strategyKey,
                                      String tabelaClassificacaoId) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.padrao = padrao;
        this.protocolo = protocolo;
        this.strategyKey = strategyKey;
        this.tabelaClassificacaoId = tabelaClassificacaoId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getPadrao() { return padrao; }
    public void setPadrao(Boolean padrao) { this.padrao = padrao; }

    public Protocolo getProtocolo() { return protocolo; }
    public void setProtocolo(Protocolo protocolo) { this.protocolo = protocolo; }

    public String getStrategyKey() { return strategyKey; }
    public void setStrategyKey(String strategyKey) { this.strategyKey = strategyKey; }

    public String getTabelaClassificacaoId() { return tabelaClassificacaoId; }
    public void setTabelaClassificacaoId(String tabelaClassificacaoId) { this.tabelaClassificacaoId = tabelaClassificacaoId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getComoRealizar() { return comoRealizar; }
    public void setComoRealizar(String comoRealizar) { this.comoRealizar = comoRealizar; }

    public String getCalculadora() { return calculadora; }
    public void setCalculadora(String calculadora) { this.calculadora = calculadora; }

    public String getReferenciaBibliografica() { return referenciaBibliografica; }
    public void setReferenciaBibliografica(String referenciaBibliografica) { this.referenciaBibliografica = referenciaBibliografica; }

    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }

    public Integer getTempoMinimoSegundos() { return tempoMinimoSegundos; }
    public void setTempoMinimoSegundos(Integer tempoMinimoSegundos) { this.tempoMinimoSegundos = tempoMinimoSegundos; }

    public Integer getTempoMaximoSegundos() { return tempoMaximoSegundos; }
    public void setTempoMaximoSegundos(Integer tempoMaximoSegundos) { this.tempoMaximoSegundos = tempoMaximoSegundos; }

    public String getEquipamentoNecessario() { return equipamentoNecessario; }
    public void setEquipamentoNecessario(String equipamentoNecessario) { this.equipamentoNecessario = equipamentoNecessario; }

    public String getCriteriosExclusao() { return criteriosExclusao; }
    public void setCriteriosExclusao(String criteriosExclusao) { this.criteriosExclusao = criteriosExclusao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
