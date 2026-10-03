package com.dana.view;

import com.dana.exception.DanaException;
import com.dana.model.Candidatura;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.Transacao;
import com.dana.model.Vaga;
import com.dana.model.Voluntario;
import com.dana.service.DanaService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Painel do voluntário: vagas, candidaturas, catálogo, vouchers e extrato. */
public class PainelVoluntario extends PainelBase {

    private final Voluntario voluntario;
    private final Label resumo = new Label();
    private final ListView<Vaga> listaVagas = new ListView<>();
    private final ListView<Candidatura> listaCandidaturas = new ListView<>();
    private final ListView<Servico> listaServicos = new ListView<>();
    private final ListView<Resgate> listaVouchers = new ListView<>();
    private final ListView<Transacao> listaExtrato = new ListView<>();

    public PainelVoluntario(App app, DanaService sistema, Voluntario voluntario) {
        super(app, sistema, voluntario, " D\u0101na - Painel do Voluntário");
        this.voluntario = voluntario;

        resumo.setPadding(new Insets(10, 12, 0, 12));
        resumo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        TabPane abas = new TabPane(criarAbaVagas(), criarAbaCandidaturas(), criarAbaCatalogo(),
                criarAbaVouchers(), criarAbaExtrato());
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox centro = new VBox(resumo, abas);
        VBox.setVgrow(abas, Priority.ALWAYS);
        setCenter(centro);

        atualizar();
    }

    @Override
    protected void atualizar() {
        resumo.setText(String.format("Saldo: %.2f créditos   |   Nível: %s   |   Horas voluntariadas: %.1f",
                voluntario.getCarteira().getSaldo(), voluntario.getNivel(), voluntario.getHorasTotais()));
        listaVagas.getItems().setAll(sistema.listarVagasAbertas());
        listaCandidaturas.getItems().setAll(sistema.listarCandidaturasDoVoluntario(voluntario));
        listaServicos.getItems().setAll(sistema.listarServicosAtivos());
        listaVouchers.getItems().setAll(sistema.listarResgatesDoVoluntario(voluntario));
        listaExtrato.getItems().setAll(sistema.extrato(voluntario));
    }

    // ------------------------------------------------------------------ abas

    private Tab criarAbaVagas() {
        Label ganho = new Label("Selecione uma vaga para ver o ganho estimado.");
        listaVagas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, vaga) -> {
            if (vaga == null) {
                ganho.setText("Selecione uma vaga para ver o ganho estimado.");
            } else {
                double creditos = sistema.getCalculadora()
                        .calcularCreditos(vaga, vaga.getHorasPrevistas(), voluntario.getNivel());
                ganho.setText(String.format("Ganho estimado: %.2f créditos (%.1f h previstas, nível %s)",
                        creditos, vaga.getHorasPrevistas(), voluntario.getNivel()));
            }
        });

        Button candidatar = new Button("Candidatar-se à vaga selecionada");
        candidatar.setOnAction(e -> {
            Vaga vaga = listaVagas.getSelectionModel().getSelectedItem();
            if (vaga == null) {
                Ui.aviso("Selecione uma vaga primeiro.");
                return;
            }
            try {
                sistema.candidatar(voluntario, vaga);
                Ui.info("Candidatura enviada! Aguarde a aprovação da instituição.");
                atualizar();
            } catch (DanaException ex) {
                Ui.erro(ex.getMessage());
            }
        });

        return criarAba("Vagas abertas", caixa(listaVagas, ganho, candidatar));
    }

    private Tab criarAbaCandidaturas() {
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
        return criarAba("Minhas candidaturas", caixa(listaCandidaturas, detalhe));
    }

    private Tab criarAbaCatalogo() {
        // célula personalizada: mostra o custo já com o desconto do nível de cada tipo de serviço
        listaServicos.setCellFactory(lista -> new ListCell<Servico>() {
            @Override
            protected void updateItem(Servico servico, boolean vazio) {
                super.updateItem(servico, vazio);
                if (vazio || servico == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %.2f créditos (%s)", servico.getNome(),
                            servico.calcularCusto(voluntario.getNivel()), servico.getParceiro().getNome()));
                }
            }
        });

        Button resgatar = new Button("Resgatar serviço selecionado");
        resgatar.setOnAction(e -> {
            Servico servico = listaServicos.getSelectionModel().getSelectedItem();
            if (servico == null) {
                Ui.aviso("Selecione um serviço primeiro.");
                return;
            }
            try {
                Resgate resgate = sistema.resgatar(voluntario, servico);
                Ui.info("Resgate realizado!\nCódigo do voucher: " + resgate.getCodigoVoucher()
                        + "\nVálido até: " + resgate.getValidade()
                        + "\n\n" + servico.descreverResgate());
                atualizar();
            } catch (DanaException ex) {
                Ui.erro(ex.getMessage());
            }
        });

        return criarAba("Catálogo de serviços", caixa(listaServicos, resgatar));
    }

    private Tab criarAbaVouchers() {
        Label instrucao = new Label("Selecione um voucher para ver como usar.");
        instrucao.setWrapText(true);
        listaVouchers.getSelectionModel().selectedItemProperty().addListener((obs, antigo, r) ->
                instrucao.setText(r == null ? "Selecione um voucher para ver como usar."
                        : r.getServico().descreverResgate()));
        return criarAba("Meus vouchers", caixa(listaVouchers, instrucao));
    }

    private Tab criarAbaExtrato() {
        listaExtrato.setStyle("-fx-font-family: monospace;");
        return criarAba("Extrato", caixa(listaExtrato));
    }
}
