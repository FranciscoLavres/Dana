package com.dana.view;

import com.dana.exception.DanaException;
import com.dana.relatorio.RelatorioTxt;
import com.dana.service.DanaService;
import com.dana.util.Valor;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Pequenos atalhos de interface usados por todas as telas. */
public final class Ui {

    public static final String COR = "#1F4E79";
    public static final String TITULO = "Dāna";
    private static final double TAMANHO_MINIATURA = 56;
    private static final Map<String, Image> CACHE_IMAGENS = new HashMap<>();

    private Ui() {
    }

    // ------------------------------------------------------------------ mensagens

    public static void info(String mensagem) {
        mostrar(Alert.AlertType.INFORMATION, mensagem);
    }

    public static void aviso(String mensagem) {
        mostrar(Alert.AlertType.WARNING, mensagem);
    }

    public static void erro(String mensagem) {
        mostrar(Alert.AlertType.ERROR, mensagem);
    }

    public static boolean confirmar(String mensagem) {
        Alert alerta = criarAlerta(Alert.AlertType.CONFIRMATION, mensagem);
        return alerta.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private static void mostrar(Alert.AlertType tipo, String mensagem) {
        criarAlerta(tipo, mensagem).showAndWait();
    }

    private static Alert criarAlerta(Alert.AlertType tipo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(TITULO);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // evita cortar textos longos
        return alerta;
    }

    public static Label subtitulo(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + COR + ";");
        return label;
    }

    // ------------------------------------------------------------------ números

    /** Aceita vírgula ou ponto. Lança NumberFormatException se não for número. */
    public static double lerNumero(String texto) {
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    public static BigDecimal lerValor(String texto) {
        return Valor.de(texto.trim().replace(',', '.'));
    }

    // ------------------------------------------------------------------ listas

    /** Define o texto de cada linha da lista (sem depender do toString do modelo). */
    public static <T> void celulas(ListView<T> lista, Function<T, String> texto) {
        lista.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean vazio) {
                super.updateItem(item, vazio);
                setText(vazio || item == null ? null : texto.apply(item));
            }
        });
    }

    /** Igual a celulas(), mas com uma miniatura da foto (ou "sem foto") à esquerda. */
    public static <T> void celulasComImagem(ListView<T> lista, Function<T, String> texto,
                                            Function<T, String> imagem, DanaService sistema) {
        lista.setCellFactory(l -> new ListCell<>() {
            private final ImageView miniatura = new ImageView();
            private final Label semFoto = new Label("sem\nfoto");
            private final StackPane quadro = new StackPane(semFoto, miniatura);

            {
                miniatura.setFitWidth(TAMANHO_MINIATURA);
                miniatura.setFitHeight(TAMANHO_MINIATURA);
                miniatura.setPreserveRatio(true);
                semFoto.setStyle("-fx-text-fill: #8A9BB0; -fx-font-size: 10px; -fx-text-alignment: center;");
                quadro.setMinSize(TAMANHO_MINIATURA, TAMANHO_MINIATURA);
                quadro.setMaxSize(TAMANHO_MINIATURA, TAMANHO_MINIATURA);
                quadro.setAlignment(Pos.CENTER);
                quadro.setStyle("-fx-background-color: #E8EEF4; -fx-background-radius: 6;");
                setGraphicTextGap(12);
            }

            @Override
            protected void updateItem(T item, boolean vazio) {
                super.updateItem(item, vazio);
                if (vazio || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(texto.apply(item));
                Image foto = carregarImagem(sistema, imagem.apply(item), TAMANHO_MINIATURA * 2);
                miniatura.setImage(foto);
                semFoto.setVisible(foto == null);
                setGraphic(quadro);
            }
        });
    }

    // ------------------------------------------------------------------ imagens

    /** Carrega (com cache) a imagem guardada no sistema, ou null se não houver. */
    public static Image carregarImagem(DanaService sistema, String referencia, double tamanho) {
        Path arquivo = sistema.localizarImagem(referencia);
        if (arquivo == null) {
            return null;
        }
        String chave = arquivo + "@" + tamanho;
        return CACHE_IMAGENS.computeIfAbsent(chave,
                k -> new Image(arquivo.toUri().toString(), tamanho, tamanho, true, true, false));
    }

    /** Mostra a imagem num ImageView de detalhe; esconde o ImageView se não houver imagem. */
    public static void mostrarImagem(ImageView visor, DanaService sistema, String referencia) {
        Image imagem = carregarImagem(sistema, referencia, 480);
        visor.setImage(imagem);
        visor.setVisible(imagem != null);
        visor.setManaged(imagem != null);
    }

    public static Path escolherImagem(Window dono) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Escolher imagem");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Imagens (PNG, JPG, GIF, BMP)", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File arquivo = seletor.showOpenDialog(dono);
        return arquivo == null ? null : arquivo.toPath();
    }

    // ------------------------------------------------------------------ arquivos txt

    /** Abre o "Salvar como" e grava o relatório. Não faz nada se a pessoa cancelar. */
    public static void salvarRelatorio(Window dono, RelatorioTxt relatorio) throws DanaException {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Salvar arquivo de texto");
        seletor.setInitialFileName(relatorio.nomeArquivoSugerido());
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo de texto", "*.txt"));
        File arquivo = seletor.showSaveDialog(dono);
        if (arquivo != null) {
            relatorio.salvar(arquivo.toPath());
            info("Arquivo salvo em:\n" + arquivo.getAbsolutePath());
        }
    }
}
