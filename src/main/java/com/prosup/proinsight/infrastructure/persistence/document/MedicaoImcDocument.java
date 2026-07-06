package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("medicaoImc")
public class MedicaoImcDocument extends MedicaoDocument {

    private Integer massaCorporalGramas;
    private Integer alturaCm;
    private Integer imcCalculado;
    private String classificacaoImc;

    public MedicaoImcDocument() {
        super(MedicaoTipo.IMC);
    }

    public Integer getMassaCorporalGramas() {
        return massaCorporalGramas;
    }

    public void setMassaCorporalGramas(Integer massaCorporalGramas) {
        this.massaCorporalGramas = massaCorporalGramas;
    }

    public Integer getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(Integer alturaCm) {
        this.alturaCm = alturaCm;
    }

    public Integer getImcCalculado() {
        return imcCalculado;
    }

    public void setImcCalculado(Integer imcCalculado) {
        this.imcCalculado = imcCalculado;
    }

    public String getClassificacaoImc() {
        return classificacaoImc;
    }

    public void setClassificacaoImc(String classificacaoImc) {
        this.classificacaoImc = classificacaoImc;
    }

    public Double getPesoKg() {
        return massaCorporalGramas != null ? massaCorporalGramas / 1000.0 : null;
    }

    public Double getAlturaMetros() {
        return alturaCm != null ? alturaCm / 100.0 : null;
    }
}
