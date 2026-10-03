/**
 * Módulo do Dāna. Ter este arquivo faz a IDE colocar o JavaFX no module path,
 * então dá para rodar direto a classe com.dana.view.App (sem classe "Launcher").
 */
module com.dana {
    requires javafx.controls;

    // o JavaFX precisa acessar a classe App para iniciar a aplicação
    exports com.dana.view to javafx.graphics;
}
