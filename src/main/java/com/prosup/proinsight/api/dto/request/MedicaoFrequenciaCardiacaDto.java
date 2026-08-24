package com.prosup.proinsight.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MedicaoFrequenciaCardiacaDto {

    @NotNull(message = "tempo_decorrido_segundos é obrigatório")
    @JsonProperty("tempo_decorrido_segundos")
    private int tempoDecorridoSegundos;

    @NotNull(message = "fc_bpm é obrigatório")
    @Positive(message = "fc_bpm deve ser positivo")
    @JsonProperty("fc_bpm")
    private int fcBpm;

    public MedicaoFrequenciaCardiacaDto() {}

    public MedicaoFrequenciaCardiacaDto(int tempoDecorridoSegundos, int fcBpm) {
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
