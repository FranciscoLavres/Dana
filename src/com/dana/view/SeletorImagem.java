package com.dana.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Path;

/**
 * Campo reutilizável de "foto opcional": escolher, ver a prévia e remover.
 * Usado na publicação de vaga (foto do trabalho) e no cadastro de serviço (foto do produto).
 */
public class SeletorImagem extends VBox {

    private final ImageView previa = new ImageView();
    private final Label rotulo = new Label();
    private final Button remover = new Button("Remover");
    private final String textoSemImagem;
    private Path caminho;

    public SeletorImagem(String textoSemImagem) {
        super(8);
        this.textoSemImagem = textoSemImagem;
        previa.setFitWidth(220);
        previa.setFitHeight(140);
        previa.setPreserveRatio(true);
        rotulo.setStyle("-fx-text-fill: gray;");

        Button escolher = new Button("Escolher imagem…");
        escolher.setOnAction(e -> {
            Path escolhido = Ui.escolherImagem(getScene().getWindow());
            if (escolhido != null) {
                definir(escolhido);
            }
        });
        remover.setOnAction(e -> limpar());

        getChildren().addAll(previa, rotulo, new HBox(8, escolher, remover));
        limpar();
    }

    /** Arquivo escolhido, ou null se não houver imagem. */
    public Path getCaminho() {
        return caminho;
    }

    public void limpar() {
        caminho = null;
        previa.setImage(null);
        previa.setVisible(false);
        previa.setManaged(false);
        rotulo.setText(textoSemImagem);
        remover.setDisable(true);
    }

    private void definir(Path arquivo) {
        caminho = arquivo;
        previa.setImage(new Image(arquivo.toUri().toString(), 440, 280, true, true, true));
        previa.setVisible(true);
        previa.setManaged(true);
        rotulo.setText(arquivo.getFileName().toString());
        remover.setDisable(false);
    }
}
