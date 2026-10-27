package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.response.ProtocoloResumoResponse;
import com.prosup.proinsight.infrastructure.persistence.document.ProtocoloAvaliacaoDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UsuarioProtocoloFavoritoDocument;
import com.prosup.proinsight.infrastructure.persistence.repository.ProtocoloAvaliacaoRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UsuarioProtocoloFavoritoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocoloHubServiceTest {

    @Mock
    private ProtocoloAvaliacaoRepository protocoloRepository;

    @Mock
    private UsuarioProtocoloFavoritoRepository favoritoRepository;

    @InjectMocks
    private ProtocoloHubService service;

    @Test
    @DisplayName("getHub usa projection para todos e para favoritos")
    void getHubUsaProjection() {
        var favorito = new UsuarioProtocoloFavoritoDocument("user-1", "p1");
        var p1 = protocolo("p1", "Força de preensão", "FORCA");
        var p2 = protocolo("p2", "IMC", null);

        when(favoritoRepository.findByUserId("user-1")).thenReturn(List.of(favorito));
        when(protocoloRepository.findResumosByIdIn(List.of("p1"))).thenReturn(List.of(p1));
        when(protocoloRepository.findAllResumos()).thenReturn(List.of(p1, p2));

        var hub = service.getHub("user-1");

        assertThat(hub.favoritos()).extracting(ProtocoloResumoResponse::id).containsExactly("p1");

        assertThat(hub.porCategoria()).containsKeys("FORCA", "OUTROS");
        assertThat(hub.porCategoria().get("OUTROS"))
                .extracting(ProtocoloResumoResponse::id)
                .containsExactly("p2");

        verify(protocoloRepository).findAllResumos();
        verify(protocoloRepository).findResumosByIdIn(List.of("p1"));
        verify(protocoloRepository, never()).findAll();
        verify(protocoloRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("getHub sem favoritos não consulta documentos por id")
    void getHubSemFavoritosNaoConsultaFavoritos() {
        when(favoritoRepository.findByUserId("user-1")).thenReturn(List.of());
        when(protocoloRepository.findAllResumos()).thenReturn(List.of(protocolo("p2", "IMC", "IMC")));

        var hub = service.getHub("user-1");

        assertThat(hub.favoritos()).isEmpty();
        verify(protocoloRepository, never()).findResumosByIdIn(any());
        verify(protocoloRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("listarTodos usa projection e não carrega documentos completos")
    void listarTodosUsaProjection() {
        when(protocoloRepository.findAllResumos())
                .thenReturn(List.of(protocolo("p1", "Cooper", "VO2")));

        var result = service.listarTodos();

        assertThat(result).extracting(ProtocoloResumoResponse::id).containsExactly("p1");
        verify(protocoloRepository, never()).findAll();
    }

    @Test
    @DisplayName("listarFavoritos sem ids não consulta documentos")
    void listarFavoritosSemIdsNaoConsulta() {
        when(favoritoRepository.findByUserId("user-1")).thenReturn(List.of());

        var result = service.listarFavoritos("user-1");

        assertThat(result).isEmpty();
        verify(protocoloRepository, never()).findResumosByIdIn(any());
        verify(protocoloRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("listarFavoritos usa projection para ids favoritos")
    void listarFavoritosUsaProjection() {
        when(favoritoRepository.findByUserId("user-1"))
                .thenReturn(List.of(new UsuarioProtocoloFavoritoDocument("user-1", "p1")));
        when(protocoloRepository.findResumosByIdIn(List.of("p1")))
                .thenReturn(List.of(protocolo("p1", "Cooper", "VO2")));

        var result = service.listarFavoritos("user-1");

        assertThat(result).extracting(ProtocoloResumoResponse::id).containsExactly("p1");
        verify(protocoloRepository, never()).findAllById(any());
    }

    private static ProtocoloAvaliacaoDocument protocolo(String id, String nome, String categoria) {
        var doc = new ProtocoloAvaliacaoDocument();
        doc.setId(id);
        doc.setNome(nome);
        doc.setCategoria(categoria);
        doc.setDescricao("descricao " + id);
        doc.setUnidadeMedida("unidade");
        return doc;
    }
}
