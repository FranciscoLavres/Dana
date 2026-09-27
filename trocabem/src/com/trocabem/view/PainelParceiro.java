package com.trocabem.view;

import com.trocabem.exception.TrocaBemException;
import com.trocabem.model.Parceiro;
import com.trocabem.model.Resgate;
import com.trocabem.model.Servico;
import com.trocabem.model.TipoServico;
import com.trocabem.service.ServicoFactory;
import com.trocabem.service.TrocaBemService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Painel do parceiro: cadastrar serviços e validar vouchers. */
public class PainelParceiro extends PainelBase {

    private final Parceiro parceiro;
    private final ListView<Servico> listaServicos = new ListView<>();

    public PainelParceiro(App app, TrocaBemService sistema, Parceiro parceiro) {
        super(app, sistema, parceiro, "TrocaBem - Painel do Parceiro");
        this.parceiro = parceiro;

        TabPane abas = new TabPane(criarAbaValidar(), criarAbaCadastrar(), criarAbaServicos());
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setCenter(abas);

        atualizar();
    }

    @Override
    protected void atualizar() {
        listaServicos.getItems().setAll(sistema.listarServicosDoParceiro(parceiro));
    }

    // ------------------------------------------------------------------ abas

    private Tab criarAbaValidar() {
        TextField codigo = new TextField();
        codigo.setPromptText("Código do voucher (ex.: 5DBB38AB)");
        codigo.setPrefWidth(300);
        Label resultado = new Label();
        resultado.setWrapText(true);

        Button validar = new Button("Validar e dar baixa");
        validar.setDefaultButton(true);
        validar.setOnAction(e -> {
            if (codigo.getText().isBlank()) {
                Ui.aviso("Digite o código do voucher.");
                return;
            }
            try {
                Resgate resgate = sistema.validarVoucher(parceiro, codigo.getText().trim());
                resultado.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                resultado.setText("Voucher válido! Entregar: " + resgate.getServico().getNome()
                        + " para " + resgate.getVoluntario().getNome() + ".");
                codigo.clear();
            } catch (TrocaBemException ex) {
                resultado.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                resultado.setText(ex.getMessage());
            }
        });

        VBox caixa = new VBox(12, Ui.subtitulo("Validação de voucher"), codigo, validar, resultado);
        caixa.setPadding(new Insets(15));
        return criarAba("Validar voucher", caixa);
    }

    private Tab criarAbaCadastrar() {
        TextField nome = new TextField();
        nome.setPrefWidth(350);
        TextField descricao = new TextField();
        TextField custo = new TextField();
        custo.setPromptText("Custo em créditos");
        ChoiceBox<TipoServico> tipo = new ChoiceBox<>(FXCollections.observableArrayList(TipoServico.values()));
        tipo.getSelectionModel().selectFirst();

        Button cadastrar = new Button("Cadastrar serviço");
        cadastrar.setOnAction(e -> {
            if (nome.getText().isBlank()) {
                Ui.aviso("Informe o nome do serviço.");
                return;
            }
            try {
                double valor = Ui.lerNumero(custo.getText());
                if (valor <= 0) {
                    Ui.erro("O custo deve ser maior que zero.");
                    return;
                }
                // a fábrica escolhe a subclasse certa a partir do tipo
                Servico servico = ServicoFactory.criar(tipo.getValue(), nome.getText().trim(),
                        descricao.getText().trim(), valor, parceiro);
                sistema.cadastrarServico(servico);
                Ui.info("Serviço cadastrado!");
                nome.clear();
                descricao.clear();
                custo.clear();
                atualizar();
            } catch (NumberFormatException ex) {
                Ui.erro("Digite um número válido no custo.");
            }
        });

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Tipo"), 0, 0);
        grade.add(tipo, 1, 0);
        grade.add(new Label("Nome"), 0, 1);
        grade.add(nome, 1, 1);
        grade.add(new Label("Descrição"), 0, 2);
        grade.add(descricao, 1, 2);
        grade.add(new Label("Custo (créditos)"), 0, 3);
        grade.add(custo, 1, 3);
        grade.add(cadastrar, 1, 4);

        return criarAba("Cadastrar serviço", grade);
    }

    private Tab criarAbaServicos() {
        return criarAba("Meus serviços", caixa(listaServicos));
    }
}
