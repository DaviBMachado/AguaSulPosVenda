# Evidências de Execução e Documentação Técnica — Aqua Sul Pós-Venda

**Disciplina:** Laboratório de Desenvolvimento para Dispositivos Móveis (LDDM) — FATEC  
**Projeto:** Aqua Sul Pós-Venda (`com.Aquasul.posvenda`)

---

## 1. Índice da Documentação Entregue

Este diretório reúne os artefatos comprobatórios do desenvolvimento do primeiro incremento funcional (MVP) do aplicativo **Aqua Sul Pós-Venda**:

* **[Relatório Técnico do Incremento (`RELATORIO_EVIDENCIAS_PROFESSOR.md`)](./RELATORIO_EVIDENCIAS_PROFESSOR.md):** Documento descritivo detalhando o problema de negócio, a definição do usuário principal, a arquitetura MVVM adotada, as classes de domínio, o funcionamento das cinco telas implementadas, as dificuldades superadas pela equipe e os próximos passos planejados.
* **[Checklist de Entrega (`../CHECKLIST_ENTREGA_V01.md`)](../CHECKLIST_ENTREGA_V01.md):** Espelho direto dos critérios de avaliação da disciplina devidamente preenchidos e justificados.

---

## 2. Registro das Telas do Fluxo Principal

As telas desenvolvidas no aplicativo seguem a sequência operacional abaixo, correspondente às evidências visuais do emulador:

1. **`evidencia_01_agenda.png` — Agenda de Visitas (`AgendaActivity`):**  
   Exibe a listagem diária de compromissos técnicos carregada a partir do repositório local, apresentando o nome do cliente, o horário agendado e a modalidade de serviço, além da barra de navegação inferior.
2. **`evidencia_02_clientes.png` — Lista de Clientes com Busca (`ClientesActivity`):**  
   Demonstra o catálogo de clientes da empresa com filtragem dinâmica por nome, resumo do andamento de uso/usabilidade e sinalização visual para clientes que possuem reclamações registradas.
3. **`evidencia_03_perfil.png` — Perfil Detalhado do Cliente (`PerfilClienteActivity`):**  
   Apresenta os dados de contato, indicadores de utilização dos equipamentos e o quadro de alertas contendo o histórico de reclamações do cliente selecionado.
4. **`evidencia_04_formulario.png` — Formulário Técnico de Campo (`FormularioAtendimentoActivity`):**  
   Mostra o cabeçalho contextual preenchido com os dados da visita selecionada na agenda, os seletores de tipo de serviço e condição do equipamento, o campo obrigatório para parecer técnico com validação de preenchimento e a opção de registro de nova queixa.
5. **`evidencia_05_confirmacao.png` — Confirmação de Atendimento (`ConfirmacaoActivity`):**  
   Exibe o comprovante visual de gravação do atendimento no dispositivo, o resumo das informações submetidas e o acionamento de retorno à agenda com limpeza da pilha de navegação.

---

## 3. Comprovação de Compilação e Testes Unitários

O projeto foi integralmente validado utilizando o Gradle Wrapper do Android Studio, obtendo aprovação tanto na compilação das fontes quanto na execução dos testes automatizados de unidade:

* **Compilação do Código-Fonte e Geração de ViewBinding (`compileDebugSources`):** Concluída com sucesso (`BUILD SUCCESSFUL`), sem erros de sintaxe, recursos ou referências de layout.
* **Suíte de Testes Unitários (`testDebugUnitTest`):** Concluída com sucesso (`BUILD SUCCESSFUL`), validando os métodos de domínio da classe `Cliente` (`possuiReclamacoes()` e `resumoAndamento()`), a filtragem de busca no `ClientesViewModel`, as regras de obrigatoriedade no `FormularioViewModel` e a persistência no `AppRepository`.
