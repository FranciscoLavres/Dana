package com.trocabem.service;

import com.trocabem.exception.CadastroInvalidoException;
import com.trocabem.exception.LoginInvalidoException;
import com.trocabem.exception.OperacaoInvalidaException;
import com.trocabem.exception.SaldoInsuficienteException;
import com.trocabem.exception.TrocaBemException;
import com.trocabem.exception.UsuarioNaoAutorizadoException;
import com.trocabem.exception.VagaLotadaException;
import com.trocabem.exception.VoucherInvalidoException;
import com.trocabem.model.CategoriaAcao;
import com.trocabem.model.Candidatura;
import com.trocabem.model.Dificuldade;
import com.trocabem.model.Instituicao;
import com.trocabem.model.Parceiro;
import com.trocabem.model.Resgate;
import com.trocabem.model.Servico;
import com.trocabem.model.StatusCandidatura;
import com.trocabem.model.Transacao;
import com.trocabem.model.Usuario;
import com.trocabem.model.Vaga;
import com.trocabem.model.Voluntario;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras de negócio do fluxo principal, com os dados guardados em memória (listas).
 * Depois é só trocar as listas por leitura/gravação em arquivo ou banco.
 */
public class TrocaBemService {

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Vaga> vagas = new ArrayList<>();
    private final List<Candidatura> candidaturas = new ArrayList<>();
    private final List<Servico> servicos = new ArrayList<>();
    private final List<Resgate> resgates = new ArrayList<>();
    private final CalculadoraCreditos calculadora = new CalculadoraCreditos();

    // ---------------------------------------------------------------- 1. Cadastro e login

