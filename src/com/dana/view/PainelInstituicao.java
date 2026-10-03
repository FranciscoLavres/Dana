package com.dana.view;

import com.dana.model.Candidatura;
import com.dana.model.CategoriaAcao;
import com.dana.model.Dificuldade;
import com.dana.model.Instituicao;
import com.dana.model.StatusCandidatura;
import com.dana.model.StatusVaga;
import com.dana.model.Vaga;
import com.dana.service.DanaService;
import com.dana.util.Formato;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.time.LocalDate;

/** Painel da instituição: publicar vagas (com foto opcional), ver candidatos, confirmar horas e avaliar. */
public class PainelInstituicao extends PainelBase {

    private final Instituicao instituicao;
    private final ListView<Vaga> listaVagas = new ListView<>();
    private final ListView<Candidatura> listaCandidaturas = new ListView<>();

    public PainelInstituicao(App app, DanaService sistema, Instituicao instituicao) {
        super(app, sistema, instituicao, "Painel da Instituição");
        this.instituicao = instituicao;
        setCenter(montarAbas(criarAbaPublicar(), criarAbaVagas(), criarAbaCandidaturas()));
        atualizar();
    }

    @Override
    protected void atualizarConteudo() {
        listaVagas.getItems().setAll(sistema.listarVagasDaInstituicao(instituicao));
        listaCandidaturas.getItems().setAll(sistema.listarCandidaturasDaInstituicao(instituicao));
    }

    // ------------------------------------------------------------------ abas

    private Tab criarAbaPublicar() {
        TextField titulo = new TextField();
        titulo.setPromptText("Ex.: Reforço de matemática");
        titulo.setPrefWidth(420);
        TextArea descricao = new TextArea();
        descricao.setPrefRowCount(3);
        descricao.setWrapText(true);
        TextField horas = new TextField("4");
        TextField numeroVagas = new TextField("1");
        DatePicker data = new DatePicker(LocalDate.now().plusDays(7));
        ChoiceBox<CategoriaAcao> categoria = new ChoiceBox<>(
                FXCollections.observableArrayList(sistema.listarCategorias()));
        ChoiceBox<Dificuldade> dificuldade = new ChoiceBox<>(
                FXCollections.observableArrayList(Dificuldade.values()));
        categoria.getSelectionModel().selectFirst();
        dificuldade.getSelectionModel().selectFirst();
        SeletorImagem imagem = new SeletorImagem("Sem foto do trabalho (opcional)");

        // "simulador": mostra o valor por hora assim que a categoria ou a dificuldade mudam
        Label estimativa = new Label();
        Runnable recalcular = () -> {
            if (categoria.getValue() != null && dificuldade.getValue() != null) {
                estimativa.setText("Valor por hora desta vaga: " + Formato.creditos(sistema.getCalculadora()
                        .calcularValorHoraVaga(categoria.getValue(), dificuldade.getValue()))
                        + " créditos (antes do bônus de nível do voluntário)");
            }
        };
        categoria.valueProperty().addListener((obs, antiga, nova) -> recalcular.run());
        dificuldade.valueProperty().addListener((obs, antiga, nova) -> recalcular.run());
        recalcular.run();

        Button publicar = new Button("Publicar vaga");
        publicar.setOnAction(e -> {
            double h;
            int n;
            try {
                h = Ui.lerNumero(horas.getText());
                n = Integer.parseInt(numeroVagas.getText().trim());
            } catch (NumberFormatException ex) {
                Ui.erro("Digite números válidos em horas e número de vagas.");
                return;
            }
            executar(() -> {
                sistema.publicarVaga(instituicao, titulo.getText(), descricao.getText(), h, n,
                        categoria.getValue(), dificuldade.getValue(), data.getValue(), imagem.getCaminho());
                Ui.info("Vaga publicada com sucesso!");
                titulo.clear();
                descricao.clear();
                imagem.limpar();
            });
        });

        GridPane grade = new GridPane();
        grade.setHgap(10);
        grade.setVgap(10);
        grade.setPadding(new Insets(15));
        grade.add(new Label("Título"), 0, 0);
        grade.add(titulo, 1, 0);
        grade.add(new Label("Descrição"), 0, 1);
        grade.add(descricao, 1, 1);
        grade.add(new Label("Data da ação"), 0, 2);
        grade.add(data, 1, 2);
        grade.add(new Label("Horas previstas"), 0, 3);
        grade.add(horas, 1, 3);
        grade.add(new Label("Nº de vagas"), 0, 4);
        grade.add(numeroVagas, 1, 4);
        grade.add(new Label("Categoria"), 0, 5);
        grade.add(categoria, 1, 5);
        grade.add(new Label("Dificuldade"), 0, 6);
        grade.add(dificuldade, 1, 6);
        grade.add(new Label("Foto do trabalho"), 0, 7);
        grade.add(imagem, 1, 7);
        grade.add(estimativa, 1, 8);
        grade.add(publicar, 1, 9);

        ScrollPane rolagem = new ScrollPane(grade);
        rolagem.setFitToWidth(true);
        return criarAba("Publicar vaga", rolagem);
    }

