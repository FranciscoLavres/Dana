package com.dana.service;

import com.dana.exception.CadastroInvalidoException;
import com.dana.exception.DanaException;
import com.dana.exception.LoginInvalidoException;
import com.dana.exception.OperacaoInvalidaException;
import com.dana.exception.PersistenciaException;
import com.dana.exception.UsuarioNaoAutorizadoException;
import com.dana.exception.VoucherInvalidoException;
import com.dana.model.Avaliacao;
import com.dana.model.Candidatura;
import com.dana.model.CategoriaAcao;
import com.dana.model.ComImagem;
import com.dana.model.Dificuldade;
import com.dana.model.Entidade;
import com.dana.model.Instituicao;
import com.dana.model.NivelVoluntario;
import com.dana.model.Notificacao;
import com.dana.model.Parceiro;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.StatusCandidatura;
import com.dana.model.StatusResgate;
import com.dana.model.TipoServico;
import com.dana.model.Transacao;
import com.dana.model.Usuario;
import com.dana.model.Vaga;
import com.dana.model.Voluntario;
import com.dana.persistencia.BaseDados;
import com.dana.persistencia.Repositorio;
import com.dana.persistencia.RepositorioMemoria;
import com.dana.relatorio.ComprovanteHoras;
import com.dana.relatorio.RelatorioExtrato;
import com.dana.relatorio.RelatorioInstituicao;
import com.dana.relatorio.RelatorioParceiro;
import com.dana.relatorio.RelatorioTxt;
import com.dana.relatorio.RelatorioVoucher;
import com.dana.util.Formato;
import com.dana.util.Valor;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Regras de negócio do sistema. Os dados ficam num BaseDados, e toda operação que altera
 * alguma coisa termina com salvar(): o Repositorio grava na hora (arquivo ou memória).
 */
public class DanaService {

    private final Repositorio repositorio;
    private final CalculadoraCreditos calculadora = new CalculadoraCreditos();
    private final List<CategoriaAcao> categorias;
    private final BaseDados dados;
    private boolean salvamentoAutomatico = true;

    /** Sistema só em memória (nada é gravado em disco). Usado nos testes. */
    public DanaService() throws DanaException {
        this(new RepositorioMemoria());
    }

    /** Carrega os dados salvos; na primeira execução, cria os dados de exemplo. */
    public DanaService(Repositorio repositorio) throws DanaException {
        this.repositorio = repositorio;
        this.categorias = List.copyOf(repositorio.carregarCategorias());
        BaseDados salvos = repositorio.carregar();
        if (salvos != null) {
            dados = salvos;
            Entidade.sincronizarContadores(dados.todasEntidades());
        } else {
            dados = new BaseDados();
            salvamentoAutomatico = false; // grava uma vez só, no fim
            try {
                DadosExemplo.carregar(this);
            } finally {
                salvamentoAutomatico = true;
            }
            dados.setDadosDeExemplo(true);
            salvar();
        }
    }

    // ---------------------------------------------------------------- 1. Cadastro e login

    /**
     * Cadastra qualquer tipo de usuário (polimorfismo: recebe Usuario, não importa o filho).
     * Os dados já foram validados no construtor; aqui só falta ver se e-mail e documento são únicos.
     */
    public <T extends Usuario> T cadastrar(T usuario) throws DanaException {
        for (Usuario u : dados.getUsuarios()) {
            if (u.getEmail().equals(usuario.getEmail())) {
                throw new CadastroInvalidoException("Já existe um cadastro com o e-mail " + usuario.getEmail() + ".");
            }
            if (u.getDocumento().equals(usuario.getDocumento())) {
                throw new CadastroInvalidoException("Já existe um cadastro com este " + usuario.getNomeDocumento() + ".");
            }
        }
        dados.getUsuarios().add(usuario);
        salvar();
        return usuario;
    }

