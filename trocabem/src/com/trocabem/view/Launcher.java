package com.trocabem.view;

/**
 * Classe de partida alternativa. Algumas IDEs reclamam de "JavaFX runtime components are missing"
 * quando a classe main estende Application; iniciar por aqui contorna isso.
 */
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}
