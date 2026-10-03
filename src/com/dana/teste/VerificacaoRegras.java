package com.dana.teste;

import com.dana.exception.CadastroInvalidoException;
import com.dana.exception.DanaException;
import com.dana.exception.OperacaoInvalidaException;
import com.dana.exception.PersistenciaException;
import com.dana.exception.SaldoInsuficienteException;
import com.dana.exception.UsuarioNaoAutorizadoException;
import com.dana.exception.VoucherInvalidoException;
import com.dana.model.Candidatura;
import com.dana.model.Carteira;
import com.dana.model.CategoriaAcao;
import com.dana.model.Dificuldade;
import com.dana.model.Instituicao;
import com.dana.model.NivelVoluntario;
import com.dana.model.Parceiro;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.StatusResgate;
import com.dana.model.TipoServico;
import com.dana.model.Usuario;
import com.dana.model.Vaga;
import com.dana.model.Voluntario;
import com.dana.persistencia.RepositorioArquivo;
import com.dana.service.CalculadoraCreditos;
import com.dana.service.DanaService;
import com.dana.util.Documento;
import com.dana.util.SenhaUtil;
import com.dana.util.Valor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Verificação automática das regras de negócio, sem biblioteca de testes (só Java).
 * Executar: rode o main desta classe na IDE. Termina com código 1 se algum teste falhar.
 */
public final class VerificacaoRegras {

    @FunctionalInterface
    private interface Teste {
        void executar() throws Exception;
    }

    private static int aprovados;
    private static int falhas;

    private VerificacaoRegras() {
    }