    public Usuario login(String email, String senha) throws LoginInvalidoException {
        String normalizado = Usuario.normalizarEmail(email);
        for (Usuario u : dados.getUsuarios()) {
            if (u.getEmail().equals(normalizado) && u.autenticar(senha)) {
                return u;
            }
        }
        throw new LoginInvalidoException();
    }

    /** true quando o sistema começou com as contas de teste (mostra a dica na tela de login). */
    public boolean isDadosDeExemplo() {
        return dados.isDadosDeExemplo();
    }

    // ---------------------------------------------------------------- 2. Vagas

    public List<CategoriaAcao> listarCategorias() {
        return categorias;
    }

    public Vaga publicarVaga(Instituicao instituicao, String titulo, String descricao, double horasPrevistas,
                             int totalVagas, CategoriaAcao categoria, Dificuldade dificuldade, LocalDate dataAcao,
                             Path imagem) throws DanaException {
        exigirTexto(titulo, "Informe o título da vaga.");
        if (horasPrevistas <= 0 || totalVagas <= 0) {
            throw new OperacaoInvalidaException("Horas previstas e número de vagas devem ser maiores que zero.");
        }
        if (categoria == null || dificuldade == null) {
            throw new OperacaoInvalidaException("Escolha a categoria e a dificuldade.");
        }
        if (dataAcao == null || dataAcao.isBefore(LocalDate.now())) {
            throw new OperacaoInvalidaException("A data da ação deve ser hoje ou uma data futura.");
        }
        BigDecimal valorHora = calculadora.calcularValorHoraVaga(categoria, dificuldade);
        Vaga vaga = new Vaga(instituicao, titulo.trim(), textoOuVazio(descricao), horasPrevistas, totalVagas,
                categoria, dificuldade, valorHora, dataAcao);
        if (imagem != null) {
            vaga.setImagem(repositorio.guardarImagem(imagem));
        }
        dados.getVagas().add(vaga);
        salvar();
        return vaga;
    }

    public void encerrarVaga(Instituicao instituicao, Vaga vaga) throws DanaException {
        exigirResponsavel(instituicao, vaga);
        vaga.encerrar();
        salvar();
    }

    public List<Vaga> listarVagasDaInstituicao(Instituicao instituicao) {
        return filtrar(dados.getVagas(), v -> v.getInstituicao().equals(instituicao));
    }

    public List<Vaga> listarVagasAbertas() {
        return filtrar(dados.getVagas(), Vaga::aceitaCandidaturas);
    }

    // ---------------------------------------------------------------- 3. Imagens (vagas e serviços)

    /** Troca a imagem de uma vaga ou serviço. imagem == null remove a imagem atual. */
    public void definirImagem(Usuario solicitante, ComImagem item, Path imagem) throws DanaException {
        exigirResponsavel(solicitante, item);
        String antiga = item.getImagem();
        item.setImagem(imagem == null ? null : repositorio.guardarImagem(imagem));
        salvar();
        if (antiga != null) {
            repositorio.apagarImagem(antiga);
        }
    }

    public Path localizarImagem(String referencia) {
        return repositorio.localizarImagem(referencia);
    }

    // ---------------------------------------------------------------- 4. Candidatura e aprovação

    public Candidatura candidatar(Voluntario voluntario, Vaga vaga) throws DanaException {
        for (Candidatura c : dados.getCandidaturas()) {
            boolean mesma = c.getVoluntario().equals(voluntario) && c.getVaga().equals(vaga);
            if (mesma && c.getStatus() != StatusCandidatura.RECUSADA) {
                throw new OperacaoInvalidaException("Você já se candidatou a esta vaga.");
            }
        }
        vaga.ocuparVaga(); // lança VagaLotadaException se não houver vaga
        Candidatura candidatura = new Candidatura(voluntario, vaga);
        dados.getCandidaturas().add(candidatura);
        notificar(vaga.getInstituicao(),
                voluntario.getNome() + " se candidatou à vaga \"" + vaga.getTitulo() + "\".");
        salvar();
        return candidatura;
    }

