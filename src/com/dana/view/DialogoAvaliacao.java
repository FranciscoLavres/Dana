package com.dana.view;

import com.dana.model.Avaliacao;
import com.dana.util.Formato;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/** Janela de avaliação: nota de 1 a 5 estrelas e comentário opcional. */
public class DialogoAvaliacao extends Dialog<DialogoAvaliacao.Resultado> {

    /** O que a pessoa preencheu. */
    public record Resultado(int nota, String comentario) {
    }

    public DialogoAvaliacao(String pergunta) {
        setTitle("Avaliar");
        setHeaderText(pergunta);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        List<Integer> notas = new ArrayList<>();
        for (int n = Avaliacao.NOTA_MAXIMA; n >= Avaliacao.NOTA_MINIMA; n--) {
            notas.add(n);
        }
        ChoiceBox<Integer> nota = new ChoiceBox<>(FXCollections.observableArrayList(notas));
        nota.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer n) {
                return n == null ? "" : Formato.estrelas(n) + "  (" + n + ")";
            }

            @Override
            public Integer fromString(String texto) {
                return null;
            }
        });
        nota.setValue(Avaliacao.NOTA_MAXIMA);

        TextArea comentario = new TextArea();
        comentario.setPromptText("Comentário (opcional)");
        comentario.setPrefRowCount(3);
        comentario.setWrapText(true);

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Nota"), 0, 0);
        grade.add(nota, 1, 0);
        grade.add(new Label("Comentário"), 0, 1);
        grade.add(comentario, 1, 1);
        getDialogPane().setContent(grade);

        setResultConverter(botao -> botao == ButtonType.OK
                ? new Resultado(nota.getValue(), comentario.getText().trim()) : null);
    }
}
