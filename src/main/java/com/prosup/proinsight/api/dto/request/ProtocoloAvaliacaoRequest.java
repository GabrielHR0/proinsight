package com.prosup.proinsight.api.dto.request;

import com.prosup.proinsight.domain.enums.Protocolo;
import jakarta.validation.constraints.NotBlank;

public class ProtocoloAvaliacaoRequest {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    private Protocolo protocolo;

    @NotBlank(message = "strategyKey é obrigatório")
    private String strategyKey;

    @NotBlank(message = "tabelaClassificacaoId é obrigatório")
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

    public ProtocoloAvaliacaoRequest() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

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
}