    public void aprovarCandidatura(Instituicao instituicao, Candidatura candidatura) throws DanaException {
        exigirResponsavel(instituicao, candidatura.getVaga());
        candidatura.aprovar();
        notificar(candidatura.getVoluntario(), "Sua candidatura para \"" + candidatura.getVaga().getTitulo()
                + "\" foi aprovada! Data da ação: " + Formato.data(candidatura.getVaga().getDataAcao()) + ".");
        salvar();
    }

    public void recusarCandidatura(Instituicao instituicao, Candidatura candidatura) throws DanaException {
        exigirResponsavel(instituicao, candidatura.getVaga());
        candidatura.recusar();
        candidatura.getVaga().liberarVaga(); // a vaga volta a ficar disponível
        notificar(candidatura.getVoluntario(),
                "Sua candidatura para \"" + candidatura.getVaga().getTitulo() + "\" não foi aprovada.");
        salvar();
    }

    public List<Candidatura> listarCandidaturasDaInstituicao(Instituicao instituicao) {
        return filtrar(dados.getCandidaturas(), c -> c.getVaga().getInstituicao().equals(instituicao));
    }

    public List<Candidatura> listarCandidaturasDoVoluntario(Voluntario voluntario) {
        return filtrar(dados.getCandidaturas(), c -> c.getVoluntario().equals(voluntario));
    }

    // ---------------------------------------------------------------- 5. Horas e créditos

    /** A instituição informa as horas trabalhadas; o sistema calcula e credita na carteira. */
    public BigDecimal confirmarHoras(Instituicao instituicao, Candidatura candidatura, double horas)
            throws DanaException {
        Vaga vaga = candidatura.getVaga();
        exigirResponsavel(instituicao, vaga);
        if (horas <= 0) {
            throw new OperacaoInvalidaException("As horas devem ser maiores que zero.");
        }
        double maximo = calculadora.horasMaximas(vaga);
        if (horas > maximo) {
            throw new OperacaoInvalidaException("Esta vaga prevê " + Formato.horas(vaga.getHorasPrevistas())
                    + " h; o máximo que pode ser confirmado é " + Formato.horas(maximo) + " h.");
        }
        Voluntario voluntario = candidatura.getVoluntario();

        // o nível conta no momento da confirmação (antes de somar estas horas)
        NivelVoluntario nivelAntes = voluntario.getNivel();
        BigDecimal creditos = calculadora.calcularCreditos(vaga, horas, nivelAntes);
        String detalhe = calculadora.detalharCalculo(vaga, horas, nivelAntes);

        candidatura.concluir(horas, creditos, detalhe); // valida o status antes de mexer na carteira
        voluntario.registrarHoras(horas);
        voluntario.getCarteira().creditar(creditos, "Horas confirmadas: " + vaga.getTitulo());
        notificar(voluntario, instituicao.getNome() + " confirmou " + Formato.horas(horas) + " h em \""
                + vaga.getTitulo() + "\". Você recebeu " + Formato.creditos(creditos) + " créditos.");
        if (voluntario.getNivel() != nivelAntes) {
            notificar(voluntario, "Parabéns! Você subiu para o nível " + voluntario.getNivel() + ".");
        }
        salvar();
        return creditos;
    }

    public CalculadoraCreditos getCalculadora() {
        return calculadora;
    }

    // ---------------------------------------------------------------- 6. Catálogo e resgate

    public Servico cadastrarServico(Parceiro parceiro, TipoServico tipo, String nome, String descricao,
                                    BigDecimal custo, Path imagem) throws DanaException {
        if (tipo == null) {
            throw new OperacaoInvalidaException("Escolha o tipo do serviço.");
        }
        exigirTexto(nome, "Informe o nome do serviço.");
        if (!Valor.positivo(custo)) {
            throw new OperacaoInvalidaException("O custo deve ser maior que zero.");
        }
        // a fábrica escolhe a subclasse certa a partir do tipo
        Servico servico = ServicoFactory.criar(tipo, nome.trim(), textoOuVazio(descricao), custo, parceiro);
        if (imagem != null) {
            servico.setImagem(repositorio.guardarImagem(imagem));
        }
        dados.getServicos().add(servico);
        salvar();
        return servico;
    }

