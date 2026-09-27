package com.trocabem.view;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/** Pequenos atalhos de interface usados por todas as telas. */
public final class Ui {

    public static final String COR = "#1F4E79";

    private Ui() {
    }

    public static void info(String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, mensagem);
    }

    public static void aviso(String mensagem) {
        mostrar(Alert.AlertType.WARNING, mensagem);
    }

    public static void erro(String mensagem) {
        mostrar(Alert.AlertType.ERROR, mensagem);
    }

    private static void mostrar(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("TrocaBem");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // evita cortar textos longos
        alerta.showAndWait();
    }

    public static Label subtitulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + COR + ";");
        return label;
    }

    /** Aceita vírgula ou ponto. Lança NumberFormatException se não for número. */
    public static double lerNumero(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }
}
