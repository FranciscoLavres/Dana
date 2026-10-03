# TrocaBem

Plataforma de voluntariado em que a pessoa faz ações voluntárias em instituições, ganha **créditos** e troca por **serviços** de parceiros (lanche, ingresso de cinema, dia de spa...). Projeto de faculdade em Java, com POO e interface JavaFX.

## Como rodar

Requisitos: Java 17+ e Maven.

```
mvn javafx:run
```

Na IDE (IntelliJ/Eclipse): abra como projeto Maven e execute `com.dana.view.App`.
Se aparecer "JavaFX runtime components are missing", execute `com.dana.view.Launcher`.

Versão só de console (demonstração do fluxo, sem tela): `com.dana.Main`.

## Contas de teste (senha `1234`)

| Perfil | E-mail |
|---|---|
| Voluntário | ana@email.com (já com 78 créditos), bruno@email.com |
| Instituição | ong@email.com, abrigo@email.com |
| Parceiro | sabor@email.com, spa@email.com, cine@email.com |

## Fluxo para testar

1. Ana resgata um serviço no catálogo e recebe um código de voucher.
2. O parceiro dono do serviço valida o código na aba "Validar voucher".
3. Bruno se candidata a uma vaga.
4. A instituição da vaga aprova a candidatura e confirma as horas; os créditos caem na carteira do Bruno.

## Funcionalidades

1. Cadastro e login (senha guardada com hash SHA-256)
2. Publicação de vagas (valor por hora calculado pelo sistema)
3. Candidatura e aprovação
4. Confirmação de horas e crédito na carteira
5. Catálogo e resgate com voucher
6. Validação de voucher pelo parceiro
7. Extrato e histórico

Fórmula: `créditos = horas x base(categoria) x mult(dificuldade) x bônus(nível)`, com teto de 40 créditos por hora.

## Estrutura

```
src/com/trocabem
  model/      entidades, enums e interfaces (Usuario, Voluntario, Vaga, Servico, Carteira...)
  service/    regras de negócio (TrocaBemService, CalculadoraCreditos, ServicoFactory, DadosExemplo)
  exception/  exceções próprias (todas herdam de TrocaBemException)
  util/       SenhaUtil (hash)
  view/       telas JavaFX (App, TelaLogin, PainelVoluntario, PainelInstituicao, PainelParceiro)
  Main.java   demo em console
```

## POO no projeto

- **Herança/abstração:** `Usuario` (abstrata) e seus filhos; `Servico` e filhos; `Transacao` com `Credito`/`Debito`; `PainelBase` e os painéis.
- **Polimorfismo:** `calcularCusto()` e `descreverResgate()` variam por tipo de serviço.
- **Encapsulamento:** o saldo da `Carteira` só muda por `creditar()` e `debitar()`.
- **Interface:** `Autenticavel`.
- **Enums com comportamento:** `Dificuldade`, `NivelVoluntario`.
- **Padrão Factory:** `ServicoFactory`.

## Limitações atuais

- **Sem persistência:** os dados ficam só em memória. Ao fechar o programa, tudo volta ao estado inicial (contas criadas somem; os dados de exemplo são recarregados).
- Ainda não há avaliações, notificações, agendamento nem geração de arquivos txt.