    /** Desativar tira o serviço do catálogo (os vouchers já emitidos continuam valendo). */
    public void alterarDisponibilidade(Parceiro parceiro, Servico servico, boolean ativo) throws DanaException {
        exigirResponsavel(parceiro, servico);
        servico.setAtivo(ativo);
        salvar();
    }

    public List<Servico> listarServicosAtivos() {
        return filtrar(dados.getServicos(), Servico::isAtivo);
    }

    public List<Servico> listarServicosDoParceiro(Parceiro parceiro) {
        return filtrar(dados.getServicos(), s -> s.getParceiro().equals(parceiro));
    }

    public Resgate resgatar(Voluntario voluntario, Servico servico) throws DanaException {
        if (!servico.isAtivo()) {
            throw new OperacaoInvalidaException("Este serviço não está mais disponível.");
        }
        BigDecimal custo = servico.calcularCusto(voluntario.getNivel());
        voluntario.getCarteira().debitar(custo, "Resgate: " + servico.getNome());
        Resgate resgate = new Resgate(gerarCodigoVoucher(), voluntario, servico, custo);
        dados.getResgates().add(resgate);
        notificar(servico.getParceiro(), voluntario.getNome() + " resgatou \"" + servico.getNome()
                + "\" (voucher " + resgate.getCodigoVoucher() + ").");
        salvar();
        return resgate;
    }

    /** Cancela um voucher ainda não usado e devolve os créditos. */
    public void cancelarResgate(Voluntario voluntario, Resgate resgate) throws DanaException {
        exigirDonoDoResgate(voluntario, resgate);
        resgate.cancelar();
        voluntario.getCarteira().creditar(resgate.getCustoFinal(), "Estorno: " + resgate.getServico().getNome());
        notificar(resgate.getServico().getParceiro(), "O voucher " + resgate.getCodigoVoucher() + " ("
                + resgate.getServico().getNome() + ") foi cancelado por " + voluntario.getNome() + ".");
        salvar();
    }

    public List<Resgate> listarResgatesDoVoluntario(Voluntario voluntario) {
        return filtrar(dados.getResgates(), r -> r.getVoluntario().equals(voluntario));
    }

    public List<Resgate> listarResgatesDoParceiro(Parceiro parceiro) {
        return filtrar(dados.getResgates(), r -> r.getServico().getParceiro().equals(parceiro));
    }

    // ---------------------------------------------------------------- 7. Agendamento

    /** O voluntário escolhe quando vai usar o voucher; o parceiro vê na agenda. */
    public void agendarResgate(Voluntario voluntario, Resgate resgate, LocalDateTime dataHora)
            throws DanaException {
        exigirDonoDoResgate(voluntario, resgate);
        boolean remarcacao = resgate.getAgendadoPara() != null;
        resgate.agendar(dataHora);
        notificar(resgate.getServico().getParceiro(), (remarcacao ? "Agendamento remarcado: " : "Novo agendamento: ")
                + voluntario.getNome() + ", \"" + resgate.getServico().getNome() + "\" em "
                + Formato.dataHora(dataHora) + " (voucher " + resgate.getCodigoVoucher() + ").");
        salvar();
    }

    /** Vouchers agendados e ainda válidos, do mais próximo para o mais distante. */
    public List<Resgate> listarAgendaDoParceiro(Parceiro parceiro) {
        return listarResgatesDoParceiro(parceiro).stream()
                .filter(r -> r.getAgendadoPara() != null && r.getStatus() == StatusResgate.GERADO)
                .sorted(Comparator.comparing(Resgate::getAgendadoPara))
                .toList();
    }

