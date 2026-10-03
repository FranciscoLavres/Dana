# Dāna

Plataforma de voluntariado: a pessoa faz ações voluntárias em instituições, ganha **créditos** e troca por **serviços** de parceiros (lanche, ingresso de cinema, dia de spa...). Projeto de faculdade em Java, feito só com POO e interface JavaFX (sem outras bibliotecas).

## Como rodar

Requisitos: Java 17+ e Maven.

```
mvn javafx:run
```

(rodar na raiz do repositório, onde está o `pom.xml`)

**Na IDE (IntelliJ/Eclipse):** abra como projeto Maven e execute a classe **`com.dana.view.App`**.
O projeto tem um `module-info.java`, então a IDE já coloca o JavaFX no module path e o `App` roda direto (não existe mais a classe `Launcher`).
Se a IDE ainda mostrar a configuração antiga, recarregue o projeto Maven (*Reload All Maven Projects*).

**Verificação das regras** (testes sem biblioteca externa): execute `com.dana.teste.VerificacaoRegras`. Ela mostra `OK`/`FALHA` para cada regra e termina com código 1 se algo falhar.

## Onde ficam os dados

Tudo é salvo automaticamente a cada operação, numa pasta do usuário:

| Sistema | Pasta |
|---|---|
| Windows | `C:\Users\<você>\.dana\` |
| Linux / macOS | `~/.dana/` |

Para usar outra pasta: `-Ddana.dados=caminho` (ex.: `mvn javafx:run -Ddana.dados=dados`).

```
.dana/
  dana.dat        todos os dados (usuários, vagas, carteiras, vouchers...) - serialização do Java
  categorias.txt  categorias de ação e valor-base por hora (texto, pode editar)
  imagens/        fotos das vagas e dos serviços
```

- Na **primeira execução** (pasta vazia) o sistema cria os dados de exemplo abaixo e o `categorias.txt` padrão.
- **Para recomeçar do zero**, apague a pasta `.dana`.
- Se o `dana.dat` não puder ser lido (corrompido ou de uma versão antiga), o sistema oferece guardar uma cópia `dana-AAAAMMDD-HHMMSS.bak` e recomeçar.
- `categorias.txt`: uma categoria por linha no formato `Nome;valor` (aceita vírgula decimal). Linhas com `#` são comentários. Mudanças valem para as vagas publicadas depois (as antigas têm o valor congelado).

## Contas de teste (senha `1234`)

Existem só quando o sistema começou com os dados de exemplo. Na tela de login, clique em **"Mostrar contas de teste"**.

| Perfil | E-mail |
|---|---|
| Voluntário | ana@email.com (já com 78 créditos), bruno@email.com |
| Instituição | ong@email.com, abrigo@email.com |
| Parceiro | sabor@email.com, spa@email.com, cine@email.com |

## Fluxo para testar

1. Ana resgata um serviço no catálogo e recebe um código de voucher.
2. Em "Meus vouchers", Ana agenda o dia e o horário de uso; o parceiro vê o agendamento na aba "Agenda".
3. O parceiro dono do serviço valida o código na aba "Validar voucher". Ana é notificada e pode avaliar o serviço.
4. Bruno se candidata a uma vaga (a instituição é notificada).
5. A instituição aprova a candidatura e confirma as horas; os créditos caem na carteira do Bruno.
6. Bruno avalia a instituição e gera o comprovante de horas em `.txt`; a instituição avalia o Bruno.

## Funcionalidades

1. **Cadastro e login.** Senha guardada com PBKDF2 + salt; CPF/CNPJ com dígito verificador; e-mail e documento únicos.
2. **Publicação de vagas.** Data da ação, valor por hora calculado pelo sistema e **foto do trabalho opcional**. Dá para trocar ou remover a foto e encerrar a vaga depois.
3. **Candidatura e aprovação.**
4. **Confirmação de horas e crédito na carteira.** Limite de 1,5× as horas previstas.
5. **Catálogo de serviços** com **foto do produto opcional** e avaliação média. O parceiro pode ativar/desativar serviços e trocar a foto.
6. **Resgate com voucher.** Código único com validade de 30 dias. Pode ser **cancelado com estorno** dos créditos.
7. **Agendamento** do uso do voucher (dentro da validade) e **agenda** do parceiro.
8. **Validação de voucher** pelo parceiro.
9. **Avaliações de 1 a 5 estrelas** com comentário:
   - o voluntário avalia a instituição (depois das horas confirmadas);
   - a instituição avalia o voluntário;
   - o voluntário avalia o serviço (depois de usar o voucher).

   Cada perfil vê as avaliações recebidas e a média na aba "Avaliações".
