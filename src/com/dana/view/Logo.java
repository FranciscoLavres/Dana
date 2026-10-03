package com.dana.view;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;

/**
 * Logo do Dāna: duas mãos segurando um coração (ajuda e voluntariado).
 * É desenhada com formas do JavaFX (sem arquivo de imagem), numa área de 100 x 100,
 * e a mesma figura vira o ícone da janela (favicon) por meio de um snapshot.
 */
public final class Logo {

    private static final String CORACAO = "M50,64 C35,53 27,45 27,36 C27,29 32,24 38,24 "
            + "C43,24 47,27 50,31 C53,27 57,24 62,24 C68,24 73,29 73,36 C73,45 65,53 50,64 Z";
    private static final String MAO_ESQUERDA = "M14,44 C14,60 24,74 46,80 L46,86 L24,86 C16,80 10,66 10,52 Z";
    private static final String MAO_DIREITA = "M86,44 C86,60 76,74 54,80 L54,86 L76,86 C84,80 90,66 90,52 Z";
    private static final String DEDOS_ESQUERDA = "M14,44 C17,52 22,58 30,62";
    private static final String DEDOS_DIREITA = "M86,44 C83,52 78,58 70,62";

    private Logo() {
    }

    public static Node criar(double tamanho) {
        Circle fundo = new Circle(50, 50, 49, Color.web(Ui.COR));

        SVGPath coracao = forma(CORACAO, Color.web("#F28C6B"));
        SVGPath maoEsquerda = forma(MAO_ESQUERDA, Color.WHITE);
        SVGPath maoDireita = forma(MAO_DIREITA, Color.WHITE);

        Group desenho = new Group(fundo, coracao, maoEsquerda, maoDireita,
                dedo(DEDOS_ESQUERDA), dedo(DEDOS_DIREITA));
        desenho.getTransforms().add(new Scale(tamanho / 100.0, tamanho / 100.0, 0, 0));
        return new Group(desenho);
    }

    /** Ícone da janela (favicon), gerado a partir da própria logo. */
    public static Image icone(int tamanho) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return criar(tamanho).snapshot(parametros, null);
    }

    private static SVGPath forma(String caminho, Color cor) {
        SVGPath forma = new SVGPath();
        forma.setContent(caminho);
        forma.setFill(cor);
        return forma;
    }

    private static SVGPath dedo(String caminho) {
        SVGPath dedo = new SVGPath();
        dedo.setContent(caminho);
        dedo.setFill(null);
        dedo.setStroke(Color.WHITE);
        dedo.setStrokeWidth(6);
        dedo.setStrokeLineCap(StrokeLineCap.ROUND);
        return dedo;
    }
}