    // ---------------------------------------------------------------- 8. Validação de voucher

    public Resgate validarVoucher(Parceiro parceiro, String codigo) throws DanaException {
        for (Resgate r : dados.getResgates()) {
            if (r.getCodigoVoucher().equalsIgnoreCase(codigo.trim())) {
                if (!r.getServico().getParceiro().equals(parceiro)) {
                    throw new UsuarioNaoAutorizadoException("Este voucher pertence a outro parceiro.");
                }
                r.utilizar();
                notificar(r.getVoluntario(), "Seu voucher " + r.getCodigoVoucher() + " (" + r.getServico().getNome()
                        + ") foi utilizado em " + parceiro.getNome() + ". Que tal avaliar o serviço?");
                salvar();
                return r;
            }
        }
        throw new VoucherInvalidoException("Voucher " + codigo + " não encontrado.");
    }

    // ---------------------------------------------------------------- 9. Avaliações

    /** Voluntário avalia a instituição depois que as horas foram confirmadas. */
    public Avaliacao avaliarInstituicao(Voluntario voluntario, Candidatura candidatura, int nota, String comentario)
            throws DanaException {
        if (!candidatura.getVoluntario().equals(voluntario)) {
            throw new UsuarioNaoAutorizadoException("Esta candidatura é de outro voluntário.");
        }
        Instituicao instituicao = candidatura.getVaga().getInstituicao();
        Avaliacao avaliacao = new Avaliacao(voluntario, instituicao, null, nota, comentario);
        candidatura.registrarAvaliacaoDoVoluntario(avaliacao);
        return registrarAvaliacao(avaliacao, "\"" + candidatura.getVaga().getTitulo() + "\"");
    }

    /** Instituição avalia o voluntário depois de confirmar as horas. */
    public Avaliacao avaliarVoluntario(Instituicao instituicao, Candidatura candidatura, int nota, String comentario)
            throws DanaException {
        exigirResponsavel(instituicao, candidatura.getVaga());
        Avaliacao avaliacao = new Avaliacao(instituicao, candidatura.getVoluntario(), null, nota, comentario);
        candidatura.registrarAvaliacaoDaInstituicao(avaliacao);
        return registrarAvaliacao(avaliacao, "\"" + candidatura.getVaga().getTitulo() + "\"");
    }

    /** Voluntário avalia o serviço depois de usar o voucher. */
    public Avaliacao avaliarServico(Voluntario voluntario, Resgate resgate, int nota, String comentario)
            throws DanaException {
        exigirDonoDoResgate(voluntario, resgate);
        Servico servico = resgate.getServico();
        Avaliacao avaliacao = new Avaliacao(voluntario, servico.getParceiro(), servico, nota, comentario);
        resgate.registrarAvaliacao(avaliacao);
        return registrarAvaliacao(avaliacao, "\"" + servico.getNome() + "\"");
    }

    private Avaliacao registrarAvaliacao(Avaliacao avaliacao, String sobre) throws PersistenciaException {
        dados.getAvaliacoes().add(avaliacao);
        notificar(avaliacao.getAvaliado(), avaliacao.getAutor().getNome() + " avaliou você com "
                + Formato.estrelas(avaliacao.getNota()) + " por " + sobre + ".");
        salvar();
        return avaliacao;
    }

    public List<Avaliacao> listarAvaliacoesRecebidas(Usuario usuario) {
        return filtrar(dados.getAvaliacoes(), a -> a.getAvaliado().equals(usuario));
    }

    public OptionalDouble mediaAvaliacoes(Usuario usuario) {
        return media(a -> a.getAvaliado().equals(usuario));
    }

    public OptionalDouble mediaAvaliacoes(Servico servico) {
        return media(a -> servico.equals(a.getServico()));
    }

    private OptionalDouble media(Predicate<Avaliacao> filtro) {
        return dados.getAvaliacoes().stream().filter(filtro).mapToInt(Avaliacao::getNota).average();
    }

