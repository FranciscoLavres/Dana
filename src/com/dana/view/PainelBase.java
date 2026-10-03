package com.dana.view;

import com.dana.exception.DanaException;
import com.dana.model.Avaliacao;
import com.dana.model.Notificacao;
import com.dana.model.Usuario;
import com.dana.service.DanaService;
import com.dana.util.Formato;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Herança na interface: os três painéis (voluntário, instituição, parceiro) herdam daqui
 * a barra superior com nome e botão "Sair", as abas comuns (Avaliações e Notificações)
 * e alguns atalhos para montar abas e executar ações.
 */
public abstract class PainelBase extends BorderPane {

    /** Ação de tela que pode falhar com uma regra de negócio. */
    @FunctionalInterface
    protected interface Acao {
        void executar() throws DanaException;
    }

    private static final double LARGURA_DETALHE = 280;

    protected final App app;
    protected final DanaService sistema;
    private final Usuario usuario;
    private final Tab abaNotificacoes = new Tab();
    private final ListView<Notificacao> listaNotificacoes = new ListView<>();
    private final ListView<Avaliacao> listaAvaliacoes = new ListView<>();
    private final Label mediaRecebida = new Label();

    protected PainelBase(App app, DanaService sistema, Usuario usuario, String titulo) {
        this.app = app;
        this.sistema = sistema;
        this.usuario = usuario;

        Label rotuloTitulo = new Label(Ui.TITULO + " - " + titulo);
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

    /** Recarrega listas e textos do painel específico. */
    protected abstract void atualizarConteudo();

    /** Recarrega tudo: o conteúdo do painel e as abas comuns. */
    public final void atualizar() {
        atualizarConteudo();
        listaNotificacoes.getItems().setAll(sistema.listarNotificacoes(usuario));
        long naoLidas = sistema.contarNotificacoesNaoLidas(usuario);
        abaNotificacoes.setText(naoLidas == 0 ? "Notificações" : "Notificações (" + naoLidas + ")");
        listaAvaliacoes.getItems().setAll(sistema.listarAvaliacoesRecebidas(usuario));
        mediaRecebida.setText("Sua avaliação média: " + Formato.media(sistema.mediaAvaliacoes(usuario))
                + "  (" + listaAvaliacoes.getItems().size() + " avaliação(ões))");
    }

    /** Junta as abas do perfil com as abas comuns. Ao trocar de aba, a tela é recarregada. */
    protected final TabPane montarAbas(Tab... abasDoPerfil) {
        TabPane abas = new TabPane(abasDoPerfil);
        abas.getTabs().addAll(criarAbaAvaliacoes(), criarAbaNotificacoes());
        abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        abas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> {
            if (antiga == abaNotificacoes) {
                executar(() -> sistema.marcarNotificacoesComoLidas(usuario)); // saiu da aba: já leu
            } else {
                atualizar();
            }
        });
        return abas;
    }

    /** Roda a ação, mostra o erro de regra de negócio (se houver) e recarrega a tela. */
    protected final void executar(Acao acao) {
        try {
            acao.executar();
        } catch (DanaException ex) {
            Ui.erro(ex.getMessage());
        }
        atualizar();
    }

    protected final Window janela() {
        return getScene() == null ? null : getScene().getWindow();
    }

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

    /** Lista à esquerda e um painel de detalhes à direita. */
    protected HBox listaComDetalhe(ListView<?> lista, Node detalhe) {
        HBox linha = new HBox(12, lista, detalhe);
        HBox.setHgrow(lista, Priority.ALWAYS);
        return linha;
    }

    /** Item selecionado na lista; se não houver, mostra o aviso e devolve null. */
    protected static <T> T selecionado(ListView<T> lista, String aviso) {
        T item = lista.getSelectionModel().getSelectedItem();
        if (item == null) {
            Ui.aviso(aviso);
        }
        return item;
    }

    /** Foto grande do painel de detalhes (fica escondida enquanto não houver imagem). */
    protected static ImageView imagemDeDetalhe() {
        ImageView foto = new ImageView();
        foto.setFitWidth(LARGURA_DETALHE);
        foto.setFitHeight(180);
        foto.setPreserveRatio(true);
        foto.setVisible(false);
        foto.setManaged(false);
        return foto;
    }

    protected static Label textoDeDetalhe(String inicial) {
        Label texto = new Label(inicial);
        texto.setWrapText(true);
        return texto;
    }

    protected static VBox colunaDetalhe(Node... conteudo) {
        VBox coluna = new VBox(10, conteudo);
        coluna.setPrefWidth(LARGURA_DETALHE);
        coluna.setMinWidth(LARGURA_DETALHE);
        return coluna;
    }

    // ------------------------------------------------------------------ abas comuns

    private Tab criarAbaAvaliacoes() {
        Ui.celulas(listaAvaliacoes, a -> {
            String sobre = a.getServico() == null ? "" : " sobre \"" + a.getServico().getNome() + "\"";
            String comentario = a.getComentario().isBlank() ? "" : "\n    \"" + a.getComentario() + "\"";
            return Formato.estrelas(a.getNota()) + "  " + a.getAutor().getNome() + sobre
                    + "  (" + Formato.dataHora(a.getData()) + ")" + comentario;
        });
        mediaRecebida.setStyle("-fx-font-weight: bold;");
        return criarAba("Avaliações", caixa(listaAvaliacoes, mediaRecebida));
    }

    private Tab criarAbaNotificacoes() {
        Ui.celulas(listaNotificacoes, n -> (n.isLida() ? "     " : "●  ")
                + Formato.dataHora(n.getData()) + "   " + n.getMensagem());
        Button marcar = new Button("Marcar todas como lidas");
        marcar.setOnAction(e -> executar(() -> sistema.marcarNotificacoesComoLidas(usuario)));
        abaNotificacoes.setContent(caixa(listaNotificacoes, marcar));
        abaNotificacoes.setClosable(false);
        return abaNotificacoes;
    }
}
