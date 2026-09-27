package com.trocabem.view;

import com.trocabem.exception.TrocaBemException;
import com.trocabem.model.Candidatura;
import com.trocabem.model.CategoriaAcao;
import com.trocabem.model.Dificuldade;
import com.trocabem.model.Instituicao;
import com.trocabem.model.Vaga;
import com.trocabem.service.DadosExemplo;
import com.trocabem.service.TrocaBemService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Optional;

/** Painel da instituição: publicar vagas, ver candidatos, aprovar/recusar e confirmar horas. */
public class PainelInstituicao extends PainelBase {

    private final Instituicao instituicao;
    private final ListView<Vaga> listaVagas = new ListView<>();
    private final ListView<Candidatura> listaCandidaturas = new ListView<>();

    public PainelInstituicao(App app, TrocaBemService sistema, Instituicao instituicao) {
        super(app, sistema, instituicao, "TrocaBem - Painel da Instituição");
        this.instituicao = instituicao;

        TabPane abas = new TabPane(criarAbaPublicar(), criarAbaVagas(), criarAbaCandidaturas());
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setCenter(abas);

        atualizar();
    }

    @Override
    protected void atualizar() {
        listaVagas.getItems().setAll(sistema.listarVagasDaInstituicao(instituicao));
        listaCandidaturas.getItems().setAll(sistema.listarCandidaturasDaInstituicao(instituicao));
    }

    // ------------------------------------------------------------------ abas

    private Tab criarAbaPublicar() {
        TextField titulo = new TextField();
        titulo.setPromptText("Ex.: Reforço de matemática");
        titulo.setPrefWidth(420);
        TextArea descricao = new TextArea();
        descricao.setPrefRowCount(3);
        descricao.setWrapText(true);
        TextField horas = new TextField("4");
        TextField numeroVagas = new TextField("1");
        ChoiceBox<CategoriaAcao> categoria = new ChoiceBox<>(
                FXCollections.observableArrayList(DadosExemplo.categorias()));
        ChoiceBox<Dificuldade> dificuldade = new ChoiceBox<>(
                FXCollections.observableArrayList(Dificuldade.values()));
        categoria.getSelectionModel().selectFirst();
        dificuldade.getSelectionModel().selectFirst();

        // "simulador": mostra o valor por hora assim que a categoria ou a dificuldade mudam
        Label estimativa = new Label();
        Runnable recalcular = () -> {
            if (categoria.getValue() != null && dificuldade.getValue() != null) {
                double valor = sistema.getCalculadora()
                        .calcularValorHoraVaga(categoria.getValue(), dificuldade.getValue());
                estimativa.setText(String.format(
                        "Valor por hora desta vaga: %.2f créditos (antes do bônus de nível do voluntário)", valor));
            }
        };
        categoria.valueProperty().addListener((obs, antiga, nova) -> recalcular.run());
        dificuldade.valueProperty().addListener((obs, antiga, nova) -> recalcular.run());
        recalcular.run();

        Button publicar = new Button("Publicar vaga");
        publicar.setOnAction(e -> {
            if (titulo.getText().isBlank()) {
                Ui.aviso("Informe o título da vaga.");
                return;
            }
            try {
                double h = Ui.lerNumero(horas.getText());
                int n = Integer.parseInt(numeroVagas.getText().trim());
                sistema.publicarVaga(instituicao, titulo.getText().trim(), descricao.getText().trim(),
                        h, n, categoria.getValue(), dificuldade.getValue());
                Ui.info("Vaga publicada com sucesso!");
                titulo.clear();
                descricao.clear();
                atualizar();
            } catch (NumberFormatException ex) {
                Ui.erro("Digite números válidos em horas e número de vagas.");
            } catch (TrocaBemException ex) {
                Ui.erro(ex.getMessage());
            }
        });

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Título"), 0, 0);
        grade.add(titulo, 1, 0);
        grade.add(new Label("Descrição"), 0, 1);
        grade.add(descricao, 1, 1);
        grade.add(new Label("Horas previstas"), 0, 2);
        grade.add(horas, 1, 2);
        grade.add(new Label("Nº de vagas"), 0, 3);
        grade.add(numeroVagas, 1, 3);
        grade.add(new Label("Categoria"), 0, 4);
        grade.add(categoria, 1, 4);
        grade.add(new Label("Dificuldade"), 0, 5);
        grade.add(dificuldade, 1, 5);
        grade.add(estimativa, 1, 6);
        grade.add(publicar, 1, 7);

        return criarAba("Publicar vaga", grade);
    }

    private Tab criarAbaVagas() {
        return criarAba("Minhas vagas", caixa(listaVagas));
    }

    private Tab criarAbaCandidaturas() {
        Button aprovar = new Button("Aprovar");
        aprovar.setOnAction(e -> {
            Candidatura c = selecionada();
            if (c == null) {
                return;
            }
            try {
                sistema.aprovarCandidatura(instituicao, c);
                atualizar();
            } catch (TrocaBemException ex) {
                Ui.erro(ex.getMessage());
            }
        });

        Button recusar = new Button("Recusar");
        recusar.setOnAction(e -> {
            Candidatura c = selecionada();
            if (c == null) {
                return;
            }
            try {
                sistema.recusarCandidatura(instituicao, c);
                atualizar();
            } catch (TrocaBemException ex) {
                Ui.erro(ex.getMessage());
            }
        });

        Button confirmar = new Button("Confirmar horas e creditar");
        confirmar.setOnAction(e -> confirmarHoras());

        HBox botoes = new HBox(10, aprovar, recusar, confirmar);
        return criarAba("Candidaturas", caixa(listaCandidaturas, botoes));
    }

    // ------------------------------------------------------------------ ações

    private Candidatura selecionada() {
        Candidatura c = listaCandidaturas.getSelectionModel().getSelectedItem();
        if (c == null) {
            Ui.aviso("Selecione uma candidatura primeiro.");
        }
        return c;
    }

    private void confirmarHoras() {
        Candidatura c = selecionada();
        if (c == null) {
            return;
        }
        TextInputDialog dialogo = new TextInputDialog(String.valueOf(c.getVaga().getHorasPrevistas()));
        dialogo.setTitle("Confirmar horas");
        dialogo.setHeaderText("Quantas horas " + c.getVoluntario().getNome() + " trabalhou?");
        dialogo.setContentText("Horas:");
        Optional<String> resposta = dialogo.showAndWait();
        if (resposta.isEmpty()) {
            return;
        }
        try {
            double horas = Ui.lerNumero(resposta.get());
            double creditos = sistema.confirmarHoras(instituicao, c, horas);
            Ui.info(String.format("Horas confirmadas! %.2f créditos foram para a carteira de %s.\n\n%s",
                    creditos, c.getVoluntario().getNome(), c.getDetalheCalculo()));
            atualizar();
        } catch (NumberFormatException ex) {
            Ui.erro("Digite um número válido de horas.");
        } catch (TrocaBemException ex) {
            Ui.erro(ex.getMessage());
        }
    }
}
