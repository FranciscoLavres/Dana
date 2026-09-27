package com.trocabem.view;

import com.trocabem.model.Usuario;
import com.trocabem.service.TrocaBemService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Herança na interface: os três painéis (voluntário, instituição, parceiro) herdam daqui
 * a barra superior com nome e botão "Sair", mais alguns atalhos para montar abas.
 */
public abstract class PainelBase extends BorderPane {

    protected final App app;
    protected final TrocaBemService sistema;

    protected PainelBase(App app, TrocaBemService sistema, Usuario usuario, String titulo) {
        this.app = app;
        this.sistema = sistema;

        Label rotuloTitulo = new Label(titulo);
        rotuloTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label rotuloUsuario = new Label(usuario.getNome());
        rotuloUsuario.setStyle("-fx-text-fill: white;");
        Button sair = new Button("Sair");
        sair.setOnAction(e -> app.mostrarLogin());

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);

        HBox topo = new HBox(12, rotuloTitulo, espaco, rotuloUsuario, sair);
        topo.setAlignment(Pos.CENTER_LEFT);
        topo.setPadding(new Insets(12));
        topo.setStyle("-fx-background-color: " + Ui.COR + ";");
        setTop(topo);
    }

    /** Recarrega listas e textos da tela. Cada painel implementa o seu. */
    protected abstract void atualizar();

    protected Tab criarAba(String titulo, Node conteudo) {
        Tab aba = new Tab(titulo, conteudo);
        aba.setClosable(false);
        return aba;
    }

    /** Coluna com o elemento principal (ex.: uma lista) ocupando o espaço e os extras embaixo. */
    protected VBox caixa(Node principal, Node... extras) {
        VBox caixa = new VBox(10);
        caixa.setPadding(new Insets(12));
        caixa.getChildren().add(principal);
        caixa.getChildren().addAll(extras);
        VBox.setVgrow(principal, Priority.ALWAYS);
        return caixa;
    }
}