    /** Cadastra qualquer tipo de usuário (polimorfismo: recebe Usuario, não importa o filho). */
    public <T extends Usuario> T cadastrar(T usuario, String senha) throws CadastroInvalidoException {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new CadastroInvalidoException("O nome é obrigatório.");
        }
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            throw new CadastroInvalidoException("E-mail inválido.");
        }
        if (senha == null || senha.length() < 4) {
            throw new CadastroInvalidoException("A senha deve ter pelo menos 4 caracteres.");
        }
        for (Usuario u : usuarios) {
            if (u.getEmail().equals(usuario.getEmail())) {
                throw new CadastroInvalidoException("Já existe um cadastro com o e-mail " + usuario.getEmail() + ".");
            }
        }
        usuarios.add(usuario);
        return usuario;
    }

    public Usuario login(String email, String senha) throws LoginInvalidoException {
        for (Usuario u : usuarios) {
            if (u.getEmail().equals(email.toLowerCase()) && u.autenticar(senha)) {
                return u;
            }
        }
        throw new LoginInvalidoException();
    }

    // ---------------------------------------------------------------- 2. Vagas

    public Vaga publicarVaga(Instituicao instituicao, String titulo, String descricao, double horasPrevistas,
                             int totalVagas, CategoriaAcao categoria, Dificuldade dificuldade)
            throws OperacaoInvalidaException {
        if (horasPrevistas <= 0 || totalVagas <= 0) {
            throw new OperacaoInvalidaException("Horas previstas e número de vagas devem ser maiores que zero.");
        }
        double valorHora = calculadora.calcularValorHoraVaga(categoria, dificuldade);
        Vaga vaga = new Vaga(instituicao, titulo, descricao, horasPrevistas, totalVagas, categoria, dificuldade,
                valorHora);
        vagas.add(vaga);
        return vaga;
    }

    public List<Vaga> listarVagasDaInstituicao(Instituicao instituicao) {
        List<Vaga> lista = new ArrayList<>();
        for (Vaga v : vagas) {
            if (v.getInstituicao() == instituicao) {
                lista.add(v);
            }
        }
        return lista;
    }

    public List<Vaga> listarVagasAbertas() {
        List<Vaga> abertas = new ArrayList<>();
        for (Vaga v : vagas) {
            if (v.aceitaCandidaturas()) {
                abertas.add(v);
            }
        }
        return abertas;
    }

    // ---------------------------------------------------------------- 3. Candidatura e aprovação

    public Candidatura candidatar(Voluntario voluntario, Vaga vaga)
            throws VagaLotadaException, OperacaoInvalidaException {
        for (Candidatura c : candidaturas) {
            boolean mesma = c.getVoluntario() == voluntario && c.getVaga() == vaga;
            if (mesma && c.getStatus() != StatusCandidatura.RECUSADA) {
                throw new OperacaoInvalidaException("Você já se candidatou a esta vaga.");
            }
        }
        vaga.ocuparVaga(); // lança VagaLotadaException se não houver vaga
        Candidatura candidatura = new Candidatura(voluntario, vaga);
        candidaturas.add(candidatura);
        return candidatura;
    }

    public void aprovarCandidatura(Instituicao instituicao, Candidatura candidatura) throws TrocaBemException {
        exigirDonaDaVaga(instituicao, candidatura);
        candidatura.aprovar();
    }

    public void recusarCandidatura(Instituicao instituicao, Candidatura candidatura) throws TrocaBemException {
        exigirDonaDaVaga(instituicao, candidatura);
        candidatura.recusar();
        candidatura.getVaga().liberarVaga(); // a vaga volta a ficar disponível
    }

    public List<Candidatura> listarCandidaturasDaInstituicao(Instituicao instituicao) {
        List<Candidatura> lista = new ArrayList<>();
        for (Candidatura c : candidaturas) {
            if (c.getVaga().getInstituicao() == instituicao) {
                lista.add(c);
            }
        }
        return lista;
    }

    public List<Candidatura> listarCandidaturasDoVoluntario(Voluntario voluntario) {
        List<Candidatura> lista = new ArrayList<>();
        for (Candidatura c : candidaturas) {
            if (c.getVoluntario() == voluntario) {
                lista.add(c);
            }
        }
        return lista;
    }

    // ---------------------------------------------------------------- 4. Horas e créditos

    /** A instituição informa as horas trabalhadas; o sistema calcula e credita na carteira. */
    public double confirmarHoras(Instituicao instituicao, Candidatura candidatura, double horas)
            throws TrocaBemException {
        exigirDonaDaVaga(instituicao, candidatura);
        if (horas <= 0) {
            throw new OperacaoInvalidaException("As horas devem ser maiores que zero.");
        }
        Voluntario voluntario = candidatura.getVoluntario();
        Vaga vaga = candidatura.getVaga();

        // o nível conta no momento da confirmação (antes de somar estas horas)
        double creditos = calculadora.calcularCreditos(vaga, horas, voluntario.getNivel());
        String detalhe = calculadora.detalharCalculo(vaga, horas, voluntario.getNivel());

        candidatura.concluir(horas, creditos, detalhe); // valida o status antes de mexer na carteira
        voluntario.registrarHoras(horas);
        voluntario.getCarteira().creditar(creditos, "Horas confirmadas: " + vaga.getTitulo());
        return creditos;
    }

    // ---------------------------------------------------------------- 5. Catálogo e resgate

    public Servico cadastrarServico(Servico servico) {
        servicos.add(servico);
        return servico;
    }

    public List<Servico> listarServicosAtivos() {
        List<Servico> ativos = new ArrayList<>();
        for (Servico s : servicos) {
            if (s.isAtivo()) {
                ativos.add(s); // lista de Servico tratada de forma única, seja qual for o filho
            }
        }
        return ativos;
    }

    public List<Servico> listarServicosDoParceiro(Parceiro parceiro) {
        List<Servico> lista = new ArrayList<>();
        for (Servico s : servicos) {
            if (s.getParceiro() == parceiro) {
                lista.add(s);
            }
        }
        return lista;
    }

    public Resgate resgatar(Voluntario voluntario, Servico servico)
            throws SaldoInsuficienteException, OperacaoInvalidaException {
        if (!servico.isAtivo()) {
            throw new OperacaoInvalidaException("Este serviço não está mais disponível.");
        }
        double custo = servico.calcularCusto(voluntario.getNivel());
        voluntario.getCarteira().debitar(custo, "Resgate: " + servico.getNome());
        Resgate resgate = new Resgate(voluntario, servico, custo);
        resgates.add(resgate);
        return resgate;
    }

    // ---------------------------------------------------------------- 6. Validação de voucher

    public Resgate validarVoucher(Parceiro parceiro, String codigo)
            throws VoucherInvalidoException, UsuarioNaoAutorizadoException {
        for (Resgate r : resgates) {
            if (r.getCodigoVoucher().equalsIgnoreCase(codigo)) {
                if (r.getServico().getParceiro() != parceiro) {
                    throw new UsuarioNaoAutorizadoException("Este voucher pertence a outro parceiro.");
                }
                r.utilizar();
                return r;
            }
        }
        throw new VoucherInvalidoException("Voucher " + codigo + " não encontrado.");
    }

    // ---------------------------------------------------------------- 8. Extrato

    public List<Transacao> extrato(Voluntario voluntario) {
        return voluntario.getCarteira().getExtrato();
    }

    public List<Resgate> listarResgatesDoVoluntario(Voluntario voluntario) {
        List<Resgate> lista = new ArrayList<>();
        for (Resgate r : resgates) {
            if (r.getVoluntario() == voluntario) {
                lista.add(r);
            }
        }
        return lista;
    }

    public CalculadoraCreditos getCalculadora() {
        return calculadora;
    }

    // ---------------------------------------------------------------- apoio

    private void exigirDonaDaVaga(Instituicao instituicao, Candidatura candidatura)
            throws UsuarioNaoAutorizadoException {
        if (candidatura.getVaga().getInstituicao() != instituicao) {
            throw new UsuarioNaoAutorizadoException("Somente a instituição dona da vaga pode fazer isso.");
        }
    }
}
