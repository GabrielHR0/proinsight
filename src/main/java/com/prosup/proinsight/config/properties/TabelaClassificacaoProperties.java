package com.prosup.proinsight.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tabelas.classificacao")
public class TabelaClassificacaoProperties {

    private String cooperId = "classificacao_cooper_12min";
    private String rockportId = "classificacao_rockport_1mile";
    private String ahaId = "classificacao_aha_vo2max";
    private String esteiraIncrementalId = "classificacao_esteira_incremental";
    private String imcId = "classificacao_imc_oms";

    public String getCooperId() {
        return cooperId;
    }

    public void setCooperId(String cooperId) {
        this.cooperId = cooperId;
    }

    public String getRockportId() {
        return rockportId;
    }

    public void setRockportId(String rockportId) {
        this.rockportId = rockportId;
    }

    public String getAhaId() {
        return ahaId;
    }

    public void setAhaId(String ahaId) {
        this.ahaId = ahaId;
    }

    public String getEsteiraIncrementalId() {
        return esteiraIncrementalId;
    }

    public void setEsteiraIncrementalId(String esteiraIncrementalId) {
        this.esteiraIncrementalId = esteiraIncrementalId;
    }

    public String getImcId() {
        return imcId;
    }

    public void setImcId(String imcId) {
        this.imcId = imcId;
    }
}