10. **Notificações** para cada evento importante: nova candidatura, aprovação/recusa, horas confirmadas, subida de nível, resgate, agendamento, cancelamento, voucher utilizado e avaliação recebida. O número de não lidas aparece na aba.
11. **Extrato** com o saldo após cada movimentação.
12. **Arquivos .txt:** extrato do voluntário, comprovante de horas, voucher, relatório da instituição (vagas e candidaturas) e relatório do parceiro (serviços e resgates).

Fórmula: `créditos = horas x min(base(categoria) x mult(dificuldade) x bônus(nível), 40)`, ou seja, teto de 40 créditos por hora.

## Estrutura

```
src/
  module-info.java   módulo com.dana (requer javafx.controls)
  com/dana/
    model/        entidades, enums e interfaces (Entidade, Usuario, Voluntario, Vaga, Servico, Carteira,
                  Resgate, Avaliacao, Notificacao, ComImagem...)
    service/      regras de negócio (DanaService, CalculadoraCreditos, ServicoFactory, DadosExemplo)
    persistencia/ Repositorio (interface), RepositorioArquivo, RepositorioMemoria, BaseDados
    relatorio/    geração dos .txt (RelatorioTxt e subclasses)
    exception/    exceções próprias (todas herdam de DanaException)
    util/         SenhaUtil (hash), Documento (CPF/CNPJ), Valor (créditos), Formato (pt-BR)
    view/         telas JavaFX (App, TelaLogin, Logo, painéis, diálogos, SeletorImagem)
    teste/        VerificacaoRegras (testes das regras em Java puro)
```

## POO no projeto

- **Herança/abstração:**
  - `Entidade` (id, equals/hashCode) é a base de usuários, vagas, candidaturas etc.;
  - `Usuario` (abstrata) e seus filhos;
  - `Servico` e filhos;
  - `Transacao` com `Credito`/`Debito`;
  - `RelatorioTxt` e os relatórios;
  - `PainelBase` e os painéis.
- **Polimorfismo:**
  - `calcularCusto()` e `descreverResgate()` variam por tipo de serviço;
  - cada relatório escreve o próprio corpo;
  - o `DanaService` funciona com qualquer `Repositorio`.
- **Encapsulamento:**
  - o saldo da `Carteira` só muda por `creditar()` e `debitar()`;
  - os construtores validam os dados, então não existe usuário inválido;
  - o extrato é só leitura.
- **Interfaces:**
  - `Autenticavel`;
  - `ComImagem` (vaga e serviço podem ter foto);
  - `Repositorio` (arquivo ou memória).
- **Enums com comportamento:** `Dificuldade` e `NivelVoluntario`.
- **Padrões de projeto:**
  - Factory (`ServicoFactory`);
  - Template Method (`RelatorioTxt.gerar()`);
  - Repository (`Repositorio`).
- **Composição:** o `Voluntario` tem uma `Carteira`, e a `Candidatura` guarda as `Avaliacao`.

## Decisões técnicas

- **Créditos em `BigDecimal`** (2 casas), para não acumular erro de arredondamento como acontece com `double`.
- **Formatação sempre em pt-BR** (vírgula decimal, datas dd/MM/aaaa) pela classe `Formato`.
- **Regras validadas no serviço**, não só na tela: um teste ou outra interface não conseguem burlá-las.
- **Gravação segura:** o arquivo é salvo primeiro como temporário e depois substituído, para nunca ficar pela metade.
- **Imagens:** PNG, JPG, GIF ou BMP de até 5 MB. São copiadas para a pasta de dados, então apagar o arquivo original não afeta o sistema.
- **Logo:** desenhada com formas do próprio JavaFX (mãos segurando um coração). Aparece só na tela inicial e, por snapshot, vira o ícone da janela.

## Limitações atuais

- Os dados são locais (um computador por vez); não há servidor nem sincronização entre máquinas.
- As notificações aparecem dentro do sistema (não há e-mail ou push).
- As telas são atualizadas ao trocar de aba ou ao entrar de novo. Como o sistema roda numa janela só, não existem duas pessoas usando ao mesmo tempo.
- Mudanças nas classes do modelo podem tornar um `dana.dat` antigo ilegível. Nesse caso o sistema oferece backup e recomeça.
