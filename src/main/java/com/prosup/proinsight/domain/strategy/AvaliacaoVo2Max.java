package com.prosup.proinsight.domain.strategy;

import com.prosup.proinsight.infrastructure.persistence.repository.TabelaClassificacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.mapper.PersistedComponentMapper;
import com.prosup.proinsight.domain.model.composite.Leaf;
import com.prosup.proinsight.domain.model.teste.TesteVo2Max;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;

@Component
@StrategyFor("VO2_MAX")
public class AvaliacaoVo2Max implements AvaliacaoStrategy<AvaliacaoVo2MaxContext> {

    private TabelaClassificacaoRepository tabelaRepository;
    private PersistedComponentMapper componentMapper;

    public AvaliacaoVo2Max(
        TabelaClassificacaoRepository tabelaRepository,
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
        var dados = contexto.getDadosAvaliacao();

        for (TesteVo2Max teste : contexto.getTestes()) {
            var resultado = tabelaClassificacao.classificarComTeste(teste, dados);

            if (resultado != null) {
                return resultado;
            }
        }

        return null;
    }

    @Autowired
    public void setTabelaRepository(TabelaClassificacaoRepository tabelaRepository) {
        this.tabelaRepository = tabelaRepository;
    }

    @Autowired
    public void setComponentMapper(PersistedComponentMapper componentMapper) {
        this.componentMapper = componentMapper;
    }
}
