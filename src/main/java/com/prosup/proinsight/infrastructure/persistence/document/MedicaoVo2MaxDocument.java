package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.enums.Protocolo;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

@TypeAlias("medicaoVo2Max")
public class MedicaoVo2MaxDocument extends MedicaoDocument {

    private Protocolo protocolo;
    private Integer distanciaMetros;
    private Integer tempoSegundos;
    private Integer frequenciaCardiacaBpm;
    private Double pesoKg;
    private Double velocidadeKmh;
    private Double inclinacaoPercent;
    private Integer vo2MaxCalculado;
    private Double metsCalculado;
    private String classificacaoVo2;
    private List<Object> testesAdicionais;
    private List<MedicaoFrequenciaCardiacaDocument> frequenciasCardiacas;

    private AquecimentoDocument aquecimento;

    public MedicaoVo2MaxDocument() {
        super(MedicaoTipo.VO2_MAX);
    }

    public Protocolo getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(Protocolo protocolo) {
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

    public Double getVelocidadeKmh() {
        return velocidadeKmh;
    }

    public void setVelocidadeKmh(Double velocidadeKmh) {
        this.velocidadeKmh = velocidadeKmh;
    }

    public Double getInclinacaoPercent() {
        return inclinacaoPercent;
    }

    public void setInclinacaoPercent(Double inclinacaoPercent) {
        this.inclinacaoPercent = inclinacaoPercent;
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

    public Double getMetsCalculado() {
        return metsCalculado;
    }

    public void setMetsCalculado(Double metsCalculado) {
        this.metsCalculado = metsCalculado;
    }

    public List<MedicaoFrequenciaCardiacaDocument> getFrequenciasCardiacas() {
        return frequenciasCardiacas;
    }

    public void setFrequenciasCardiacas(List<MedicaoFrequenciaCardiacaDocument> frequenciasCardiacas) {
        this.frequenciasCardiacas = frequenciasCardiacas;
    }

    public static class MedicaoFrequenciaCardiacaDocument {
        private int tempoDecorridoSegundos;
        private int fcBpm;

        public MedicaoFrequenciaCardiacaDocument() {}

        public MedicaoFrequenciaCardiacaDocument(int tempoDecorridoSegundos, int fcBpm) {
            this.tempoDecorridoSegundos = tempoDecorridoSegundos;
            this.fcBpm = fcBpm;
        }

        public int getTempoDecorridoSegundos() {
            return tempoDecorridoSegundos;
        }

        public void setTempoDecorridoSegundos(int tempoDecorridoSegundos) {
            this.tempoDecorridoSegundos = tempoDecorridoSegundos;
        }

        public int getFcBpm() {
            return fcBpm;
        }

        public void setFcBpm(int fcBpm) {
            this.fcBpm = fcBpm;
        }
    }
}
