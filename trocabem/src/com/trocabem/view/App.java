package com.trocabem.view;

import com.trocabem.model.Instituicao;
import com.trocabem.model.Parceiro;
import com.trocabem.model.Usuario;
import com.trocabem.model.Voluntario;
import com.trocabem.service.DadosExemplo;
import com.trocabem.service.TrocaBemService;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Ponto de entrada do JavaFX: guarda o sistema e troca de tela conforme o tipo de usuário. */
public class App extends Application {

    private final TrocaBemService sistema = new TrocaBemService();
    private Stage janela;

    @Override
    public void start(Stage stage) {
        this.janela = stage;
        DadosExemplo.carregar(sistema);
        janela.setTitle("TrocaBem");
        janela.setWidth(980);
        janela.setHeight(660);
        mostrarLogin();
        janela.show();
    }

    public void mostrarLogin() {
        trocarTela(new TelaLogin(this, sistema));
    }

    /** Escolhe o painel pelo tipo do usuário logado. */
    public void mostrarPainel(Usuario usuario) {
        if (usuario instanceof Voluntario) {
            trocarTela(new PainelVoluntario(this, sistema, (Voluntario) usuario));
        } else if (usuario instanceof Instituicao) {
            trocarTela(new PainelInstituicao(this, sistema, (Instituicao) usuario));
        } else if (usuario instanceof Parceiro) {
            trocarTela(new PainelParceiro(this, sistema, (Parceiro) usuario));
        }
    }

    private void trocarTela(Parent raiz) {
        janela.setScene(new Scene(raiz, janela.getWidth(), janela.getHeight()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
