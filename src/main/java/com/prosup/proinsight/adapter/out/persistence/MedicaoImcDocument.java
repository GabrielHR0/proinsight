package com.prosup.proinsight.adapter.out.persistence;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import org.springframework.data.annotation.TypeAlias;

@TypeAlias("medicaoImc")
public class MedicaoImcDocument extends MedicaoDocument {

    private Double pesoKg;
    private Double alturaM;
    private Double imcCalculado;
    private String classificacaoImc;

    public MedicaoImcDocument() {
        super(MedicaoTipo.IMC);
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Double getAlturaM() {
        return alturaM;
    }

    public void setAlturaM(Double alturaM) {
        this.alturaM = alturaM;
    }

    public Double getImcCalculado() {
        return imcCalculado;
    }

    public void setImcCalculado(Double imcCalculado) {
        this.imcCalculado = imcCalculado;
    }

    public String getClassificacaoImc() {
        return classificacaoImc;
    }

    public void setClassificacaoImc(String classificacaoImc) {
        this.classificacaoImc = classificacaoImc;
    }
}