    public static void main(String[] args) {
        System.out.println("Verificando as regras do Dāna...\n");

        teste("valor por hora: base x dificuldade", () -> {
            CalculadoraCreditos calc = new CalculadoraCreditos();
            CategoriaAcao educacao = new CategoriaAcao("Educação", Valor.de("15"));
            igual(Valor.de("19.50"), calc.calcularValorHoraVaga(educacao, Dificuldade.MODERADO));
            CategoriaAcao cara = new CategoriaAcao("Cara", Valor.de("30"));
            igual(CalculadoraCreditos.TETO_POR_HORA, calc.calcularValorHoraVaga(cara, Dificuldade.PESADO));
        });

        teste("nível pelas horas", () -> {
            verificar(NivelVoluntario.porHoras(0) == NivelVoluntario.BRONZE, "0 h = BRONZE");
            verificar(NivelVoluntario.porHoras(20) == NivelVoluntario.PRATA, "20 h = PRATA");
            verificar(NivelVoluntario.porHoras(150) == NivelVoluntario.DIAMANTE, "150 h = DIAMANTE");
        });

        teste("carteira não fica negativa", () -> {
            Carteira carteira = new Carteira();
            carteira.creditar(Valor.de("10"), "teste");
            esperar(SaldoInsuficienteException.class, () -> carteira.debitar(Valor.de("10.01"), "teste"));
            carteira.debitar(Valor.de("3.30"), "teste");
            igual(Valor.de("6.70"), carteira.getSaldo());
            igual(Valor.de("6.70"), carteira.getExtrato().get(1).getSaldoApos());
        });

        teste("CPF e CNPJ", () -> {
            verificar(Documento.cpfValido("529.982.247-25"), "CPF válido");
            verificar(!Documento.cpfValido("111.222.333-44"), "CPF inválido");
            verificar(!Documento.cpfValido("111.111.111-11"), "CPF repetido");
            verificar(Documento.cnpjValido("12.345.678/0001-95"), "CNPJ válido");
            verificar(!Documento.cnpjValido("12.345.678/0001-90"), "CNPJ inválido");
        });

        teste("senha com hash e salt", () -> {
            String h1 = SenhaUtil.gerarHash("segredo");
            String h2 = SenhaUtil.gerarHash("segredo");
            verificar(!h1.equals(h2), "salts diferentes geram hashes diferentes");
            verificar(SenhaUtil.verificar("segredo", h1), "senha certa");
            verificar(!SenhaUtil.verificar("errada", h1), "senha errada");
        });

        teste("cadastro inválido não quebra (sem NullPointerException)", () -> {
            esperar(CadastroInvalidoException.class, () -> new Voluntario("Zé", null, "1234", "529.982.247-25"));
            esperar(CadastroInvalidoException.class, () -> new Voluntario("Zé", "ze@email.com", null, "529.982.247-25"));
            esperar(CadastroInvalidoException.class, () -> new Voluntario(" ", "ze@email.com", "1234", "529.982.247-25"));
            esperar(CadastroInvalidoException.class, () -> new Voluntario("Zé", "ze@email.com", "1234", "123"));
            esperar(CadastroInvalidoException.class, () -> new Voluntario("Zé", "sem-arroba", "1234", "529.982.247-25"));
        });

        teste("e-mail e CPF únicos", () -> {
            DanaService s = new DanaService();
            esperar(CadastroInvalidoException.class,
                    () -> s.cadastrar(new Voluntario("Outra Ana", "ANA@email.com", "1234", "529.982.247-25")));
            esperar(CadastroInvalidoException.class,
                    () -> s.cadastrar(new Voluntario("Outra", "outra@email.com", "1234", "123.456.789-09")));
            s.cadastrar(new Voluntario("Zé", "ze@email.com", "1234", "529.982.247-25"));
            verificar(s.login(" ZE@email.com ", "1234") != null, "login com e-mail em outra caixa");
        });

        teste("dados de exemplo: Ana com 78 créditos", () -> {
            DanaService s = new DanaService();
            Voluntario ana = (Voluntario) s.login("ana@email.com", "1234");
            igual(Valor.de("78"), ana.getCarteira().getSaldo());
            verificar(s.isDadosDeExemplo(), "base marcada como exemplo");
        });

        teste("fluxo completo: candidatar, aprovar, confirmar, resgatar, validar", () -> {
            DanaService s = new DanaService();
            Voluntario bruno = (Voluntario) s.login("bruno@email.com", "1234");
            Instituicao abrigo = (Instituicao) s.login("abrigo@email.com", "1234");
            Parceiro lanchonete = (Parceiro) s.login("sabor@email.com", "1234");
            Vaga vaga = s.listarVagasDaInstituicao(abrigo).get(0);

            Candidatura c = s.candidatar(bruno, vaga);
            verificar(s.contarNotificacoesNaoLidas(abrigo) == 1, "instituição notificada da candidatura");
            esperar(OperacaoInvalidaException.class, () -> s.candidatar(bruno, vaga));
            s.aprovarCandidatura(abrigo, c);
            s.confirmarHoras(abrigo, c, 3);
            verificar(Valor.positivo(bruno.getCarteira().getSaldo()), "créditos na carteira");

            Servico sorvete = s.listarServicosDoParceiro(lanchonete).get(1);
            Resgate r = s.resgatar(bruno, sorvete);
            esperar(UsuarioNaoAutorizadoException.class,
                    () -> s.validarVoucher((Parceiro) s.login("cine@email.com", "1234"), r.getCodigoVoucher()));
            s.validarVoucher(lanchonete, r.getCodigoVoucher().toLowerCase());
            esperar(VoucherInvalidoException.class, () -> s.validarVoucher(lanchonete, r.getCodigoVoucher()));
        });

        teste("limite de horas confirmadas (1,5x o previsto)", () -> {
            DanaService s = new DanaService();
            Voluntario bruno = (Voluntario) s.login("bruno@email.com", "1234");
            Instituicao ong = (Instituicao) s.login("ong@email.com", "1234");
            Vaga vaga = s.listarVagasDaInstituicao(ong).get(0); // 4 h previstas
            Candidatura c = s.candidatar(bruno, vaga);
            s.aprovarCandidatura(ong, c);
            esperar(OperacaoInvalidaException.class, () -> s.confirmarHoras(ong, c, 1000));
            esperar(OperacaoInvalidaException.class, () -> s.confirmarHoras(ong, c, 6.5));
            s.confirmarHoras(ong, c, 6);
        });

        teste("só a dona da vaga confirma horas", () -> {
            DanaService s = new DanaService();
            Voluntario bruno = (Voluntario) s.login("bruno@email.com", "1234");
            Instituicao ong = (Instituicao) s.login("ong@email.com", "1234");
            Instituicao abrigo = (Instituicao) s.login("abrigo@email.com", "1234");
            Candidatura c = s.candidatar(bruno, s.listarVagasDaInstituicao(ong).get(0));
            esperar(UsuarioNaoAutorizadoException.class, () -> s.aprovarCandidatura(abrigo, c));
        });

        teste("validações do serviço (não só da tela)", () -> {
            DanaService s = new DanaService();
            Instituicao ong = (Instituicao) s.login("ong@email.com", "1234");
            Parceiro cine = (Parceiro) s.login("cine@email.com", "1234");
            CategoriaAcao cat = s.listarCategorias().get(0);
            esperar(OperacaoInvalidaException.class, () -> s.publicarVaga(ong, " ", "", 2, 1, cat,
                    Dificuldade.LEVE, LocalDate.now(), null));
            esperar(OperacaoInvalidaException.class, () -> s.publicarVaga(ong, "Vaga", "", 2, 1, cat,
                    Dificuldade.LEVE, LocalDate.now().minusDays(1), null));
            esperar(OperacaoInvalidaException.class, () -> s.cadastrarServico(cine, TipoServico.BEM_ESTAR,
                    "", "", Valor.de("10"), null));
            esperar(OperacaoInvalidaException.class, () -> s.cadastrarServico(cine, TipoServico.BEM_ESTAR,
                    "Pipoca", "", Valor.de("0"), null));
        });

        teste("encerrar vaga e desativar serviço", () -> {
            DanaService s = new DanaService();
            Instituicao abrigo = (Instituicao) s.login("abrigo@email.com", "1234");
            Parceiro cine = (Parceiro) s.login("cine@email.com", "1234");
            Vaga vaga = s.listarVagasDaInstituicao(abrigo).get(0);
            s.encerrarVaga(abrigo, vaga);
            verificar(!s.listarVagasAbertas().contains(vaga), "vaga encerrada sai da lista");
            Servico ingresso = s.listarServicosDoParceiro(cine).get(0);
            s.alterarDisponibilidade(cine, ingresso, false);
            verificar(!s.listarServicosAtivos().contains(ingresso), "serviço inativo sai do catálogo");
        });

        teste("cancelar resgate devolve os créditos", () -> {
            DanaService s = new DanaService();
            Voluntario ana = (Voluntario) s.login("ana@email.com", "1234");
            Servico sorvete = s.listarServicosAtivos().get(1);
            Resgate r = s.resgatar(ana, sorvete);
            s.cancelarResgate(ana, r);
            igual(Valor.de("78"), ana.getCarteira().getSaldo());
            verificar(r.getStatus() == StatusResgate.CANCELADO, "status cancelado");
            esperar(OperacaoInvalidaException.class, () -> s.cancelarResgate(ana, r));
        });

        teste("agendamento dentro da validade", () -> {
            DanaService s = new DanaService();
            Voluntario ana = (Voluntario) s.login("ana@email.com", "1234");
            Parceiro spa = (Parceiro) s.login("spa@email.com", "1234");
            Resgate r = s.resgatar(ana, s.listarServicosDoParceiro(spa).get(0));
            esperar(OperacaoInvalidaException.class, () -> s.agendarResgate(ana, r, LocalDateTime.now().minusHours(1)));
            esperar(OperacaoInvalidaException.class, () -> s.agendarResgate(ana, r, LocalDateTime.now().plusDays(60)));
            s.agendarResgate(ana, r, LocalDateTime.now().plusDays(2));
            verificar(s.listarAgendaDoParceiro(spa).contains(r), "aparece na agenda do parceiro");
        });

        teste("avaliações: só depois de concluir/usar, uma vez só", () -> {
            DanaService s = new DanaService();
            Voluntario ana = (Voluntario) s.login("ana@email.com", "1234");
            Instituicao ong = (Instituicao) s.login("ong@email.com", "1234");
            Parceiro sabor = (Parceiro) s.login("sabor@email.com", "1234");
            Candidatura c = s.listarCandidaturasDoVoluntario(ana).get(0);
            s.avaliarInstituicao(ana, c, 5, "Ótima organização");
            esperar(OperacaoInvalidaException.class, () -> s.avaliarInstituicao(ana, c, 4, ""));
            s.avaliarVoluntario(ong, c, 4, "");
            esperar(OperacaoInvalidaException.class, () -> s.avaliarVoluntario(ong, c, 9, ""));

            Servico sorvete = s.listarServicosDoParceiro(sabor).get(1);
            Resgate r = s.resgatar(ana, sorvete);
            esperar(OperacaoInvalidaException.class, () -> s.avaliarServico(ana, r, 5, ""));
            s.validarVoucher(sabor, r.getCodigoVoucher());
            s.avaliarServico(ana, r, 3, "Bom");
            verificar(s.mediaAvaliacoes(ong).getAsDouble() == 5.0, "média da instituição");
            verificar(s.mediaAvaliacoes(sorvete).getAsDouble() == 3.0, "média do serviço");
            verificar(s.mediaAvaliacoes((Usuario) ana).getAsDouble() == 4.0, "média da voluntária");
        });

        teste("relatórios .txt", () -> {
            DanaService s = new DanaService();
            Voluntario ana = (Voluntario) s.login("ana@email.com", "1234");
            String extrato = s.relatorioExtrato(ana).gerar();
            verificar(extrato.contains("SALDO ATUAL") && extrato.contains("78,00"), "extrato com saldo");
            String comprovante = s.comprovanteHoras(s.listarCandidaturasDoVoluntario(ana).get(0)).gerar();
            verificar(comprovante.contains("Reforço de matemática"), "comprovante com a vaga");
            Voluntario bruno = (Voluntario) s.login("bruno@email.com", "1234");
            Candidatura pendente = s.candidatar(bruno, s.listarVagasAbertas().get(0));
            esperar(OperacaoInvalidaException.class, () -> s.comprovanteHoras(pendente));
        });

        teste("persistência: dados continuam depois de fechar", () -> {
            Path pasta = Files.createTempDirectory("dana-teste");
            try {
                DanaService primeiro = new DanaService(new RepositorioArquivo(pasta));
                Voluntario ze = primeiro.cadastrar(new Voluntario("Zé", "ze@email.com", "abcd", "529.982.247-25"));
                Instituicao ong = (Instituicao) primeiro.login("ong@email.com", "1234");
                Path foto = pasta.resolve("foto.png");
                Files.write(foto, new byte[]{1, 2, 3});
                Vaga comFoto = primeiro.publicarVaga(ong, "Com foto", "", 2, 1, primeiro.listarCategorias().get(0),
                        Dificuldade.LEVE, LocalDate.now(), foto);
                verificar(primeiro.localizarImagem(comFoto.getImagem()) != null, "imagem copiada");
                verificar(Files.exists(pasta.resolve("categorias.txt")), "categorias.txt criado");

                DanaService segundo = new DanaService(new RepositorioArquivo(pasta));
                Usuario recarregado = segundo.login("ze@email.com", "abcd");
                verificar(recarregado.equals(ze), "mesmo usuário depois de recarregar");
                Instituicao ong2 = (Instituicao) segundo.login("ong@email.com", "1234");
                Vaga nova = segundo.publicarVaga(ong2, "Nova", "", 2, 1, segundo.listarCategorias().get(0),
                        Dificuldade.LEVE, LocalDate.now(), null);
                verificar(nova.getId() > comFoto.getId(), "ids continuam de onde pararam");
                verificar(segundo.listarVagasDaInstituicao(ong2).size() == 3, "vagas recarregadas");
            } finally {
                apagar(pasta);
            }
        });

        teste("categorias.txt editado e com erro", () -> {
            Path pasta = Files.createTempDirectory("dana-teste");
            try {
                Files.writeString(pasta.resolve("categorias.txt"), "# minhas\nEducação e reforço;20\nObras;22,5\n");
                DanaService s = new DanaService(new RepositorioArquivo(pasta));
                verificar(s.listarCategorias().size() == 2, "2 categorias lidas");
                igual(Valor.de("22.5"), s.listarCategorias().get(1).getValorBaseHora());

                Files.writeString(pasta.resolve("categorias.txt"), "Sem valor\n");
                esperar(PersistenciaException.class, () -> new DanaService(new RepositorioArquivo(pasta)));
            } finally {
                apagar(pasta);
            }
        });

        teste("imagem com formato inválido é recusada", () -> {
            Path pasta = Files.createTempDirectory("dana-teste");
            try {
                DanaService s = new DanaService(new RepositorioArquivo(pasta));
                Parceiro cine = (Parceiro) s.login("cine@email.com", "1234");
                Path texto = pasta.resolve("nao-e-imagem.txt");
                Files.writeString(texto, "oi");
                esperar(OperacaoInvalidaException.class, () -> s.cadastrarServico(cine, TipoServico.BEM_ESTAR,
                        "Pipoca", "", Valor.de("10"), texto));
            } finally {
                apagar(pasta);
            }
        });

        System.out.println("\n" + aprovados + " aprovado(s), " + falhas + " falha(s).");
        System.exit(falhas == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------ mini "framework" de testes

    private static void teste(String nome, Teste teste) {
        try {
            teste.executar();
            aprovados++;
            System.out.println("  OK     " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("  FALHA  " + nome + "  ->  " + e);
        }
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError(descricao);
        }
    }

    private static void igual(Object esperado, Object obtido) {
        if (!esperado.equals(obtido)) {
            throw new AssertionError("esperado " + esperado + ", obtido " + obtido);
        }
    }

    private static void esperar(Class<? extends DanaException> tipo, Teste acao) throws Exception {
        try {
            acao.executar();
        } catch (DanaException e) {
            if (tipo.isInstance(e)) {
                return;
            }
            throw new AssertionError("esperava " + tipo.getSimpleName() + ", veio " + e);
        }
        throw new AssertionError("esperava " + tipo.getSimpleName() + ", mas nada foi lançado");
    }

    private static void apagar(Path pasta) throws IOException {
        try (Stream<Path> arquivos = Files.walk(pasta)) {
            for (Path p : arquivos.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        }
    }
}