    private Tab criarAbaVagas() {
        Ui.celulasComImagem(listaVagas, v -> v.getTitulo() + (v.getStatus() == StatusVaga.ENCERRADA ? "  [ENCERRADA]" : "")
                        + "\n" + Formato.data(v.getDataAcao()) + "  ·  " + v.getCategoria().getNome() + "  ·  "
                        + Formato.creditos(v.getValorPorHoraCongelado()) + " cred/h  ·  "
                        + v.getVagasDisponiveis() + " de " + v.getTotalVagas() + " vaga(s) livre(s)",
                Vaga::getImagem, sistema);

        ImageView foto = imagemDeDetalhe();
        Label detalhe = textoDeDetalhe("Selecione uma vaga.");
        listaVagas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, v) -> {
            Ui.mostrarImagem(foto, sistema, v == null ? null : v.getImagem());
            detalhe.setText(v == null ? "Selecione uma vaga."
                    : v.getTitulo() + "\n\n" + v.getDescricao() + "\n\nStatus: " + v.getStatus()
                    + "\nDificuldade: " + v.getDificuldade()
                    + "\nHoras previstas: " + Formato.horas(v.getHorasPrevistas())
                    + (v.temImagem() ? "" : "\n\nEsta vaga ainda não tem foto."));
        });

        Button alterarFoto = new Button("Alterar foto…");
        alterarFoto.setOnAction(e -> {
            Vaga v = selecionado(listaVagas, "Selecione uma vaga primeiro.");
            if (v == null) {
                return;
            }
            Path arquivo = Ui.escolherImagem(janela());
            if (arquivo != null) {
                executar(() -> sistema.definirImagem(instituicao, v, arquivo));
            }
        });

        Button removerFoto = new Button("Remover foto");
        removerFoto.setOnAction(e -> {
            Vaga v = selecionado(listaVagas, "Selecione uma vaga primeiro.");
            if (v != null && v.temImagem() && Ui.confirmar("Remover a foto de \"" + v.getTitulo() + "\"?")) {
                executar(() -> sistema.definirImagem(instituicao, v, null));
            }
        });

        Button encerrar = new Button("Encerrar vaga");
        encerrar.setOnAction(e -> {
            Vaga v = selecionado(listaVagas, "Selecione uma vaga primeiro.");
            if (v != null && Ui.confirmar("Encerrar \"" + v.getTitulo()
                    + "\"? Ela deixa de receber candidaturas (as atuais continuam valendo).")) {
                executar(() -> sistema.encerrarVaga(instituicao, v));
            }
        });

        Button relatorio = new Button("Exportar relatório (.txt)");
        relatorio.setOnAction(e -> executar(() ->
                Ui.salvarRelatorio(janela(), sistema.relatorioInstituicao(instituicao))));

        return criarAba("Minhas vagas", caixa(listaComDetalhe(listaVagas, colunaDetalhe(foto, detalhe)),
                new HBox(10, alterarFoto, removerFoto, encerrar, relatorio)));
    }

    private Tab criarAbaCandidaturas() {
        Ui.celulas(listaCandidaturas, c -> c.getVoluntario().getNome() + " (" + c.getVoluntario().getNivel() + ", "
                + Formato.media(sistema.mediaAvaliacoes(c.getVoluntario())) + ")  →  " + c.getVaga().getTitulo()
                + "  ·  " + c.getStatus() + (c.getAvaliacaoDaInstituicao() != null ? "  ·  avaliado" : ""));

        Button aprovar = new Button("Aprovar");
        aprovar.setOnAction(e -> {
            Candidatura c = selecionado(listaCandidaturas, "Selecione uma candidatura primeiro.");
            if (c != null) {
                executar(() -> sistema.aprovarCandidatura(instituicao, c));
            }
        });

        Button recusar = new Button("Recusar");
        recusar.setOnAction(e -> {
            Candidatura c = selecionado(listaCandidaturas, "Selecione uma candidatura primeiro.");
            if (c != null) {
                executar(() -> sistema.recusarCandidatura(instituicao, c));
            }
        });

        Button confirmar = new Button("Confirmar horas e creditar");
        confirmar.setOnAction(e -> confirmarHoras());

        Button avaliar = new Button("Avaliar voluntário");
        avaliar.setOnAction(e -> {
            Candidatura c = selecionado(listaCandidaturas, "Selecione uma candidatura concluída.");
            if (c == null) {
                return;
            }
            if (c.getStatus() != StatusCandidatura.CONCLUIDA) {
                Ui.aviso("Confirme as horas antes de avaliar o voluntário.");
                return;
            }
            new DialogoAvaliacao("Como foi a participação de " + c.getVoluntario().getNome() + "?")
                    .showAndWait()
                    .ifPresent(r -> executar(() -> {
                        sistema.avaliarVoluntario(instituicao, c, r.nota(), r.comentario());
                        Ui.info("Avaliação registrada!");
                    }));
        });

        HBox botoes = new HBox(10, aprovar, recusar, confirmar, avaliar);
        return criarAba("Candidaturas", caixa(listaCandidaturas, botoes));
    }

    // ------------------------------------------------------------------ ações

    private void confirmarHoras() {
        Candidatura c = selecionado(listaCandidaturas, "Selecione uma candidatura primeiro.");
        if (c == null) {
            return;
        }
        double maximo = sistema.getCalculadora().horasMaximas(c.getVaga());
        TextInputDialog dialogo = new TextInputDialog(Formato.horas(c.getVaga().getHorasPrevistas()));
        dialogo.setTitle("Confirmar horas");
        dialogo.setHeaderText("Quantas horas " + c.getVoluntario().getNome() + " trabalhou?\n"
                + "Previstas: " + Formato.horas(c.getVaga().getHorasPrevistas())
                + " h  ·  máximo permitido: " + Formato.horas(maximo) + " h");
        dialogo.setContentText("Horas:");
        dialogo.showAndWait().ifPresent(resposta -> {
            double horas;
            try {
                horas = Ui.lerNumero(resposta);
            } catch (NumberFormatException ex) {
                Ui.erro("Digite um número válido de horas.");
                return;
            }
            executar(() -> {
                var creditos = sistema.confirmarHoras(instituicao, c, horas);
                Ui.info("Horas confirmadas! " + Formato.creditos(creditos) + " créditos foram para a carteira de "
                        + c.getVoluntario().getNome() + ".\n\n" + c.getDetalheCalculo());
            });
        });
    }
}
