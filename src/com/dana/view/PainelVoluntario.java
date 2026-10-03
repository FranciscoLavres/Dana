package com.dana.view;

import com.dana.model.Candidatura;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.StatusCandidatura;
import com.dana.model.Transacao;
import com.dana.model.Vaga;
import com.dana.model.Voluntario;
import com.dana.service.DanaService;
import com.dana.util.Formato;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Painel do voluntário: vagas, candidaturas, catálogo, vouchers (com agendamento) e extrato. */
public class PainelVoluntario extends PainelBase {

    private final Voluntario voluntario;
    private final Label resumo = new Label();
    private final ListView<Vaga> listaVagas = new ListView<>();
    private final ListView<Candidatura> listaCandidaturas = new ListView<>();
    private final ListView<Servico> listaServicos = new ListView<>();
    private final ListView<Resgate> listaVouchers = new ListView<>();
    private final ListView<Transacao> listaExtrato = new ListView<>();

    public PainelVoluntario(App app, DanaService sistema, Voluntario voluntario) {
        super(app, sistema, voluntario, "Painel do Voluntário");
        this.voluntario = voluntario;

        resumo.setPadding(new Insets(10, 12, 0, 12));
        resumo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TabPane abas = montarAbas(criarAbaVagas(), criarAbaCandidaturas(), criarAbaCatalogo(),
                criarAbaVouchers(), criarAbaExtrato());

        VBox centro = new VBox(resumo, abas);
        VBox.setVgrow(abas, Priority.ALWAYS);
        setCenter(centro);

        atualizar();
    }

    @Override
    protected void atualizarConteudo() {
        resumo.setText(Formato.texto("Saldo: %s créditos   |   Nível: %s   |   Horas voluntariadas: %s",
                Formato.creditos(voluntario.getCarteira().getSaldo()), voluntario.getNivel(),
                Formato.horas(voluntario.getHorasTotais())));
        listaVagas.getItems().setAll(sistema.listarVagasAbertas());
        listaCandidaturas.getItems().setAll(sistema.listarCandidaturasDoVoluntario(voluntario));
        listaServicos.getItems().setAll(sistema.listarServicosAtivos());
        listaVouchers.getItems().setAll(sistema.listarResgatesDoVoluntario(voluntario));
        listaExtrato.getItems().setAll(sistema.extrato(voluntario));
    }

    // ------------------------------------------------------------------ abas

