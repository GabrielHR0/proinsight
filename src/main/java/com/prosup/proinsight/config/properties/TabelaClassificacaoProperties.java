package com.prosup.proinsight.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tabelas.classificacao")
public class TabelaClassificacaoProperties {

    private String cooperId = "classificacao_cooper_12min";
    private String rockportId = "classificacao_rockport_1mile";

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
}
