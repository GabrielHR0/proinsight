package com.prosup.proinsight.infrastructure.persistence.document;

import com.prosup.proinsight.domain.enums.MedicaoTipo;
import org.springframework.data.annotation.TypeAlias;

import java.util.LinkedHashMap;
import java.util.Map;

@TypeAlias("medicaoFuncional")
public class MedicaoFuncionalDocument extends MedicaoDocument {

    private Double sentarLevantar30s;
    private Double flexaoCotovelo30s;
    private Double marchaEstacionaria2Min;
    private Double sentarAlcancarPes;
    private Double alcancarCostas;
    private Double levantarCaminhar25m;
    private Map<String, String> classificacoes = new LinkedHashMap<>();
    private Map<String, Integer> percentis = new LinkedHashMap<>();

    public MedicaoFuncionalDocument() {
        super(MedicaoTipo.FUNCIONAL);
    }

    public Double getSentarLevantar30s() {
        return sentarLevantar30s;
    }

    public void setSentarLevantar30s(Double sentarLevantar30s) {
        this.sentarLevantar30s = sentarLevantar30s;
    }

    public Double getFlexaoCotovelo30s() {
        return flexaoCotovelo30s;
    }

    public void setFlexaoCotovelo30s(Double flexaoCotovelo30s) {
        this.flexaoCotovelo30s = flexaoCotovelo30s;
    }

    public Double getMarchaEstacionaria2Min() {
        return marchaEstacionaria2Min;
    }

    public void setMarchaEstacionaria2Min(Double marchaEstacionaria2Min) {
        this.marchaEstacionaria2Min = marchaEstacionaria2Min;
    }

    public Double getSentarAlcancarPes() {
        return sentarAlcancarPes;
    }

    public void setSentarAlcancarPes(Double sentarAlcancarPes) {
        this.sentarAlcancarPes = sentarAlcancarPes;
    }

    public Double getAlcancarCostas() {
        return alcancarCostas;
    }

    public void setAlcancarCostas(Double alcancarCostas) {
        this.alcancarCostas = alcancarCostas;
    }

    public Double getLevantarCaminhar25m() {
        return levantarCaminhar25m;
    }

    public void setLevantarCaminhar25m(Double levantarCaminhar25m) {
        this.levantarCaminhar25m = levantarCaminhar25m;
    }

    public Map<String, String> getClassificacoes() {
        return classificacoes;
    }

    public void setClassificacoes(Map<String, String> classificacoes) {
        this.classificacoes = classificacoes != null ? classificacoes : new LinkedHashMap<>();
    }

    public Map<String, Integer> getPercentis() {
        return percentis;
    }

    public void setPercentis(Map<String, Integer> percentis) {
        this.percentis = percentis != null ? percentis : new LinkedHashMap<>();
    }
}
