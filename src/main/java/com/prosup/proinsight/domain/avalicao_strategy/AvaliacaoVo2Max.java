package com.prosup.proinsight.domain.avalicao_strategy;

import com.prosup.proinsight.adapter.out.persistence.MongoTabelaClassificacaoDataRepository;
import com.prosup.proinsight.adapter.out.persistence.converter.PersistedComponentMapper;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoVo2Max implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {

    @Transient
    private MongoTabelaClassificacaoDataRepository tabelaRepository;
    @Transient
    private PersistedComponentMapper componentMapper;

    public AvaliacaoVo2Max(
        MongoTabelaClassificacaoDataRepository tabelaRepository,
        PersistedComponentMapper componentMapper
    ) {
        this.tabelaRepository = tabelaRepository;
        this.componentMapper = componentMapper;
    }

    @Override
    public Leaf avaliar(AvaliacaoVo2MaxContext contexto) {
        var tabelaDoc = tabelaRepository.findById(contexto.getTabelaClassificacaoId())
            .orElseThrow(() -> new RuntimeException(
                "Tabela de classificação VO2Max não encontrada com ID: " + contexto.getTabelaClassificacaoId()
            ));

        var tabelaClassificacao = componentMapper.toDomain(tabelaDoc.getRaiz());

        for (TesteVo2Max teste : contexto.getTestes()) {
            var resultado = tabelaClassificacao.classificarComTeste(teste);

            if (resultado != null) {
                return resultado;
            }
        }

        return null;
    }

    @Autowired
    public void setTabelaRepository(MongoTabelaClassificacaoDataRepository tabelaRepository) {
        this.tabelaRepository = tabelaRepository;
    }

    @Autowired
    public void setComponentMapper(PersistedComponentMapper componentMapper) {
        this.componentMapper = componentMapper;
    }
}
