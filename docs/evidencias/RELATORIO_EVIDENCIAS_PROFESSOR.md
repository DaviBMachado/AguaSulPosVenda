# Relatório Técnico do Incremento — Aqua Sul Pós-Venda

**Disciplina:** Laboratório de Desenvolvimento para Dispositivos Móveis (LDDM) — FATEC  
**Projeto:** Aplicativo Aqua Sul Pós-Venda (`com.aquasul.posvenda`)  
**Entrega:** Primeiro Incremento Funcional (MVP Revisado)

---

## 1. Contextualização do Problema e Público-Alvo

A empresa **Aqua Sul** atua na instalação e manutenção de purificadores, bebedouros industriais e sistemas de filtragem de Aqua para clientes corporativos, escolares, hospitalares e condominiais. No processo operacional anterior, os técnicos saíam a campo sem informações consolidadas sobre problemas recentes relatados pelos clientes e dependiam do preenchimento manual de ordens de serviço em papel. Essa limitação dificultava a priorização de clientes insatisfeitos e atrasava a atualização do histórico de manutenção.

Para solucionar esse cenário, projetamos o aplicativo **Aqua Sul Pós-Venda**, voltado especificamente para o **técnico de campo**. O objetivo da aplicação é centralizar a agenda diária de visitas, sinalizar de forma imediata os clientes que possuem reclamações em aberto e permitir o registro ágil do parecer técnico logo após a intervenção no equipamento.

---

## 2. Escopo do Incremento e Arquitetura Adotada

Neste primeiro incremento, implementamos o fluxo completo previsto no MVP revisado, incorporando integralmente os apontamentos da Versão 0.1. A aplicação foi estruturada segundo o padrão arquitetural **MVVM (Model-View-ViewModel)**, organizado em três pacotes principais:

1. **`domain.model`**: Classes de domínio em Kotlin puro que representam as entidades de negócio e suas validações intrínsecas.
2. **`data.repository`**: Camada de persistência local e fornecimento de dados simulados coerentes com a operação da empresa.
3. **`ui`**: Conjunto de Activities, Adapters e ViewModels responsáveis pela exibição dos dados via **ViewBinding** e pelo gerenciamento de estado reativo com **Kotlin Coroutines** e **StateFlow**.

---

## 3. Modelagem de Domínio e Estrutura de Dados

O modelo de domínio foi construído para refletir diretamente os conceitos do negócio:

* **`Cliente` (`Cliente.kt`):** Reúne os dados cadastrais (identificador, nome, telefone/contato), indicadores de pós-venda (`andamentoUso` e `usabilidade`) e a lista de queixas registradas (`reclamacoes`). Possui dois métodos de domínio fundamentais utilizados pela interface:
  * `possuiReclamacoes(): Boolean` — Verifica se a lista de reclamações não está vazia, servindo de gatilho para a exibição de badges e alertas visuais de atenção.
  * `resumoAndamento(): String` — Consolida em uma única expressão o status de acompanhamento e a classificação de usabilidade do equipamento no cliente.
* **`Visita` (`Visita.kt`):** Representa cada compromisso agendado para o técnico, associando o identificador e nome do cliente ao horário previsto e à modalidade da visita (*Manutenção Preventiva*, *Manutenção Corretiva*, *Troca de Filtro/Refil* ou *Treinamento de Uso*).
* **`Atendimento` (`Atendimento.kt`):** Representa o documento técnico gerado ao final da visita, armazenando o tipo de intervenção realizada, o estado operacional do equipamento (*Operando Normal*, *Operando com Restrição* ou *Inoperante*), o parecer descritivo do técnico e o indicador booleano de ocorrência de reclamação.

Para viabilizar a execução e os testes do aplicativo em ambiente realista, a classe **`MockDataProvider.kt`** inicializa o sistema com seis clientes representativos (como *Condomínio Parque das Aquas*, *Hospital Santa Catarina* e *Escola Estadual Dom Pedro II*) e quatro visitas agendadas. O **`AppRepository.kt`** persiste essas informações localmente por meio do **Jetpack DataStore (`Preferences`)**, garantindo que todo novo atendimento registrado durante o uso do aplicativo seja efetivamente gravado no dispositivo.

---

## 4. Telas Desenvolvidas e Fluxo de Navegação

O aplicativo conta com cinco telas interligadas que cobrem todo o ciclo de trabalho do técnico em campo:

### 4.1. Agenda de Visitas (`AgendaActivity`)
Constitui a tela inicial do aplicativo. Utiliza um `RecyclerView` acoplado ao `VisitaAdapter` para exibir os compromissos do dia, destacando o horário, a identificação do cliente e o tipo de serviço agendado. A tela observa os estados emitidos pelo `AgendaViewModel` (`Loading`, `Success`, `Empty` e `Error`). Ao tocar sobre qualquer visita, o técnico é direcionado ao formulário de atendimento com os dados daquela visita já preenchidos.

