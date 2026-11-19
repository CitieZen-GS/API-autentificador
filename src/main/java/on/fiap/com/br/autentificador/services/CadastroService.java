package on.fiap.com.br.autentificador.services;

import lombok.RequiredArgsConstructor;
import on.fiap.com.br.autentificador.dto.CadastroCuidadorRequisicao;
import on.fiap.com.br.autentificador.dto.CadastroParenteRequisicao;
import on.fiap.com.br.autentificador.dto.TokenResposta;
import on.fiap.com.br.autentificador.dto.UsuarioResposta;
import on.fiap.com.br.autentificador.models.Cuidador;
import on.fiap.com.br.autentificador.models.Idoso;
import on.fiap.com.br.autentificador.models.Parente;
import on.fiap.com.br.autentificador.models.Perfil;
import on.fiap.com.br.autentificador.repositories.CuidadorRepository;
import on.fiap.com.br.autentificador.repositories.IdosoRepository;
import on.fiap.com.br.autentificador.repositories.ParenteRepository;
import on.fiap.com.br.autentificador.repositories.PerfilRepository;
import on.fiap.com.br.autentificador.services.excecoes.AcessoNegadoException;
import on.fiap.com.br.autentificador.services.excecoes.ConflitoException;
import on.fiap.com.br.autentificador.services.excecoes.RecursoNaoEncontradoException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Criação de contas.
 * <ul>
 *   <li>Cuidador: cadastro público. A conta já sai logada (devolve os tokens).</li>
 *   <li>Parente (FAMILIAR): só o cuidador logado cria, e só para um idoso dele.
 *       Assim ninguém ganha acesso aos dados de um idoso que não acompanha.</li>
 * </ul>
 * O e-mail é único entre cuidadores e parentes, para o login saber qual conta abrir.
 */
@Service
@RequiredArgsConstructor
public class CadastroService {

    static final String PERFIL_CUIDADOR = "Cuidador";
    static final String PERFIL_FAMILIAR = "Familiar";

    private final CuidadorRepository cuidadorRepository;
    private final ParenteRepository parenteRepository;
    private final PerfilRepository perfilRepository;
    private final IdosoRepository idosoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public TokenResposta cadastrarCuidador(CadastroCuidadorRequisicao requisicao) {
        String email = normalizarEmail(requisicao.email());
        validarEmailLivre(email);

        String cpf = formatarCpf(requisicao.cpf());
        if (cpf != null && cuidadorRepository.existsByCpf(cpf)) {
            throw new ConflitoException("CPF já cadastrado");
        }

        Cuidador cuidador = new Cuidador();
        cuidador.setNome(requisicao.nome().trim());
        cuidador.setCpf(cpf);
        cuidador.setEmail(email);
        cuidador.setSenha(passwordEncoder.encode(requisicao.senha()));
        cuidador.setPerfil(buscarPerfil(PERFIL_CUIDADOR));

        return autenticacaoService.emitirTokens(cuidadorRepository.save(cuidador));
    }

    @Transactional
    public UsuarioResposta cadastrarParente(Long idCuidadorLogado, CadastroParenteRequisicao requisicao) {
        Idoso idoso = idosoRepository.findById(requisicao.idIdoso())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Idoso", requisicao.idIdoso()));
        if (!idoso.getIdCuidador().equals(idCuidadorLogado)) {
            throw new AcessoNegadoException("Só o cuidador do idoso pode cadastrar familiares para ele");
        }

        String email = normalizarEmail(requisicao.email());
        validarEmailLivre(email);

        String telefone = textoOuNulo(requisicao.telefone());
        if (telefone != null && parenteRepository.existsByTelefone(telefone)) {
            throw new ConflitoException("Telefone já cadastrado");
        }

        Parente parente = new Parente();
        parente.setNome(requisicao.nome().trim());
        parente.setTelefone(telefone);
        parente.setEmail(email);
        parente.setSenha(passwordEncoder.encode(requisicao.senha()));
        parente.setIdIdoso(idoso.getId());
        parente.setPerfil(buscarPerfil(PERFIL_FAMILIAR));

        return UsuarioResposta.de(UsuarioAutenticado.de(parenteRepository.save(parente)));
    }

    /** E-mails são guardados e buscados em minúsculas (o login faz o mesmo). */
    static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarEmailLivre(String email) {
        if (cuidadorRepository.existsByEmail(email) || parenteRepository.existsByEmail(email)) {
            throw new ConflitoException("E-mail já cadastrado");
        }
    }

    private Perfil buscarPerfil(String nome) {
        return perfilRepository.findFirstByNomeOrderByIdAsc(nome)
                .orElseThrow(() -> new IllegalStateException(
                        "Perfil '" + nome + "' não existe no banco: confira a migration M_AUT_V02"));
    }

    /** Aceita 000.000.000-00 ou 11 dígitos e salva sempre como 000.000.000-00. */
    private String formatarCpf(String cpf) {
        String texto = textoOuNulo(cpf);
        if (texto == null) {
            return null;
        }
        String digitos = texto.replaceAll("\\D", "");
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9)
                + "-" + digitos.substring(9);
    }

    private String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
