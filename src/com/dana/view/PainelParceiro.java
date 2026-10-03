package com.dana.view;

import com.dana.exception.DanaException;
import com.dana.model.Parceiro;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.TipoServico;
import com.dana.service.DanaService;
import com.dana.util.Formato;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.nio.file.Path;

/** Painel do parceiro: validar vouchers, ver a agenda, cadastrar serviços (com foto) e gerenciar o catálogo. */
public class PainelParceiro extends PainelBase {

    private final Parceiro parceiro;
    private final ListView<Servico> listaServicos = new ListView<>();
    private final ListView<Resgate> listaAgenda = new ListView<>();

    public PainelParceiro(App app, DanaService sistema, Parceiro parceiro) {
        super(app, sistema, parceiro, "Painel do Parceiro");
        this.parceiro = parceiro;
        setCenter(montarAbas(criarAbaValidar(), criarAbaAgenda(), criarAbaCadastrar(), criarAbaServicos()));
        atualizar();
    }

    @Override
    protected void atualizarConteudo() {
        listaServicos.getItems().setAll(sistema.listarServicosDoParceiro(parceiro));
        listaAgenda.getItems().setAll(sistema.listarAgendaDoParceiro(parceiro));
    }

    // ------------------------------------------------------------------ abas

    private Tab criarAbaValidar() {
        TextField codigo = new TextField();
        codigo.setPromptText("Código do voucher (ex.: 5DBB38AB)");
        codigo.setPrefWidth(300);
        codigo.setMaxWidth(300);
        Label resultado = new Label();
        resultado.setWrapText(true);

        Button validar = new Button("Validar e dar baixa");
        validar.setDefaultButton(true);
        validar.setOnAction(e -> {
            if (codigo.getText().isBlank()) {
                Ui.aviso("Digite o código do voucher.");
                return;
            }
            try {
                Resgate resgate = sistema.validarVoucher(parceiro, codigo.getText());
                resultado.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                resultado.setText("Voucher válido! Entregar: " + resgate.getServico().getNome()
                        + " para " + resgate.getVoluntario().getNome() + ".");
                codigo.clear();
            } catch (DanaException ex) {
                resultado.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                resultado.setText(ex.getMessage());
            }
            atualizar();
        });

        VBox caixa = new VBox(12, Ui.subtitulo("Validação de voucher"), codigo, validar, resultado);
        caixa.setPadding(new Insets(15));
        return criarAba("Validar voucher", caixa);
    }

    private Tab criarAbaAgenda() {
        Ui.celulas(listaAgenda, r -> Formato.dataHora(r.getAgendadoPara()) + "   " + r.getVoluntario().getNome()
                + "  ·  " + r.getServico().getNome() + "  ·  voucher " + r.getCodigoVoucher());
        listaAgenda.setPlaceholder(new Label("Nenhum agendamento pendente."));
        Label dica = new Label("Os voluntários agendam o uso dos vouchers. Na hora do atendimento, "
                + "valide o código na aba \"Validar voucher\".");
        dica.setWrapText(true);
        return criarAba("Agenda", caixa(listaAgenda, dica));
    }

    private Tab criarAbaCadastrar() {
        TextField nome = new TextField();
        nome.setPrefWidth(350);
        TextField descricao = new TextField();
        TextField custo = new TextField();
        custo.setPromptText("Custo em créditos");
        ChoiceBox<TipoServico> tipo = new ChoiceBox<>(FXCollections.observableArrayList(TipoServico.values()));
        tipo.getSelectionModel().selectFirst();
        SeletorImagem imagem = new SeletorImagem("Sem foto do produto (opcional)");

        Button cadastrar = new Button("Cadastrar serviço");
        cadastrar.setOnAction(e -> {
            BigDecimal valor;
            try {
                valor = Ui.lerValor(custo.getText());
            } catch (NumberFormatException ex) {
                Ui.erro("Digite um número válido no custo.");
                return;
            }
            executar(() -> {
                sistema.cadastrarServico(parceiro, tipo.getValue(), nome.getText(), descricao.getText(), valor,
                        imagem.getCaminho());
                Ui.info("Serviço cadastrado!");
                nome.clear();
                descricao.clear();
                custo.clear();
                imagem.limpar();
            });
        });

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Tipo"), 0, 0);
        grade.add(tipo, 1, 0);
        grade.add(new Label("Nome"), 0, 1);
        grade.add(nome, 1, 1);
        grade.add(new Label("Descrição"), 0, 2);
        grade.add(descricao, 1, 2);
        grade.add(new Label("Custo (créditos)"), 0, 3);
        grade.add(custo, 1, 3);
        grade.add(new Label("Foto do produto"), 0, 4);
        grade.add(imagem, 1, 4);
        grade.add(cadastrar, 1, 5);

        ScrollPane rolagem = new ScrollPane(grade);
        rolagem.setFitToWidth(true);
        return criarAba("Cadastrar serviço", rolagem);
    }

    private Tab criarAbaServicos() {
        Ui.celulasComImagem(listaServicos, s -> s.getNome() + (s.isAtivo() ? "" : "  [INATIVO]") + "\n"
                        + s.getTipoServico() + "  ·  " + Formato.creditos(s.getCustoBase()) + " créditos  ·  "
                        + Formato.media(sistema.mediaAvaliacoes(s)),
                Servico::getImagem, sistema);

        ImageView foto = imagemDeDetalhe();
        Label detalhe = textoDeDetalhe("Selecione um serviço.");
        listaServicos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, s) -> {
            Ui.mostrarImagem(foto, sistema, s == null ? null : s.getImagem());
            detalhe.setText(s == null ? "Selecione um serviço."
                    : s.getNome() + "\n\n" + s.getDescricao() + "\n\nSituação: " + (s.isAtivo() ? "ativo" : "inativo")
                    + (s.temImagem() ? "" : "\n\nEste serviço ainda não tem foto."));
        });

        Button alterarFoto = new Button("Alterar foto…");
        alterarFoto.setOnAction(e -> {
            Servico s = selecionado(listaServicos, "Selecione um serviço primeiro.");
            if (s == null) {
                return;
            }
            Path arquivo = Ui.escolherImagem(janela());
            if (arquivo != null) {
                executar(() -> sistema.definirImagem(parceiro, s, arquivo));
            }
        });

        Button removerFoto = new Button("Remover foto");
        removerFoto.setOnAction(e -> {
            Servico s = selecionado(listaServicos, "Selecione um serviço primeiro.");
            if (s != null && s.temImagem() && Ui.confirmar("Remover a foto de \"" + s.getNome() + "\"?")) {
                executar(() -> sistema.definirImagem(parceiro, s, null));
            }
        });

        Button disponibilidade = new Button("Ativar / desativar");
        disponibilidade.setOnAction(e -> {
            Servico s = selecionado(listaServicos, "Selecione um serviço primeiro.");
            if (s != null) {
                executar(() -> sistema.alterarDisponibilidade(parceiro, s, !s.isAtivo()));
            }
        });

        Button relatorio = new Button("Exportar relatório (.txt)");
        relatorio.setOnAction(e -> executar(() -> Ui.salvarRelatorio(janela(), sistema.relatorioParceiro(parceiro))));

        return criarAba("Meus serviços", caixa(listaComDetalhe(listaServicos, colunaDetalhe(foto, detalhe)),
                new HBox(10, alterarFoto, removerFoto, disponibilidade, relatorio)));
    }
}
