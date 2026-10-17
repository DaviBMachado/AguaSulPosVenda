# Relatório de Entrega — Primeiro Incremento (MVP)

**Projeto:** Aqua Sul Pós-Venda  
**Disciplina:** Laboratório de Desenvolvimento para Dispositivos Móveis (LDDM) — FATEC  
**Pacote da Aplicação:** `com.aquasul.posvenda`  
**Documentação Complementar:** [Relatório Técnico e Detalhamento do Incremento](./evidencias/RELATORIO_EVIDENCIAS_PROFESSOR.md) | [Índice de Evidências](./evidencias/README.md)

---

## 1. Atendimento ao Checklist de Avaliação

### [x] Problema e usuário principal estão claros
O projeto aborda a dificuldade enfrentada pela equipe externa de manutenção da empresa **Aqua Sul** no acompanhamento pós-venda de purificadores e sistemas de filtragem. Antes da solução, os técnicos iam a campo sem visibilidade prévia sobre reclamações recorrentes dos clientes e registravam os atendimentos em fichas de papel, gerando retrabalho e perda de histórico.  
O **usuário principal** é o **técnico/colaborador de campo**, que utiliza o aplicativo durante sua rotina externa para consultar sua agenda do dia, verificar se o cliente possui queixas registradas antes de iniciar a manutenção e submeter o parecer técnico diretamente pelo celular.

### [x] Primeiro incremento corresponde ao MVP revisado
O escopo deste incremento foi delimitado para entregar o fluxo principal de ponta a ponta de forma funcional e coesa, contemplando:
1. Consulta da agenda diária de visitas técnicas;
2. Listagem e pesquisa rápida da carteira de clientes com sinalização visual de pendências;
3. Visualização do perfil detalhado do cliente e seu histórico de reclamações;
4. Preenchimento do formulário técnico de atendimento em campo com validação de dados;
5. Confirmação do registro com persistência local e retorno direto à agenda.

### [x] As correções solicitadas na Versão 0.1 foram incorporadas
Incorporamos os ajustes de escopo e de estrutura arquitetural revisados na etapa anterior: adotamos o padrão **MVVM (Model-View-ViewModel)** para separar a lógica de apresentação das regras de negócio, substituímos referências manuais de interface pelo **ViewBinding** em todas as telas e listas, centralizamos a validação no `FormularioViewModel` e implementamos persistência real no dispositivo utilizando **Jetpack DataStore**.

### [x] Há pelo menos duas telas / estrutura prevista para o incremento
Foram desenvolvidas **cinco telas completas** integradas ao fluxo operacional:
- **Agenda de Visitas (`AgendaActivity`):** Tela inicial com a listagem dos compromissos técnicos do dia.
- **Lista de Clientes (`ClientesActivity`):** Catálogo de clientes atendidos com filtro de busca em tempo real e indicadores de alerta.
- **Perfil do Cliente (`PerfilClienteActivity`):** Detalhamento cadastral, nível de usabilidade, andamento do uso e listagem dinâmica das reclamações.
- **Formulário de Atendimento (`FormularioAtendimentoActivity`):** Tela de coleta dos dados técnicos da visita (tipo de serviço, condição do equipamento, parecer técnico e ocorrência de reclamação).
- **Confirmação de Registro (`ConfirmacaoActivity`):** Tela de encerramento do fluxo exibindo o resumo dos dados gravados.

### [x] A navegação prevista é demonstrada
A navegação entre as telas principais (`AgendaActivity` e `ClientesActivity`) foi construída com o componente `BottomNavigationView`, mantendo o estado das telas com a flag `FLAG_ACTIVITY_REORDER_TO_FRONT`.  
A seleção de uma visita na agenda conduz o técnico ao formulário (`FormularioAtendimentoActivity`) transportando os dados de contexto via `Intent`. Ao concluir o preenchimento, o aplicativo avança para a tela de confirmação (`ConfirmacaoActivity`) e, ao acionar o botão *"Voltar para a Agenda"*, a pilha de navegação anterior é limpa (`FLAG_ACTIVITY_CLEAR_TOP`), evitando que o usuário retorne acidentalmente ao formulário já enviado. Na aba de clientes, o toque sobre um item abre a tela de detalhes (`PerfilClienteActivity`) com suporte a retorno pela barra superior (`MaterialToolbar`).