    // ---------------------------------------------------------------- 10. Notificações

    /** Mais recentes primeiro. */
    public List<Notificacao> listarNotificacoes(Usuario usuario) {
        return dados.getNotificacoes().stream()
                .filter(n -> n.getDestinatario().equals(usuario))
                .sorted(Comparator.comparing(Notificacao::getData).reversed())
                .toList();
    }

    public long contarNotificacoesNaoLidas(Usuario usuario) {
        return dados.getNotificacoes().stream()
                .filter(n -> n.getDestinatario().equals(usuario) && !n.isLida())
                .count();
    }

    public void marcarNotificacoesComoLidas(Usuario usuario) throws PersistenciaException {
        boolean mudou = false;
        for (Notificacao n : dados.getNotificacoes()) {
            if (n.getDestinatario().equals(usuario) && !n.isLida()) {
                n.marcarComoLida();
                mudou = true;
            }
        }
        if (mudou) {
            salvar();
        }
    }

    private void notificar(Usuario destinatario, String mensagem) {
        dados.getNotificacoes().add(new Notificacao(destinatario, mensagem));
    }

    // ---------------------------------------------------------------- 11. Extrato e relatórios (.txt)

    public List<Transacao> extrato(Voluntario voluntario) {
        return voluntario.getCarteira().getExtrato();
    }

    public RelatorioTxt relatorioExtrato(Voluntario voluntario) {
        return new RelatorioExtrato(voluntario);
    }

    public RelatorioTxt comprovanteHoras(Candidatura candidatura) throws OperacaoInvalidaException {
        return new ComprovanteHoras(candidatura);
    }

    public RelatorioTxt relatorioVoucher(Resgate resgate) {
        return new RelatorioVoucher(resgate);
    }

    public RelatorioTxt relatorioInstituicao(Instituicao instituicao) {
        return new RelatorioInstituicao(instituicao, listarVagasDaInstituicao(instituicao),
                listarCandidaturasDaInstituicao(instituicao), mediaAvaliacoes(instituicao));
    }

    public RelatorioTxt relatorioParceiro(Parceiro parceiro) {
        return new RelatorioParceiro(parceiro, listarServicosDoParceiro(parceiro),
                listarResgatesDoParceiro(parceiro), mediaAvaliacoes(parceiro));
    }

    // ---------------------------------------------------------------- apoio

    private void salvar() throws PersistenciaException {
        if (salvamentoAutomatico) {
            repositorio.salvar(dados);
        }
    }

    /** Código de 8 caracteres que ainda não foi usado por nenhum voucher. */
    private String gerarCodigoVoucher() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (existeVoucher(codigo));
        return codigo;
    }

    private boolean existeVoucher(String codigo) {
        return dados.getResgates().stream().anyMatch(r -> r.getCodigoVoucher().equals(codigo));
    }

    private static <T> List<T> filtrar(List<T> origem, Predicate<? super T> condicao) {
        return origem.stream().filter(condicao).toList();
    }

    private static void exigirResponsavel(Usuario usuario, ComImagem item) throws UsuarioNaoAutorizadoException {
        if (!item.getResponsavel().equals(usuario)) {
            throw new UsuarioNaoAutorizadoException("Somente " + item.getResponsavel().getNome()
                    + " pode fazer isso.");
        }
    }

    private static void exigirDonoDoResgate(Voluntario voluntario, Resgate resgate)
            throws UsuarioNaoAutorizadoException {
        if (!resgate.getVoluntario().equals(voluntario)) {
            throw new UsuarioNaoAutorizadoException("Este voucher é de outro voluntário.");
        }
    }

    private static void exigirTexto(String texto, String mensagem) throws OperacaoInvalidaException {
        if (texto == null || texto.isBlank()) {
            throw new OperacaoInvalidaException(mensagem);
        }
    }

    private static String textoOuVazio(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
