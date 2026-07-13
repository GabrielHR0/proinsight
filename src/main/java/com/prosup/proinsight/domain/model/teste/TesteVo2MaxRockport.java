package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.DadosAvaliacao;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import com.prosup.proinsight.domain.enums.Sexo;

public class TesteVo2MaxRockport extends TesteVo2Max {

    private Double tempoMinutos;
    private Integer frequenciaCardiaca;
    private Double pesoKg;

    public TesteVo2MaxRockport() {}

    public TesteVo2MaxRockport(Double tempoMinutos, Integer frequenciaCardiaca, Double pesoKg) {
        super(ProtocoloVo2Max.ROCKPORT, null);
        this.tempoMinutos = tempoMinutos;
        this.frequenciaCardiaca = frequenciaCardiaca;
        this.pesoKg = pesoKg;
    }

    @Override
    public String gerarCodigo() {
        return "ROCKPORT_" + System.currentTimeMillis();
    }

    @Override
    public Double calcularVo2Max(DadosAvaliacao dados) {
        return calcularVo2maxRockport(dados);
    }

    @Override
    public String getValorClassificacao() {
        return valorClassificacao != null ? String.valueOf(valorClassificacao) : null;
    }

    @Override
    public String getValorClassificacao(DadosAvaliacao dados) {
        Double vo2max = calcularVo2Max(dados);
        if (vo2max != null) {
            this.valorClassificacao = vo2max;
        }
        return vo2max != null ? String.valueOf(vo2max) : null;
    }

    private Double calcularVo2maxRockport(DadosAvaliacao dados) {
        if (tempoMinutos == null || frequenciaCardiaca == null || pesoKg == null) return null;
        if (dados == null || !dados.temIdade() || !dados.temSexo()) return null;

        double pesoLbs = pesoKg * 2.20462;
        int idade = dados.getIdade();
        int sexoFactor = dados.getSexo() == Sexo.MASCULINO ? 1 : 0;

        return 132.853
            - (0.0769 * pesoLbs)
            - (0.3877 * idade)
            + (6.315 * sexoFactor)
            - (3.2649 * tempoMinutos)
            - (0.1565 * frequenciaCardiaca);
    }

    public Double getTempoMinutos() {
        return tempoMinutos;
    }

    public void setTempoMinutos(Double tempoMinutos) {
        this.tempoMinutos = tempoMinutos;
    }

    public Integer getFrequenciaCardiaca() {
        return frequenciaCardiaca;
    }

    public void setFrequenciaCardiaca(Integer frequenciaCardiaca) {
        this.frequenciaCardiaca = frequenciaCardiaca;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }
}
