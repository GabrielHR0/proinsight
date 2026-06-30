package com.prosup.proinsight.adapter.out.persistence;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("medicaoVo2Max")
public class MedicaoVo2MaxDocument extends MedicaoDocument {

    private ProtocoloVo2Max protocolo;
    private Double distanciaMetros;
    private Integer tempoSegundos;
    private Integer frequenciaCardiacaBpm;
    private Double vo2MaxCalculado;
    private String classificacaoVo2;

    public MedicaoVo2MaxDocument() {
        super(MedicaoTipo.VO2_MAX);
    }

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }

    public Double getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Double distanciaMetros) {
        this.distanciaMetros = distanciaMetros;
    }

    public Integer getTempoSegundos() {
        return tempoSegundos;
    }

    public void setTempoSegundos(Integer tempoSegundos) {
        this.tempoSegundos = tempoSegundos;
    }

    public Integer getFrequenciaCardiacaBpm() {
        return frequenciaCardiacaBpm;
    }

    public void setFrequenciaCardiacaBpm(Integer frequenciaCardiacaBpm) {
        this.frequenciaCardiacaBpm = frequenciaCardiacaBpm;
    }

    public Double getVo2MaxCalculado() {
        return vo2MaxCalculado;
    }

    public void setVo2MaxCalculado(Double vo2MaxCalculado) {
        this.vo2MaxCalculado = vo2MaxCalculado;
    }

    public String getClassificacaoVo2() {
        return classificacaoVo2;
    }

    public void setClassificacaoVo2(String classificacaoVo2) {
        this.classificacaoVo2 = classificacaoVo2;
    }
}
