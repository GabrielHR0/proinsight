package com.prosup.proinsight.domain.model.teste;

import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;

public class TesteVo2MaxRockport extends TesteVo2Max {

    private double tempoMinutos;
    private int frequenciaCardiacaFinal;

    public TesteVo2MaxRockport(){
        super();
        this.protocolo = ProtocoloVo2Max.ROCKPORT;
    }

    public TesteVo2MaxRockport(double tempoMinutos, int frequenciaCardiacaFinal) {
        this();
        this.tempoMinutos = tempoMinutos;
        this.frequenciaCardiacaFinal = frequenciaCardiacaFinal;
    }

    @Override
    public String gerarCodigo(){
        return "ROCKPORT_" + System.currentTimeMillis();
    }

    public double getTempoMinutos() {
        return tempoMinutos;
    }

    public void setTempoMinutos(double tempoMinutos) {
        this.tempoMinutos = tempoMinutos;
    }

    public int getFrequenciaCardiacaFinal() {
        return frequenciaCardiacaFinal;
    }

    public void setFrequenciaCardiacaFinal(int frequenciaCardiacaFinal) {
        this.frequenciaCardiacaFinal = frequenciaCardiacaFinal;
    }
}
