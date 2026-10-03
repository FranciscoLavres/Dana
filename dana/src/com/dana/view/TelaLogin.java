package com.dana.view;

import com.dana.exception.DanaException;
import com.dana.model.Instituicao;
import com.dana.model.Parceiro;
import com.dana.model.Usuario;
import com.dana.model.Voluntario;
import com.dana.service.DanaService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Tela inicial: login e criação de conta. */
public class TelaLogin extends VBox {

    private static final String VOLUNTARIO = "Voluntário";
    private static final String INSTITUICAO = "Instituição";
    private static final String PARCEIRO = "Parceiro";

    private final DanaService sistema;
    private final TextField campoEmail = new TextField();
    private final PasswordField campoSenha = new PasswordField();

    public TelaLogin(App app, DanaService sistema) {
        this.sistema = sistema;

        setAlignment(Pos.CENTER);
        setSpacing(12);
        setPadding(new Insets(40));

        Label titulo = new Label("D\u0101na");
        titulo.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + Ui.COR + ";");
        Label subtitulo = new Label("Faça o bem e troque por benefícios");

        campoEmail.setPromptText("E-mail");
        campoEmail.setMaxWidth(300);
        campoSenha.setPromptText("Senha");
        campoSenha.setMaxWidth(300);

        Button entrar = new Button("Entrar");
        entrar.setDefaultButton(true);
        entrar.setPrefWidth(300);
        entrar.setOnAction(e -> {
            try {
                Usuario usuario = sistema.login(campoEmail.getText().trim(), campoSenha.getText());
                app.mostrarPainel(usuario);
            } catch (DanaException ex) {
                Ui.erro(ex.getMessage());
            }
        });

        Button criarConta = new Button("Criar conta");
        criarConta.setPrefWidth(300);
        criarConta.setOnAction(e -> abrirCadastro());

        Label dica = new Label("Contas de teste (senha 1234):\n"
                + "Voluntário: ana@email.com | bruno@email.com\n"
                + "Instituição: ong@email.com | abrigo@email.com\n"
                + "Parceiro: sabor@email.com | spa@email.com | cine@email.com");
        dica.setStyle("-fx-text-fill: gray;");

        getChildren().addAll(titulo, subtitulo, campoEmail, campoSenha, entrar, criarConta, dica);
    }

    private void abrirCadastro() {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Criar conta");
        dialogo.setHeaderText("Preencha os dados para se cadastrar");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ChoiceBox<String> tipo = new ChoiceBox<>(
                FXCollections.observableArrayList(VOLUNTARIO, INSTITUICAO, PARCEIRO));
        tipo.getSelectionModel().selectFirst();
        TextField nome = new TextField();
        TextField email = new TextField();
        PasswordField senha = new PasswordField();
        TextField documento = new TextField();
        TextField extra = new TextField();
        Label rotuloDocumento = new Label("CPF");
        Label rotuloExtra = new Label("");

        // muda os rótulos conforme o tipo escolhido
        tipo.valueProperty().addListener((obs, antigo, novo) -> {
            rotuloDocumento.setText(VOLUNTARIO.equals(novo) ? "CPF" : "CNPJ");
            rotuloExtra.setText(INSTITUICAO.equals(novo) ? "Descrição"
                    : PARCEIRO.equals(novo) ? "Tipo de negócio" : "");
            extra.setDisable(VOLUNTARIO.equals(novo));
        });
        extra.setDisable(true);

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Tipo"), 0, 0);
        grade.add(tipo, 1, 0);
        grade.add(new Label("Nome"), 0, 1);
        grade.add(nome, 1, 1);
        grade.add(new Label("E-mail"), 0, 2);
        grade.add(email, 1, 2);
        grade.add(new Label("Senha"), 0, 3);
        grade.add(senha, 1, 3);
        grade.add(rotuloDocumento, 0, 4);
        grade.add(documento, 1, 4);
        grade.add(rotuloExtra, 0, 5);
        grade.add(extra, 1, 5);
        dialogo.getDialogPane().setContent(grade);

        // Se der erro, "consumimos" o evento para o diálogo NÃO fechar e a pessoa corrigir.
        Button botaoOk = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        botaoOk.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                String n = nome.getText().trim();
                String em = email.getText().trim();
                String s = senha.getText();
                String doc = documento.getText().trim();
                String ex = extra.getText().trim();
                if (INSTITUICAO.equals(tipo.getValue())) {
                    sistema.cadastrar(new Instituicao(n, em, s, doc, ex), s);
                } else if (PARCEIRO.equals(tipo.getValue())) {
                    sistema.cadastrar(new Parceiro(n, em, s, doc, ex), s);
                } else {
                    sistema.cadastrar(new Voluntario(n, em, s, doc), s);
                }
            } catch (DanaException ex) {
                Ui.erro(ex.getMessage());
                evento.consume();
            }
        });

        dialogo.showAndWait().ifPresent(botao -> {
            if (botao == ButtonType.OK) {
                Ui.info("Conta criada! Agora é só entrar com seu e-mail e senha.");
                campoEmail.setText(email.getText().trim());
                campoSenha.clear();
            }
        });
    }
}
