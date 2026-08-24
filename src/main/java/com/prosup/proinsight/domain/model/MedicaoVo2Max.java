package com.prosup.proinsight.domain.model;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import com.prosup.proinsight.domain.model.aquecimento.AquecimentoVo2Max;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;

import java.time.Instant;
import java.util.List;


public class MedicaoVo2Max extends Medicao<TesteVo2Max> {

    private Integer resultado;
    private Double metsCalculado;
    private List<MedicaoFrequenciaCardiaca> frequenciasCardiacas;
    private AquecimentoVo2Max aquecimento;

    public MedicaoVo2Max() {
    }

    public MedicaoVo2Max(MedicaoTipo tipo) {
        super(tipo);
    }

    public MedicaoVo2Max(MedicaoTipo tipo, Instant medidoEm, Instant createdAt, Instant updatedAt, String observacoes, List<TesteVo2Max> testes) {
        super(tipo, medidoEm, createdAt, updatedAt, observacoes, testes);
    }

    public Integer getResultado() {
        return resultado;
    }

    public void setResultado(Integer resultado) {
        this.resultado = resultado;
    }

    public Double getMetsCalculado() {
        return metsCalculado;
    }

    public void setMetsCalculado(Double metsCalculado) {
        this.metsCalculado = metsCalculado;
    }

    public List<MedicaoFrequenciaCardiaca> getFrequenciasCardiacas() {
        return frequenciasCardiacas;
    }

    public void setFrequenciasCardiacas(List<MedicaoFrequenciaCardiaca> frequenciasCardiacas) {
        this.frequenciasCardiacas = frequenciasCardiacas;
    }

    public AquecimentoVo2Max getAquecimento() {
        return aquecimento;
    }

    public void setAquecimento(AquecimentoVo2Max aquecimento) {
        this.aquecimento = aquecimento;
    }

    public static class MedicaoFrequenciaCardiaca {
        private int tempoDecorridoSegundos;
        private int fcBpm;

        public MedicaoFrequenciaCardiaca() {}

        public MedicaoFrequenciaCardiaca(int tempoDecorridoSegundos, int fcBpm) {
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
