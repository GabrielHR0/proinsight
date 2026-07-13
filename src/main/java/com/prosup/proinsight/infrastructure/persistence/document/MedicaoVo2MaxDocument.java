package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.ProtocoloVo2Max;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

@TypeAlias("medicaoVo2Max")
public class MedicaoVo2MaxDocument extends MedicaoDocument {

    private ProtocoloVo2Max protocolo;
    private Integer distanciaMetros;
    private Integer tempoSegundos;
    private Integer frequenciaCardiacaBpm;
    private Double pesoKg;
    private Integer vo2MaxCalculado;
    private String classificacaoVo2;
    private List<Object> testesAdicionais;

    private AquecimentoDocument aquecimento;

    public MedicaoVo2MaxDocument() {
        super(MedicaoTipo.VO2_MAX);
    }

    public ProtocoloVo2Max getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(ProtocoloVo2Max protocolo) {
        this.protocolo = protocolo;
    }

    public Integer getDistanciaMetros() {
        return distanciaMetros;
    }

    public void setDistanciaMetros(Integer distanciaMetros) {
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

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public List<Object> getTestesAdicionais() {
        return testesAdicionais;
    }

    public void setTestesAdicionais(List<Object> testesAdicionais) {
        this.testesAdicionais = testesAdicionais;
    }

    public Integer getVo2MaxCalculado() {
        return vo2MaxCalculado;
    }

    public void setVo2MaxCalculado(Integer vo2MaxCalculado) {
        this.vo2MaxCalculado = vo2MaxCalculado;
    }

    public String getClassificacaoVo2() {
        return classificacaoVo2;
    }

    public void setClassificacaoVo2(String classificacaoVo2) {
        this.classificacaoVo2 = classificacaoVo2;
    }

    public AquecimentoDocument getAquecimento() {
        return aquecimento;
    }

    public void setAquecimento(AquecimentoDocument aquecimento) {
        this.aquecimento = aquecimento;
    }

    public Double getDistanciaKm() {
        return distanciaMetros != null ? distanciaMetros / 1000.0 : null;
    }
}