### [x] Há classe Kotlin / elemento de domínio identificável
O núcleo de domínio foi modelado no pacote `com.Aquasul.posvenda.domain.model` por meio das classes:
- **`Cliente.kt`:** Representa o cliente corporativo ou residencial e concentra regras de negócio próprias, como o método `possuiReclamacoes()` (que avalia se há queixas ativas para exibição de alertas na interface) e `resumoAndamento()` (que formata o indicador textual de acompanhamento e usabilidade).
- **`Visita.kt`:** Modela o agendamento técnico diário, vinculando cliente, data/horário e constantes de categoria de visita.
- **`Atendimento.kt`:** Representa o relatório técnico consolidado após a execução do serviço em campo.

### [x] Dados simulados ou estrutura de dados são coerentes com o incremento
Criamos a classe `MockDataProvider.kt` com uma carga inicial condizente com a operação real da empresa, contendo seis clientes (condomínios, hospital, escola, clínica e restaurante) e quatro visitas técnicas distribuídas ao longo do dia. Esses dados iniciais alimentam o `AppRepository.kt`, que utiliza o **Jetpack DataStore** para manter os registros persistidos localmente — permitindo que novos atendimentos e reclamações registrados durante o uso do aplicativo fiquem salvos mesmo após fechar a aplicação.

### [x] A equipe demonstra o aplicativo no Android Studio/emulador
O projeto encontra-se integralmente compilável e validado por testes unitários locais no Android Studio. Os detalhes técnicos da implementação e das telas constam no [Relatório Técnico do Incremento](./evidencias/RELATORIO_EVIDENCIAS_PROFESSOR.md).

### [x] A equipe reconhece dificuldades e pendências objetivamente
- **Dificuldades superadas:** A principal dificuldade técnica foi garantir que as alterações feitas no formulário de atendimento (como o acréscimo de uma nova reclamação ao cliente) refletissem imediatamente nas telas de Clientes e Perfil sem duplicar Activities na pilha de execução. Solucionamos isso combinando a persistência em DataStore com a atualização dos fluxos de estado (`StateFlow`) no ciclo de vida (`onResume`) de cada tela.
- **Pendências atuais:**
  1. Os atendimentos registrados permanecem apenas no armazenamento local do aparelho, ainda sem sincronização com servidor remoto;
  2. Os cards da tela de Agenda ainda não mudam seu estado visual para *"Concluído"* após o envio do formulário;
  3. O formulário atual registra apenas dados textuais e seletores, sem anexação de fotos do equipamento ou assinatura do cliente.

### [x] Próximos passos estão definidos
Para o próximo incremento do projeto, definimos como metas:
1. Implementar a marcação visual de visitas concluídas diretamente na listagem da Agenda;
2. Exibir o histórico de pareceres técnicos anteriores dentro da tela de Perfil do Cliente;
3. Integrar o repositório com uma camada de armazenamento em nuvem (Firebase / API REST) para compartilhamento de dados entre a equipe técnica e a central administrativa.

---

**Correções da Versão 0.1 incorporadas?**
- [x] Sim
- [ ] Parcialmente
- [ ] Não

---

## 2. Síntese de Autoavaliação do Grupo

* **Ponto forte:** Estruturação arquitetural em MVVM com persistência funcional em Jetpack DataStore e encapsulamento das regras de negócio no modelo de domínio (`Cliente`), entregando cinco telas integradas além do mínimo exigido.
* **Correção prioritária:** Atualizar o status visual do card da visita na tela de Agenda assim que o atendimento correspondente for finalizado.
* **Próximo passo:** Vincular os relatórios técnicos preenchidos ao histórico consultável no perfil de cada cliente e iniciar a sincronização remota.
