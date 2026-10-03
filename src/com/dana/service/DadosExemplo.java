package com.dana.service;

import com.dana.exception.DanaException;
import com.dana.model.Candidatura;
import com.dana.model.CategoriaAcao;
import com.dana.model.Dificuldade;
import com.dana.model.Instituicao;
import com.dana.model.Parceiro;
import com.dana.model.TipoServico;
import com.dana.model.Vaga;
import com.dana.model.Voluntario;
import com.dana.util.Valor;

import java.time.LocalDate;

/**
 * Dados de demonstração, carregados só na primeira execução (quando ainda não há nada salvo).
 * Todas as contas usam a senha 1234.
 */
final class DadosExemplo {

    static final String SENHA = "1234";

    private DadosExemplo() {
    }

    static void carregar(DanaService sistema) throws DanaException {
        LocalDate hoje = LocalDate.now();

        Voluntario ana = sistema.cadastrar(
                new Voluntario("Ana Souza", "ana@email.com", SENHA, "123.456.789-09"));
        sistema.cadastrar(new Voluntario("Bruno Lima", "bruno@email.com", SENHA, "987.654.321-00"));

        Instituicao ong = sistema.cadastrar(new Instituicao("ONG Estudar Mais", "ong@email.com", SENHA,
                "12.345.678/0001-95", "Reforço escolar para crianças"));
        Instituicao abrigo = sistema.cadastrar(new Instituicao("Abrigo Casa Amiga", "abrigo@email.com", SENHA,
                "22.333.444/0001-81", "Acolhimento de famílias"));

        Parceiro lanchonete = sistema.cadastrar(new Parceiro("Lanchonete Sabor", "sabor@email.com", SENHA,
                "98.765.432/0001-98", "Lanchonete"));
        Parceiro spa = sistema.cadastrar(new Parceiro("Spa Relax", "spa@email.com", SENHA,
                "11.222.333/0001-81", "Spa e salão"));
        Parceiro cine = sistema.cadastrar(new Parceiro("Cine Centro", "cine@email.com", SENHA,
                "33.444.555/0001-81", "Cinema"));

        Vaga reforco = sistema.publicarVaga(ong, "Reforço de matemática", "Ajudar crianças do 5º ano",
                4, 3, categoria(sistema, "Educação e reforço"), Dificuldade.MODERADO, hoje, null);
        sistema.publicarVaga(abrigo, "Distribuição de alimentos", "Separar e entregar cestas básicas",
                3, 5, categoria(sistema, "Alimentação"), Dificuldade.LEVE, hoje.plusDays(5), null);
        sistema.publicarVaga(abrigo, "Reforma do refeitório", "Pintura e pequenos reparos",
                6, 3, categoria(sistema, "Manutenção e obras"), Dificuldade.PESADO, hoje.plusDays(12), null);

        sistema.cadastrarServico(lanchonete, TipoServico.REFEICAO, "Combo lanche", "Sanduíche + refrigerante",
                Valor.de("50"), null);
        sistema.cadastrarServico(lanchonete, TipoServico.REFEICAO, "Sorvete grande", "Sorvete de 2 bolas",
                Valor.de("20"), null);
        sistema.cadastrarServico(spa, TipoServico.BELEZA, "Corte de cabelo", "Corte simples",
                Valor.de("60"), null);
        sistema.cadastrarServico(spa, TipoServico.BEM_ESTAR, "Dia de spa", "Day use completo",
                Valor.de("300"), null);
        sistema.cadastrarServico(cine, TipoServico.BEM_ESTAR, "Ingresso de cinema", "Sessão 2D em dias úteis",
                Valor.de("45"), null);

        // A Ana já começa com um trabalho concluído (78 créditos) para dar para testar o resgate.
        Candidatura c = sistema.candidatar(ana, reforco);
        sistema.aprovarCandidatura(ong, c);
        sistema.confirmarHoras(ong, c, 4);
    }

    /** Procura pelo nome; se o categorias.txt foi editado e ela não existe mais, usa a primeira. */
    private static CategoriaAcao categoria(DanaService sistema, String nome) {
        return sistema.listarCategorias().stream()
                .filter(c -> c.getNome().equalsIgnoreCase(nome))
                .findFirst()
                .orElse(sistema.listarCategorias().get(0));
    }
}