### 4.2. Lista de Clientes (`ClientesActivity`)
Acessível através da barra de navegação inferior (`BottomNavigationView`), apresenta todos os clientes cadastrados por meio do `ClienteAdapter`. Conta com um campo de pesquisa (`TextInputEditText`) monitorado em tempo real (`doOnTextChanged`), que aciona o método de filtragem no `ClientesViewModel`. Os clientes que retornam `true` em `possuiReclamacoes()` recebem um marcador visual de alerta diretamente no card da listagem.

### 4.3. Perfil do Cliente (`PerfilClienteActivity`)
Aberta ao selecionar um cliente na listagem, recebe o identificador via `Intent` e consulta os dados atualizados através do `PerfilViewModel`. Apresenta as informações de contato, o andamento do pós-venda e a avaliação de usabilidade. Caso o cliente possua histórico de queixas, um painel de destaque é exibido no topo da seção listando cada reclamação registrada; caso contrário, é exibida uma mensagem informando a ausência de pendências.

### 4.4. Formulário Técnico de Atendimento (`FormularioAtendimentoActivity`)
Recebe os parâmetros da visita selecionada na Agenda e os apresenta em um cabeçalho contextual somente-leitura, evitando erros de identificação por parte do técnico. Abaixo do cabeçalho, disponibiliza os controles de entrada:
- Seleção do **Tipo de Atendimento** (`RadioGroup`, pré-selecionado conforme o agendamento original);
- Seleção da **Condição do Equipamento** (`RadioGroup`);
- Campo de texto obrigatório para o **Parecer Técnico / Observações** (`TextInputEditText`);
- Chave seletora (`SwitchMaterial`) para indicar se **houve reclamação registrada** no local.

A validação é processada pelo `FormularioViewModel`: caso o técnico tente salvar o atendimento sem preencher o parecer técnico, o estado `ValidationError` é emitido e o campo exibe o alerta de obrigatoriedade. Quando os dados são válidos, o atendimento é salvo no repositório e, se a chave de reclamação estiver ativa, a observação também é anexada ao histórico do respectivo cliente.

### 4.5. Confirmação de Registro (`ConfirmacaoActivity`)
Após a gravação no DataStore, o aplicativo apresenta uma tela de confirmação em tela cheia contendo o resumo dos dados registrados (cliente, modalidade do atendimento, data/horário de conclusão e condição do equipamento). O botão *"Voltar para a Agenda"* (assim como o comando de retorno do sistema Android) dispara uma `Intent` configurada com `FLAG_ACTIVITY_CLEAR_TOP` e `FLAG_ACTIVITY_SINGLE_TOP`, limpando a pilha de telas intermediárias e devolvendo o usuário diretamente à tela inicial atualizada.

---

## 5. Validação Técnica, Dificuldades Encontradas e Próximos Passos

### 5.1. Validação de Compilação e Testes
Todo o código-fonte e a suíte de testes unitários (abrangendo regras de domínio na classe `Cliente`, validações de estado no `FormularioViewModel`, filtragem no `ClientesViewModel` e persistência no `AppRepository`) foram executados e aprovados sem erros pelo Gradle (`compileDebugSources` e `testDebugUnitTest`).

### 5.2. Dificuldades Enfrentadas pela Equipe
1. **Persistência e Atualização entre Telas:** Inicialmente, ao registrar um atendimento com reclamação no formulário, a lista de clientes não exibia o novo alerta imediatamente ao alternar de aba. Resolvemos esse comportamento centralizando a instância do `AppRepository` na classe `PosVendaApplication` e garantindo a recarga reativa dos dados no método `onResume()` das telas de Agenda e Clientes.
2. **Controle da Pilha de Navegação:** Ao alternar repetidamente entre as abas *Agenda* e *Clientes* na `BottomNavigationView`, novas instâncias das Activities eram empilhadas. Corrigimos o fluxo aplicando `Intent.FLAG_ACTIVITY_REORDER_TO_FRONT` e `launchMode="singleTop"` no manifesto, mantendo a transição fluida e sem sobrecarga de memória.

### 5.3. Pendências Reconhecidas e Próximos Passos
Reconhecemos como limitações naturais deste primeiro incremento (MVP) pontos que compõem o nosso plano de evolução para a próxima entrega:
1. **Marcação Visual de Visitas Concluídas:** Atualizar o modelo `Visita` para exibir um indicador de *"Realizada"* no card da Agenda assim que o formulário correspondente for enviado.
2. **Histórico Completo de Atendimentos no Perfil:** Exibir na tela de `PerfilClienteActivity` não apenas as reclamações, mas também a linha do tempo de todos os pareceres técnicos já submetidos para aquele cliente.
3. **Sincronização Remota e Mídia:** Substituir o armazenamento exclusivamente local por sincronização em nuvem (API REST / Firebase) e permitir a captura de fotos dos equipamentos durante a vistoria.
