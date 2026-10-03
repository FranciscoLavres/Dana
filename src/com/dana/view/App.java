package com.dana.view;

import com.dana.exception.DadosCorrompidosException;
import com.dana.exception.DanaException;
import com.dana.model.Instituicao;
import com.dana.model.Parceiro;
import com.dana.model.Usuario;
import com.dana.model.Voluntario;
import com.dana.persistencia.Repositorio;
import com.dana.persistencia.RepositorioArquivo;
import com.dana.service.DanaService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Ponto de entrada do sistema (é esta classe que se executa): abre os dados salvos,
 * guarda o sistema e troca de tela conforme o tipo de usuário.
 */
public class App extends Application {

    private DanaService sistema;
    private Stage janela;

    @Override
    public void start(Stage stage) {
        this.janela = stage;
        janela.setTitle(Ui.TITULO);
        janela.getIcons().addAll(Logo.icone(16), Logo.icone(32), Logo.icone(64), Logo.icone(128));

        sistema = abrirSistema();
        if (sistema == null) {
            Platform.exit();
            return;
        }

        janela.setWidth(1040);
        janela.setHeight(700);
        mostrarLogin();
        janela.show();
    }

    /** Pasta dos dados: ~/.dana (ou a indicada com -Ddana.dados=caminho). */
    static Path pastaDados() {
        String configurada = System.getProperty("dana.dados");
        return configurada != null ? Path.of(configurada) : Path.of(System.getProperty("user.home"), ".dana");
    }

    private DanaService abrirSistema() {
        Repositorio repositorio = new RepositorioArquivo(pastaDados());
        try {
            return new DanaService(repositorio);
        } catch (DadosCorrompidosException e) {
            if (!Ui.confirmar(e.getMessage() + "\n\nDeseja guardar uma cópia de segurança desse arquivo "
                    + "e começar de novo com os dados de exemplo?")) {
                return null;
            }
            try {
                repositorio.arquivarDados();
                return new DanaService(repositorio);
            } catch (DanaException ex) {
                Ui.erro(ex.getMessage());
                return null;
            }
        } catch (DanaException e) {
            Ui.erro("Não foi possível iniciar o sistema:\n" + e.getMessage());
            return null;
        }
    }

    public void mostrarLogin() {
        trocarTela(new TelaLogin(this, sistema));
    }

    /** Escolhe o painel pelo tipo do usuário logado. */
    public void mostrarPainel(Usuario usuario) {
        if (usuario instanceof Voluntario voluntario) {
            trocarTela(new PainelVoluntario(this, sistema, voluntario));
        } else if (usuario instanceof Instituicao instituicao) {
            trocarTela(new PainelInstituicao(this, sistema, instituicao));
        } else if (usuario instanceof Parceiro parceiro) {
            trocarTela(new PainelParceiro(this, sistema, parceiro));
        }
    }

    private void trocarTela(Parent raiz) {
        janela.setScene(new Scene(raiz, janela.getWidth(), janela.getHeight()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