    private Tab criarAbaVagas() {
        Ui.celulasComImagem(listaVagas, v -> v.getTitulo() + "\n" + v.getInstituicao().getNome() + "  ·  "
                        + Formato.data(v.getDataAcao()) + "  ·  " + Formato.creditos(v.getValorPorHoraCongelado())
                        + " cred/h  ·  " + v.getVagasDisponiveis() + " vaga(s)",
                Vaga::getImagem, sistema);

        ImageView foto = imagemDeDetalhe();
        Label detalhe = textoDeDetalhe("Selecione uma vaga para ver os detalhes e o ganho estimado.");
        listaVagas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, vaga) -> {
            Ui.mostrarImagem(foto, sistema, vaga == null ? null : vaga.getImagem());
            if (vaga == null) {
                detalhe.setText("Selecione uma vaga para ver os detalhes e o ganho estimado.");
                return;
            }
            detalhe.setText(vaga.getTitulo() + "\n\n" + vaga.getDescricao()
                    + "\n\nInstituição: " + vaga.getInstituicao().getNome()
                    + " (" + Formato.media(sistema.mediaAvaliacoes(vaga.getInstituicao())) + ")"
                    + "\nData: " + Formato.data(vaga.getDataAcao())
                    + "\nCategoria: " + vaga.getCategoria().getNome() + " · " + vaga.getDificuldade()
                    + "\nHoras previstas: " + Formato.horas(vaga.getHorasPrevistas())
                    + "\n\nGanho estimado: " + Formato.creditos(sistema.getCalculadora()
                    .calcularCreditos(vaga, vaga.getHorasPrevistas(), voluntario.getNivel()))
                    + " créditos (nível " + voluntario.getNivel() + ")");
        });

        Button candidatar = new Button("Candidatar-se à vaga selecionada");
        candidatar.setOnAction(e -> {
            Vaga vaga = listaVagas.getSelectionModel().getSelectedItem();
            if (vaga == null) {
                Ui.aviso("Selecione uma vaga primeiro.");
                return;
            }
            executar(() -> {
                sistema.candidatar(voluntario, vaga);
                Ui.info("Candidatura enviada! Aguarde a aprovação da instituição.");
            });
        });

        return criarAba("Vagas abertas", caixa(listaComDetalhe(listaVagas, colunaDetalhe(foto, detalhe)), candidatar));
    }

    private Tab criarAbaCandidaturas() {
        Ui.celulas(listaCandidaturas, c -> c.getVaga().getTitulo() + "  ·  " + c.getVaga().getInstituicao().getNome()
                + "  ·  " + Formato.data(c.getVaga().getDataAcao()) + "  ·  " + c.getStatus()
                + (c.getAvaliacaoDoVoluntario() != null ? "  ·  avaliada" : ""));

        Label detalhe = new Label("Selecione uma candidatura para ver o detalhe.");
        detalhe.setWrapText(true);
        listaCandidaturas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, c) -> {
            if (c == null) {
                detalhe.setText("Selecione uma candidatura para ver o detalhe.");
            } else if (c.getDetalheCalculo() != null) {
                detalhe.setText("Cálculo dos créditos: " + c.getDetalheCalculo());
            } else {
                detalhe.setText("Status: " + c.getStatus());
            }
        });

        Button avaliar = new Button("Avaliar instituição");
        avaliar.setOnAction(e -> {
            Candidatura c = selecionado(listaCandidaturas, "Selecione uma candidatura concluída.");
            if (c == null) {
                return;
            }
            if (c.getStatus() != StatusCandidatura.CONCLUIDA) {
                Ui.aviso("Você poderá avaliar depois que a instituição confirmar suas horas.");
                return;
            }
            new DialogoAvaliacao("Como foi sua experiência com " + c.getVaga().getInstituicao().getNome() + "?")
                    .showAndWait()
                    .ifPresent(r -> executar(() -> {
                        sistema.avaliarInstituicao(voluntario, c, r.nota(), r.comentario());
                        Ui.info("Obrigado pela avaliação!");
                    }));
        });

        Button comprovante = new Button("Gerar comprovante de horas (.txt)");
        comprovante.setOnAction(e -> {
            Candidatura c = selecionado(listaCandidaturas, "Selecione uma candidatura concluída.");
            if (c != null) {
                executar(() -> Ui.salvarRelatorio(janela(), sistema.comprovanteHoras(c)));
            }
        });

        return criarAba("Minhas candidaturas", caixa(listaCandidaturas, detalhe, new HBox(10, avaliar, comprovante)));
    }

    private Tab criarAbaCatalogo() {
        // a linha mostra o custo já com o desconto do nível de cada tipo de serviço
        Ui.celulasComImagem(listaServicos, s -> s.getNome() + "  —  "
                        + Formato.creditos(s.calcularCusto(voluntario.getNivel())) + " créditos\n"
                        + s.getParceiro().getNome() + "  ·  " + s.getTipoServico() + "  ·  "
                        + Formato.media(sistema.mediaAvaliacoes(s)),
                Servico::getImagem, sistema);

        ImageView foto = imagemDeDetalhe();
        Label detalhe = textoDeDetalhe("Selecione um serviço para ver os detalhes.");
        listaServicos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, s) -> {
            Ui.mostrarImagem(foto, sistema, s == null ? null : s.getImagem());
            detalhe.setText(s == null ? "Selecione um serviço para ver os detalhes."
                    : s.getNome() + "\n\n" + s.getDescricao() + "\n\nParceiro: " + s.getParceiro().getNome()
                    + "\nAvaliação: " + Formato.media(sistema.mediaAvaliacoes(s))
                    + "\nPreço base: " + Formato.creditos(s.getCustoBase())
                    + "\nSeu preço: " + Formato.creditos(s.calcularCusto(voluntario.getNivel())) + " créditos");
        });

        Button resgatar = new Button("Resgatar serviço selecionado");
        resgatar.setOnAction(e -> {
            Servico servico = selecionado(listaServicos, "Selecione um serviço primeiro.");
            if (servico == null || !Ui.confirmar("Resgatar \"" + servico.getNome() + "\" por "
                    + Formato.creditos(servico.calcularCusto(voluntario.getNivel())) + " créditos?")) {
                return;
            }
            executar(() -> {
                Resgate resgate = sistema.resgatar(voluntario, servico);
                Ui.info("Resgate realizado!\nCódigo do voucher: " + resgate.getCodigoVoucher()
                        + "\nVálido até: " + Formato.data(resgate.getValidade())
                        + "\n\n" + servico.descreverResgate()
                        + "\n\nNa aba \"Meus vouchers\" você pode agendar o uso ou salvar o voucher em .txt.");
            });
        });

        return criarAba("Catálogo de serviços",
                caixa(listaComDetalhe(listaServicos, colunaDetalhe(foto, detalhe)), resgatar));
    }

    private Tab criarAbaVouchers() {
        Ui.celulas(listaVouchers, r -> r.getCodigoVoucher() + "  ·  " + r.getServico().getNome() + "  ·  "
                + Formato.creditos(r.getCustoFinal()) + " cred  ·  " + r.getStatus()
                + "  ·  válido até " + Formato.data(r.getValidade())
                + (r.getAgendadoPara() != null ? "  ·  agendado: " + Formato.dataHora(r.getAgendadoPara()) : ""));

        Label instrucao = new Label("Selecione um voucher para ver como usar.");
        instrucao.setWrapText(true);
        listaVouchers.getSelectionModel().selectedItemProperty().addListener((obs, antigo, r) ->
                instrucao.setText(r == null ? "Selecione um voucher para ver como usar."
                        : r.getServico().descreverResgate()));

        Button agendar = new Button("Agendar uso");
        agendar.setOnAction(e -> {
            Resgate r = selecionado(listaVouchers, "Selecione um voucher primeiro.");
            if (r != null) {
                new DialogoAgendamento(r).showAndWait().ifPresent(dataHora -> executar(() -> {
                    sistema.agendarResgate(voluntario, r, dataHora);
                    Ui.info("Agendado para " + Formato.dataHora(dataHora) + ". O parceiro foi avisado.");
                }));
            }
        });

        Button cancelar = new Button("Cancelar e estornar");
        cancelar.setOnAction(e -> {
            Resgate r = selecionado(listaVouchers, "Selecione um voucher primeiro.");
            if (r != null && Ui.confirmar("Cancelar o voucher " + r.getCodigoVoucher() + "? Os "
                    + Formato.creditos(r.getCustoFinal()) + " créditos voltam para a sua carteira.")) {
                executar(() -> sistema.cancelarResgate(voluntario, r));
            }
        });

        Button avaliar = new Button("Avaliar serviço");
        avaliar.setOnAction(e -> {
            Resgate r = selecionado(listaVouchers, "Selecione um voucher já utilizado.");
            if (r != null) {
                new DialogoAvaliacao("O que achou de \"" + r.getServico().getNome() + "\"?")
                        .showAndWait()
                        .ifPresent(res -> executar(() -> {
                            sistema.avaliarServico(voluntario, r, res.nota(), res.comentario());
                            Ui.info("Obrigado pela avaliação!");
                        }));
            }
        });

        Button salvar = new Button("Salvar voucher (.txt)");
        salvar.setOnAction(e -> {
            Resgate r = selecionado(listaVouchers, "Selecione um voucher primeiro.");
            if (r != null) {
                executar(() -> Ui.salvarRelatorio(janela(), sistema.relatorioVoucher(r)));
            }
        });

        return criarAba("Meus vouchers",
                caixa(listaVouchers, instrucao, new HBox(10, agendar, cancelar, avaliar, salvar)));
    }

    private Tab criarAbaExtrato() {
        listaExtrato.setStyle("-fx-font-family: monospace;");
        Ui.celulas(listaExtrato, Transacao::toString);
        Button exportar = new Button("Exportar extrato (.txt)");
        exportar.setOnAction(e -> executar(() -> Ui.salvarRelatorio(janela(), sistema.relatorioExtrato(voluntario))));
        return criarAba("Extrato", caixa(listaExtrato, exportar));
    }
}
