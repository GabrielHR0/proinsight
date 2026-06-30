package com.prosup.proinsight.adapter.out.persistence;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("medicaoBioimpedancia")
public class MedicaoBioimpedanciaDocument extends MedicaoDocument {

    private Double pesoKg;
    private Double percentualGordura;
    private Double massaMagraKg;
    private Double massaGordaKg;
    private Double aguaCorporalPercentual;
    private Double gorduraVisceral;
    private Double tmbKcal;
    private Integer idadeMetabolica;

    public MedicaoBioimpedanciaDocument() {
        super(MedicaoTipo.BIOIMPEDANCIA);
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Double getPercentualGordura() {
        return percentualGordura;
    }

    public void setPercentualGordura(Double percentualGordura) {
        this.percentualGordura = percentualGordura;
    }

    public Double getMassaMagraKg() {
        return massaMagraKg;
    }

    public void setMassaMagraKg(Double massaMagraKg) {
        this.massaMagraKg = massaMagraKg;
    }

    public Double getMassaGordaKg() {
        return massaGordaKg;
    }

    public void setMassaGordaKg(Double massaGordaKg) {
        this.massaGordaKg = massaGordaKg;
    }

    public Double getAguaCorporalPercentual() {
        return aguaCorporalPercentual;
    }

    public void setAguaCorporalPercentual(Double aguaCorporalPercentual) {
        this.aguaCorporalPercentual = aguaCorporalPercentual;
    }

    public Double getGorduraVisceral() {
        return gorduraVisceral;
    }

    public void setGorduraVisceral(Double gorduraVisceral) {
        this.gorduraVisceral = gorduraVisceral;
    }

    public Double getTmbKcal() {
        return tmbKcal;
    }

    public void setTmbKcal(Double tmbKcal) {
        this.tmbKcal = tmbKcal;
    }

    public Integer getIdadeMetabolica() {
        return idadeMetabolica;
    }

    public void setIdadeMetabolica(Integer idadeMetabolica) {
        this.idadeMetabolica = idadeMetabolica;
    }
}
