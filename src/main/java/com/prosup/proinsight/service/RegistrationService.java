package com.prosup.proinsight.service;

import com.prosup.proinsight.api.dto.request.RegisterRequest;
import com.prosup.proinsight.api.dto.response.LoginResponse;
import com.prosup.proinsight.bootstrap.RoleInitializer;
import com.prosup.proinsight.config.JwtTokenProvider;
import com.prosup.proinsight.domain.enums.Permissao;
import com.prosup.proinsight.domain.model.CustomUserDetails;
import com.prosup.proinsight.domain.model.User;
import com.prosup.proinsight.infrastructure.persistence.document.AcademiaDocument;
import com.prosup.proinsight.infrastructure.persistence.document.UserDocument;
import com.prosup.proinsight.infrastructure.persistence.mapper.UserMapper;
import com.prosup.proinsight.infrastructure.persistence.repository.AcademiaRepository;
import com.prosup.proinsight.infrastructure.persistence.repository.UserRepository;
import com.prosup.proinsight.infrastructure.transaction.MongoTransactionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final UserRepository userRepository;
    private final AcademiaRepository academiaRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final MongoTransactionExecutor transactionExecutor;

    public RegistrationService(UserRepository userRepository,
                               AcademiaRepository academiaRepository,
                               PasswordEncoder passwordEncoder,
                               RefreshTokenService refreshTokenService,
                               JwtTokenProvider jwtTokenProvider,
                               CustomUserDetailsService userDetailsService,
                               MongoTransactionExecutor transactionExecutor) {
        this.userRepository = userRepository;
        this.academiaRepository = academiaRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.transactionExecutor = transactionExecutor;
    }

    public LoginResponse register(RegisterRequest request) {
        boolean emailDuplicado = userRepository.findByEmail(request.getEmail()).isPresent();
        boolean userNameDuplicado = userRepository.findByUserName(request.getUserName()).isPresent();

        if (emailDuplicado || userNameDuplicado) {
            if (emailDuplicado && userNameDuplicado) {
                throw new IllegalArgumentException("Email e nome de usuário já estão em uso");
            } else if (emailDuplicado) {
                throw new IllegalArgumentException("Email já está em uso");
            } else {
                throw new IllegalArgumentException("Nome de usuário já está em uso");
            }
        }

        // BCrypt é caro e não deve manter uma transação aberta.
        String pwHash = passwordEncoder.encode(request.getPassword());

        RegistrationPersistResult persisted = transactionExecutor.execute(
                () -> persistRegistration(request, pwHash));

        User domainUser = UserMapper.toDomain(persisted.user());
        CustomUserDetails userDetails = userDetailsService.toUserDetails(domainUser);

        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String jwt = jwtTokenProvider.generateToken(auth);

        Map<String, List<String>> academiaPermissoes = new HashMap<>();
        academiaPermissoes.put(persisted.academiaId(), getPermissoes(RoleInitializer.ROLE_ADMIN_ID));

        log.info("Usuário registrado: {} (academia={})",
                request.getEmail(), persisted.academiaId());

        return new LoginResponse(
                jwt,
                persisted.refreshToken(),
                "Bearer",
                jwtTokenProvider.getExpirationMs() / 1000,
                userDetails.getUser().getId(),
                userDetails.getUser().getUserName(),
                userDetails.getUser().getEmail(),
                academiaPermissoes);
    }

    private RegistrationPersistResult persistRegistration(RegisterRequest request, String pwHash) {
        var userDoc = UserMapper.toDocument(request.getUserName(), request.getEmail(), pwHash, new HashMap<>());
        userDoc.setCref(request.getCref().trim());
        userDoc.setCpf(request.getCpf().trim());
        var savedUser = userRepository.save(userDoc);

        String academiaId = request.hasAcademia()
                ? createAcademia(savedUser.getId(), request)
                : createPersonalAcademia(savedUser.getId(), request.getUserName());

        savedUser.putAcademiaRole(academiaId, Set.of(RoleInitializer.ROLE_ADMIN_ID));
        savedUser.addAcademiaId(academiaId);
        savedUser = userRepository.save(savedUser);

        String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId()).getId();
        return new RegistrationPersistResult(savedUser, academiaId, refreshToken);
    }

    private record RegistrationPersistResult(UserDocument user,
                                             String academiaId,
                                             String refreshToken) {
    }

    private String createAcademia(String ownerId, RegisterRequest request) {
        var academiaDoc = new AcademiaDocument();
        academiaDoc.setOwnerId(ownerId);
        academiaDoc.setNomeFantasia(request.getAcademiaNome());
        academiaDoc.setCnpj(request.getCnpj());
        academiaDoc.setRazaoSocial(request.getRazaoSocial());
        academiaDoc.setTelefone(request.getTelefone());

        if (request.getEndereco() != null) {
            var endereco = new AcademiaDocument.Endereco();
            endereco.setRua(request.getEndereco().getRua());
            endereco.setNumero(request.getEndereco().getNumero());
            endereco.setCidade(request.getEndereco().getCidade());
            endereco.setEstado(request.getEndereco().getEstado());
            endereco.setCep(request.getEndereco().getCep());
            academiaDoc.setEndereco(endereco);
        }

        return academiaRepository.save(academiaDoc).getId();
    }

    private String createPersonalAcademia(String ownerId, String userName) {
        var academiaDoc = new AcademiaDocument();
        academiaDoc.setOwnerId(ownerId);
        academiaDoc.setNomeFantasia("Personal - " + userName);
        return academiaRepository.save(academiaDoc).getId();
    }

    private static List<String> getPermissoes(String roleId) {
        if (RoleInitializer.ROLE_ADMIN_ID.equals(roleId)) {
            return Set.of(
                    Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER, Permissao.CLIENTES_ATUALIZAR, Permissao.CLIENTES_EXCLUIR,
                    Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER, Permissao.AVALIACOES_ATUALIZAR, Permissao.AVALIACOES_EXCLUIR,
                    Permissao.AVALIADORES_CRIAR, Permissao.AVALIADORES_LER, Permissao.AVALIADORES_ATUALIZAR,
                    Permissao.PROTOCOLOS_LER,
                    Permissao.USUARIOS_CRIAR, Permissao.USUARIOS_LER, Permissao.USUARIOS_ATUALIZAR, Permissao.USUARIOS_EXCLUIR,
                    Permissao.ACADEMIAS_CRIAR, Permissao.ACADEMIAS_LER, Permissao.ACADEMIAS_ATUALIZAR,
                    Permissao.RELATORIOS_LER, Permissao.RELATORIOS_EXPORTAR
            ).stream().map(Enum::name).toList();
        } else {
            return Set.of(
                    Permissao.CLIENTES_CRIAR, Permissao.CLIENTES_LER, Permissao.CLIENTES_ATUALIZAR, Permissao.CLIENTES_EXCLUIR,
                    Permissao.AVALIACOES_CRIAR, Permissao.AVALIACOES_LER, Permissao.AVALIACOES_ATUALIZAR, Permissao.AVALIACOES_EXCLUIR,
                    Permissao.AVALIADORES_CRIAR, Permissao.AVALIADORES_LER, Permissao.AVALIADORES_ATUALIZAR,
                    Permissao.PROTOCOLOS_LER,
                    Permissao.USUARIOS_LER,
                    Permissao.ACADEMIAS_LER,
                    Permissao.RELATORIOS_LER, Permissao.RELATORIOS_EXPORTAR
            ).stream().map(Enum::name).toList();
        }
    }
}
