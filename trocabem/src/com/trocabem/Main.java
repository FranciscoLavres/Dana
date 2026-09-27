package com.trocabem;

import com.trocabem.exception.TrocaBemException;
import com.trocabem.model.CategoriaAcao;
import com.trocabem.model.Candidatura;
import com.trocabem.model.Dificuldade;
import com.trocabem.model.Instituicao;
import com.trocabem.model.Parceiro;
import com.trocabem.model.Resgate;
import com.trocabem.model.Servico;
import com.trocabem.model.ServicoBemEstar;
import com.trocabem.model.ServicoBeleza;
import com.trocabem.model.ServicoRefeicao;
import com.trocabem.model.Transacao;
import com.trocabem.model.Usuario;
import com.trocabem.model.Vaga;
import com.trocabem.model.Voluntario;
import com.trocabem.service.TrocaBemService;

/**
 * Demonstração do fluxo completo no console (ainda sem tela).
 * Serve para testar as regras antes de ligar JavaFX ou Spring.
 */
public class Main {

    public static void main(String[] args) {
        TrocaBemService sistema = new TrocaBemService();

        try {
            // ---- 1. Cadastro e login
            titulo("1. Cadastro e login");
            Voluntario ana = sistema.cadastrar(
                    new Voluntario("Ana Souza", "ana@email.com", "1234", "111.222.333-44"), "1234");
            Instituicao ong = sistema.cadastrar(
                    new Instituicao("ONG Estudar Mais", "ong@email.com", "1234", "12.345.678/0001-90",
                            "Reforço escolar para crianças"), "1234");
            Parceiro lanchonete = sistema.cadastrar(
                    new Parceiro("Lanchonete Sabor", "sabor@email.com", "1234", "98.765.432/0001-10",
                            "Lanchonete"), "1234");
            Parceiro spa = sistema.cadastrar(
                    new Parceiro("Spa Relax", "spa@email.com", "1234", "11.222.333/0001-44", "Spa"), "1234");

            Usuario logado = sistema.login("ana@email.com", "1234");
            System.out.println("Login OK: " + logado);

            // ---- 2. Vaga (a instituição escolhe parâmetros; o sistema calcula o valor)
            titulo("2. Gestão de vagas");
            CategoriaAcao educacao = new CategoriaAcao("Educação e reforço", 15);
            Vaga vaga = sistema.publicarVaga(ong, "Reforço de matemática", "Ajudar crianças do 5º ano",
                    4, 2, educacao, Dificuldade.MODERADO);
            System.out.println(vaga);

            // ---- 3. Candidatura e aprovação
            titulo("3. Candidatura e aprovação");
            Candidatura candidatura = sistema.candidatar(ana, vaga);
            System.out.println(candidatura);
            sistema.aprovarCandidatura(ong, candidatura);
            System.out.println(candidatura);

            // ---- 4. Horas e créditos
            titulo("4. Horas e geração de créditos");
            double creditos = sistema.confirmarHoras(ong, candidatura, 4);
            System.out.println("Créditos gerados: " + creditos);
            System.out.println(candidatura.getDetalheCalculo());
            System.out.println("Saldo da Ana: " + ana.getCarteira().getSaldo() + " | nível: " + ana.getNivel());

            // ---- 5. Catálogo e resgate (lista de Servico com filhos diferentes)
            titulo("5. Catálogo e resgate");
            sistema.cadastrarServico(new ServicoRefeicao("Combo lanche", "Sanduíche + refrigerante", 50, lanchonete));
            sistema.cadastrarServico(new ServicoBeleza("Corte de cabelo", "Corte simples", 60, spa));
            Servico dia = sistema.cadastrarServico(
                    new ServicoBemEstar("Dia de spa", "Day use completo", 300, spa));
            for (Servico s : sistema.listarServicosAtivos()) {
                System.out.println(s + " -> custo p/ Ana: " + s.calcularCusto(ana.getNivel()));
            }

            Servico lanche = sistema.listarServicosAtivos().get(0);
            Resgate voucher = sistema.resgatar(ana, lanche);
            System.out.println("Resgatado: " + voucher);
            System.out.println(lanche.descreverResgate());

            // ---- Erro esperado: saldo insuficiente
            try {
                sistema.resgatar(ana, dia);
            } catch (TrocaBemException e) {
                System.out.println("Erro esperado -> " + e.getMessage());
            }

            // ---- 6. Validação de voucher
            titulo("6. Validação de voucher");
            sistema.validarVoucher(lanchonete, voucher.getCodigoVoucher());
            System.out.println("Voucher validado: " + voucher);
            try {
                sistema.validarVoucher(lanchonete, voucher.getCodigoVoucher());
            } catch (TrocaBemException e) {
                System.out.println("Erro esperado -> " + e.getMessage());
            }
            try {
                sistema.validarVoucher(spa, "INEXIST");
            } catch (TrocaBemException e) {
                System.out.println("Erro esperado -> " + e.getMessage());
            }

            // ---- Outras regras: permissão e vaga lotada
            titulo("Regras extras");
            try {
                Instituicao outra = new Instituicao("Outra ONG", "outra@email.com", "1234", "00", "x");
                sistema.aprovarCandidatura(outra, candidatura);
            } catch (TrocaBemException e) {
                System.out.println("Erro esperado -> " + e.getMessage());
            }

            // ---- 8. Extrato
            titulo("8. Extrato da Ana");
            for (Transacao t : sistema.extrato(ana)) {
                System.out.println(t);
            }
            System.out.println("Saldo final: " + ana.getCarteira().getSaldo());

        } catch (TrocaBemException e) {
            System.out.println("ERRO INESPERADO: " + e.getMessage());
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
