package com.trocabem.service;

import com.trocabem.exception.TrocaBemException;
import com.trocabem.model.Candidatura;
import com.trocabem.model.CategoriaAcao;
import com.trocabem.model.Dificuldade;
import com.trocabem.model.Instituicao;
import com.trocabem.model.Parceiro;
import com.trocabem.model.TipoServico;
import com.trocabem.model.Vaga;
import com.trocabem.model.Voluntario;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Dados pré-carregados para demonstração (todas as contas usam a senha 1234).
 * As categorias ficam aqui por enquanto; depois podem vir de um categorias.txt.
 */
public final class DadosExemplo {

    private static final List<CategoriaAcao> CATEGORIAS = Collections.unmodifiableList(Arrays.asList(
            new CategoriaAcao("Apoio administrativo", 10),
            new CategoriaAcao("Educação e reforço", 15),
            new CategoriaAcao("Cuidado e saúde", 18),
            new CategoriaAcao("Manutenção e obras", 20),
            new CategoriaAcao("Alimentação", 14),
            new CategoriaAcao("Eventos e campanhas", 12),
            new CategoriaAcao("Habilidade especializada", 25)));

    private DadosExemplo() {
    }

    public static List<CategoriaAcao> categorias() {
        return CATEGORIAS;
    }

    public static void carregar(TrocaBemService sistema) {
        try {
            Voluntario ana = sistema.cadastrar(
                    new Voluntario("Ana Souza", "ana@email.com", "1234", "111.222.333-44"), "1234");
            sistema.cadastrar(new Voluntario("Bruno Lima", "bruno@email.com", "1234", "555.666.777-88"), "1234");

            Instituicao ong = sistema.cadastrar(new Instituicao("ONG Estudar Mais", "ong@email.com", "1234",
                    "12.345.678/0001-90", "Reforço escolar para crianças"), "1234");
            Instituicao abrigo = sistema.cadastrar(new Instituicao("Abrigo Casa Amiga", "abrigo@email.com", "1234",
                    "22.333.444/0001-55", "Acolhimento de famílias"), "1234");

            Parceiro lanchonete = sistema.cadastrar(new Parceiro("Lanchonete Sabor", "sabor@email.com", "1234",
                    "98.765.432/0001-10", "Lanchonete"), "1234");
            Parceiro spa = sistema.cadastrar(new Parceiro("Spa Relax", "spa@email.com", "1234",
                    "11.222.333/0001-44", "Spa e salão"), "1234");
            Parceiro cine = sistema.cadastrar(new Parceiro("Cine Centro", "cine@email.com", "1234",
                    "33.444.555/0001-66", "Cinema"), "1234");

            Vaga reforco = sistema.publicarVaga(ong, "Reforço de matemática", "Ajudar crianças do 5º ano",
                    4, 3, CATEGORIAS.get(1), Dificuldade.MODERADO);
            sistema.publicarVaga(abrigo, "Distribuição de alimentos", "Separar e entregar cestas básicas",
                    3, 5, CATEGORIAS.get(4), Dificuldade.LEVE);
            sistema.publicarVaga(abrigo, "Reforma do refeitório", "Pintura e pequenos reparos",
                    6, 3, CATEGORIAS.get(3), Dificuldade.PESADO);

            sistema.cadastrarServico(ServicoFactory.criar(TipoServico.REFEICAO, "Combo lanche",
                    "Sanduíche + refrigerante", 50, lanchonete));
            sistema.cadastrarServico(ServicoFactory.criar(TipoServico.REFEICAO, "Sorvete grande",
                    "Sorvete de 2 bolas", 20, lanchonete));
            sistema.cadastrarServico(ServicoFactory.criar(TipoServico.BELEZA, "Corte de cabelo",
                    "Corte simples", 60, spa));
            sistema.cadastrarServico(ServicoFactory.criar(TipoServico.BEM_ESTAR, "Dia de spa",
                    "Day use completo", 300, spa));
            sistema.cadastrarServico(ServicoFactory.criar(TipoServico.BEM_ESTAR, "Ingresso de cinema",
                    "Sessão 2D em dias úteis", 45, cine));

            // A Ana já começa com um trabalho concluído (78 créditos) para dar para testar o resgate.
            Candidatura c = sistema.candidatar(ana, reforco);
            sistema.aprovarCandidatura(ong, c);
            sistema.confirmarHoras(ong, c, 4);
        } catch (TrocaBemException e) {
            throw new IllegalStateException("Falha ao carregar dados de exemplo: " + e.getMessage(), e);
        }
    }
}
