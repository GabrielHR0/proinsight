package com.prosup.proinsight.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("tabelas.classificacao")
public class TabelaClassificacaoProperties {

    private String cooperId = "classificacao_cooper_12min";
    private String esteiraIncrementalId = "classificacao_esteira_incremental";
    private String imcId = "classificacao_imc_oms";
    private String fullertonSentarLevantarId = "classificacao_fullerton_sentar_levantar";
    private String fullertonFlexaoCotoveloId = "classificacao_fullerton_flexao_cotovelo";
    private String fullertonMarcha2MinId = "classificacao_fullerton_marcha_2min";
    private String fullertonSentarAlcancarPesId = "classificacao_fullerton_sentar_alcancar_pes";
    private String fullertonAlcancarCostasId = "classificacao_fullerton_alcancar_costas";
    private String fullertonLevantarCaminharId = "classificacao_fullerton_levantar_caminhar";

    public String getCooperId() {
        return cooperId;
    }

    public void setCooperId(String cooperId) {
        this.cooperId = cooperId;
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

    public String getFullertonSentarLevantarId() {
        return fullertonSentarLevantarId;
    }

    public void setFullertonSentarLevantarId(String fullertonSentarLevantarId) {
        this.fullertonSentarLevantarId = fullertonSentarLevantarId;
    }

    public String getFullertonFlexaoCotoveloId() {
        return fullertonFlexaoCotoveloId;
    }

    public void setFullertonFlexaoCotoveloId(String fullertonFlexaoCotoveloId) {
        this.fullertonFlexaoCotoveloId = fullertonFlexaoCotoveloId;
    }

    public String getFullertonMarcha2MinId() {
        return fullertonMarcha2MinId;
    }

    public void setFullertonMarcha2MinId(String fullertonMarcha2MinId) {
        this.fullertonMarcha2MinId = fullertonMarcha2MinId;
    }

    public String getFullertonSentarAlcancarPesId() {
        return fullertonSentarAlcancarPesId;
    }

    public void setFullertonSentarAlcancarPesId(String fullertonSentarAlcancarPesId) {
        this.fullertonSentarAlcancarPesId = fullertonSentarAlcancarPesId;
    }

    public String getFullertonAlcancarCostasId() {
        return fullertonAlcancarCostasId;
    }

    public void setFullertonAlcancarCostasId(String fullertonAlcancarCostasId) {
        this.fullertonAlcancarCostasId = fullertonAlcancarCostasId;
    }

    public String getFullertonLevantarCaminharId() {
        return fullertonLevantarCaminharId;
    }

    public void setFullertonLevantarCaminharId(String fullertonLevantarCaminharId) {
        this.fullertonLevantarCaminharId = fullertonLevantarCaminharId;
    }
}
