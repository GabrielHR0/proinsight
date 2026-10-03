package com.prosup.proinsight.infrastructure.persistence.document.composite;

import com.prosup.proinsight.domain.enums.TipoLimite;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("persistedPercentilFuncional")
public class PersistedPercentilFuncional extends PersistedLeaf {

    private Integer percentil;
    private Double min;
    private Double max;
    private TipoLimite tipoMin = TipoLimite.INCLUSIVO;
    private TipoLimite tipoMax = TipoLimite.INCLUSIVO;

    public PersistedPercentilFuncional() {}

    public PersistedPercentilFuncional(Integer percentil, Double min, Double max) {
        this(percentil, min, max, TipoLimite.INCLUSIVO, TipoLimite.INCLUSIVO);
    }

    public PersistedPercentilFuncional(Integer percentil, Double min, Double max, TipoLimite tipoMin, TipoLimite tipoMax) {
        this.percentil = percentil;
        this.min = min;
        this.max = max;
        this.tipoMin = tipoMin;
        this.tipoMax = tipoMax;
    }

    public Integer getPercentil() {
        return percentil;
    }

    public void setPercentil(Integer percentil) {
        this.percentil = percentil;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public TipoLimite getTipoMin() {
        return tipoMin;
    }

    public void setTipoMin(TipoLimite tipoMin) {
        this.tipoMin = tipoMin;
    }

    public TipoLimite getTipoMax() {
        return tipoMax;
    }

    public void setTipoMax(TipoLimite tipoMax) {
        this.tipoMax = tipoMax;
    }
}
