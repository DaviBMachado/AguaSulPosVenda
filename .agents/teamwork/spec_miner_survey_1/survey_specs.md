# Relatório Técnico de Especificação do Backlog e Contratos de Domínio / UI
**Projeto**: Aqua Sul Pós-Venda (FATEC LDDM)  
**Autor**: Backlog Spec Miner (`spec_miner_survey_1`)  
**Data**: 2026-10-10  
**Fontes Autoritativas**:
- `.backlogmd/work/` (Itens 001 a 007 e suas 19 tarefas)
- `.agents/teamwork/ORIGINAL_REQUEST.md`
- Implementação de referência existente em `app/src/main/` (`AppRepository.kt`, `domain/model/*`, `res/layout/*`, `res/menu/*`, `res/values/*`)

---

## 1. Visão Geral da Arquitetura e Padrões de Projeto

O aplicativo **Aqua Sul Pós-Venda** é construído sob a stack:
- **Linguagem**: Kotlin (JVM 11, minSdk 24, targetSdk 36, compileSdk 36.1)
- **Interface Gráfica**: XML nativo com Material Components / Material Design 3 e **ViewBinding** ativado globalmente (`buildFeatures { viewBinding = true }`).
- **Arquitetura**: MVVM (Model-View-ViewModel) com separação em múltiplas `Activities` nativas.
- **Camada de Apresentação**: `RecyclerView` com `ListAdapter`/`DiffUtil` ou `RecyclerView.Adapter` utilizando ViewBinding (`ItemVisitaBinding`, `ItemClienteBinding`).
- **Persistência Local**: Jetpack DataStore (Preferences) gerenciado via repositório singleton (`AppRepository`), serializando entidades em JSON com Gson e expondo fluxos reativos com Kotlin Coroutines `Flow`.
- **Navegação**: Sistema híbrido com `BottomNavigationView` para navegação de primeiro nível (Agenda <-> Clientes) e `Intents` com extras para o fluxo transacional (Agenda -> Formulário -> Confirmação -> Agenda; Clientes -> Perfil).
- **Pacote Base**: `com.Aquasul.posvenda`.

---

## 2. Camada de Domínio e Contratos do Repositório (Base Item 001)

### 2.1 Entidade `Cliente`
Localização: `com.Aquasul.posvenda.domain.model.Cliente`

```kotlin
data class Cliente(
    val id: String,
    val nome: String,
    val contato: String,
    val andamentoUso: String,
    val usabilidade: String,
    val reclamacoes: List<String> = emptyList()
)
```

#### Métodos de Negócio:
1. `resumoAndamento(): String`:
   - **Retorno**: `"Uso: $andamentoUso | Usabilidade: $usabilidade"`
   - **Finalidade**: Exibição concisa no card de cliente na listagem (`ClientesActivity`).
2. `possuiReclamacoes(): Boolean`:
   - **Retorno**: `reclamacoes.isNotEmpty()`
   - **Finalidade**: Flag de controle visual para exibir/ocultar badges de alerta em `ClientesActivity` e alternar cards de advertência em `PerfilClienteActivity`.

---

### 2.2 Entidade `Visita`
Localização: `com.Aquasul.posvenda.domain.model.Visita`

```kotlin
data class Visita(
    val id: String,
    val clienteId: String,
    val clienteNome: String,
    val dataHorario: String,
    val tipo: String,
    val status: String = STATUS_PENDENTE
)
```

#### Constantes e Helpers:
- Constantes de Status:
  - `Visita.STATUS_PENDENTE = "PENDENTE"`
  - `Visita.STATUS_CONCLUIDA = "CONCLUIDA"`
- Constantes de Tipo:
  - `Visita.TIPO_PREVENTIVA = "Preventiva"`
  - `Visita.TIPO_CORRETIVA = "Corretiva"`
  - `Visita.TIPO_TROCA_FILTRO = "Troca de Filtro/Refil"`
  - `Visita.TIPO_TREINAMENTO = "Treinamento"`
- Helpers:
  - `isPendente(): Boolean = status == STATUS_PENDENTE`
  - `isConcluida(): Boolean = status == STATUS_CONCLUIDA`

---

### 2.3 Entidade `Atendimento`
Localização: `com.Aquasul.posvenda.domain.model.Atendimento`

```kotlin
data class Atendimento(
    val id: String,
    val visitaId: String,
    val clienteId: String,
    val clienteNome: String,
    val dataHora: String,
    val tipoAtendimento: String,
    val condicaoEquipamento: String,
    val parecerTecnico: String,
    val houveReclamacao: Boolean
)
```

#### Constantes:
- `Atendimento.CONDICAO_NORMAL = "Operando Normal"`
- `Atendimento.CONDICAO_RESTRICAO = "Operando com Restrição"`
- `Atendimento.CONDICAO_INOPERANTE = "Inoperante"`

---

### 2.4 Repositório `AppRepository`
Localização: `com.Aquasul.posvenda.data.repository.AppRepository`

- **Instanciação**: Singleton via `AppRepository.getInstance(context: Context)`.
- **Chaves de Armazenamento DataStore Preferences**:
  - `KEY_VISITAS = stringPreferencesKey("visitas_json")`
  - `KEY_CLIENTES = stringPreferencesKey("clientes_json")`
  - `KEY_ATENDIMENTOS = stringPreferencesKey("atendimentos_json")`
- **Fallback Automático**: Se o DataStore estiver vazio ou nulo na primeira leitura, inicializa e persiste os dados de `MockDataProvider` (`MockDataProvider.visitasIniciais` e `MockDataProvider.clientesIniciais`).
- **Assinaturas dos Métodos**:
  - `getVisitas(): Flow<List<Visita>>`
  - `getClientes(): Flow<List<Cliente>>`
  - `getClienteById(id: String): Flow<Cliente?>`
  - `getAtendimentos(): Flow<List<Atendimento>>`
  - `suspend fun salvarAtendimento(atendimento: Atendimento)`:
    - Adiciona `atendimento` à lista persistida.
    - Localiza a visita com `id == atendimento.visitaId` e altera seu status para `Visita.STATUS_CONCLUIDA`.
    - Se `atendimento.houveReclamacao == true`, localiza o cliente com `id == atendimento.clienteId` e anexa a reclamação `"Registrada em atendimento técnico: ${atendimento.parecerTecnico}"` na sua lista `reclamacoes`.
    - Salva atomicamente no DataStore.
  - `suspend fun resetarParaMocks()`: restaura o DataStore para o estado inicial mockado.

---

## 3. Especificação Detalhada por Item do Backlog (US01 a US05)

### 3.1 Item 002: US01 - Visualizar Agenda de Visitas (`002-feat-agenda-visitas-us01`)

- **User Story**:  
  *"Como colaborador em campo, quero visualizar minha agenda de visitas do dia para organizar meus horários."*
- **Prioridade no Backlog**: Alta (Milestone v0.1).
- **Status Atual no Backlog**: `in-progress` (Tarefas: 001 `done`, 002 `open`, 003 `open`, 004 `open`).
- **Activity Alvo**: `AgendaActivity` (declarada no `AndroidManifest.xml` como Launcher ou acessível a partir da rota principal).

#### Componentes de UI e Layouts:
1. **Layout Principal (`activity_agenda.xml`)** (já existente em `app/src/main/res/layout/activity_agenda.xml`):
   - `mainAgenda` (`ConstraintLayout`): Raiz.
   - `toolbarAgenda` (`MaterialToolbar`): Título "Agenda do Dia", Subtítulo "Atendimentos Técnicos Agendados".
   - `rvVisitas` (`RecyclerView`): Lista vertical de visitas agendadas.
   - `progressBar` (`ProgressBar`): Indicador de loading centralizado.
   - `layoutEmpty` (`LinearLayout`): Estado vazio exibindo ícone `@drawable/ic_calendar` e texto `@string/agenda_vazia`.
   - `bottomNavigation` (`BottomNavigationView`): Menu inferior `@menu/bottom_nav_menu` com abas `nav_agenda` e `nav_clientes`.
2. **Item da Lista (`item_visita.xml`)** (já existente em `app/src/main/res/layout/item_visita.xml`):
   - `cardVisita` (`MaterialCardView`): Card clicável com cantos arredondados (14dp), stroke de 1dp.
   - `tvClienteNome` (`TextView`): Nome do cliente em destaque (17sp bold).
   - `tvStatusBadge` (`TextView`): Badge de status (`@drawable/bg_status_badge`), texto "Pendente" ou "Concluída". Estilo dinâmico:
     - Pendente: background com tint `@color/status_pendente_bg`, texto `@color/status_pendente_text`.
     - Concluída: background com tint `@color/status_concluida_bg`, texto `@color/status_concluida_text`.
   - `tvTipoVisita` (`TextView`): Exibe o tipo de visita (Preventiva, Corretiva, etc.) em cor `@color/primary`.
   - `ivClock` (`ImageView`): Ícone de relógio `@drawable/ic_clock`.
   - `tvDataHorario` (`TextView`): Horário da visita (ex: "Hoje às 08:30").
   - `tvAcaoHint` (`TextView`): Dica de ação "Toque para preencher relatório".

#### Comportamentos de Código e Classes Requeridas:
- `VisitaAdapter`:
  - Herda de `RecyclerView.Adapter<VisitaViewHolder>` ou `ListAdapter<Visita, VisitaViewHolder>`.
  - Construtor ou propriedade com callback: `onVisitaClick: (Visita) -> Unit`.
  - Utiliza `ItemVisitaBinding.inflate(...)`.
  - Vincula campos do modelo `Visita` e formata badge de status conforme `visita.isPendente()` / `visita.isConcluida()`.
  - Configura clique em `binding.cardVisita` ou `binding.root` disparando `onVisitaClick(visita)`.
- `AgendaViewModel`:
  - Observa `AppRepository.getVisitas()`.
  - Expõe `StateFlow<AgendaUiState>` ou `LiveData` com estados: `Loading`, `Success(visitas: List<Visita>)`, `Empty`.
- `AgendaActivity`:
  - Infla com `ActivityAgendaBinding.inflate(layoutInflater)`.
  - Configura `rvVisitas.layoutManager = LinearLayoutManager(this)` e associa `VisitaAdapter`.
  - Configura `bottomNavigation.selectedItemId = R.id.nav_agenda`.
  - Listener do menu: ao clicar em `R.id.nav_clientes`, inicia `ClientesActivity` (com `Intent.FLAG_ACTIVITY_REORDER_TO_FRONT` ou transição sem recriação redundante).
  - Callback de clique do adapter:
    - Cria Intent para `FormularioAtendimentoActivity`.
    - Passa extras:
      - `EXTRA_VISITA_ID` = `visita.id`
      - `EXTRA_CLIENTE_ID` = `visita.clienteId`
      - `EXTRA_CLIENTE_NOME` = `visita.clienteNome`
      - `EXTRA_DATA_HORARIO` = `visita.dataHorario`
      - `EXTRA_TIPO_VISITA` = `visita.tipo`
    - Executa `startActivity(intent)`.

---

### 3.2 Item 003: US02 - Acessar Lista de Clientes com Busca (`003-feat-lista-clientes-us02`)

- **User Story**:  
  *"Como colaborador em campo, quero acessar a lista de clientes para buscar clientes específicos e visualizar indicadores rápidos."*
- **Prioridade no Backlog**: Alta (Milestone v0.1).
- **Status Atual no Backlog**: `open` (Tarefas: 001 `open`, 002 `open`, 003 `open`, 004 `open`).
- **Activity Alvo**: `ClientesActivity`.

#### Componentes de UI e Layouts:
1. **Layout Principal (`activity_clientes.xml`)**:
   - `mainClientes` (`ConstraintLayout`): Raiz.
   - `toolbarClientes` (`MaterialToolbar`): Título "Carteira de Clientes".
   - `tilBuscaCliente` (`TextInputLayout`): Caixa de busca com ícone inicial `@drawable/ic_search`, botão de limpeza de fim (`app:endIconMode="clear_text"`), hint "Buscar cliente por nome...".
   - `etBuscaCliente` (`TextInputEditText`): Campo de entrada de texto.
   - `rvClientes` (`RecyclerView`): Lista vertical de clientes.
   - `progressBar` (`ProgressBar`): Indicador de loading.
   - `layoutEmpty` (`LinearLayout`) / `tvClientesVazio` (`TextView`): Mensagem "Nenhum cliente encontrado." quando a lista ou filtro for vazio.
   - `bottomNavigation` (`BottomNavigationView`): Menu inferior `@menu/bottom_nav_menu`, com `nav_clientes` pré-selecionado.
2. **Item da Lista (`item_cliente.xml`)**:
   - `cardCliente` (`MaterialCardView`): Card clicável, elevação e cantos arredondados padronizados.
   - `tvClienteNome` (`TextView`): Nome da empresa/cliente em destaque.
   - `tvContato` (`TextView`): Telefone e contato responsável.
   - `tvResumoAndamento` (`TextView`): Texto gerado por `cliente.resumoAndamento()` (ex: "Uso: Intensivo (24h) | Usabilidade: Excelente").
   - `tvAlertaReclamacao` (`TextView` ou Chip): Badge visual de advertência com background `@color/alerta_reclamacao_bg` e texto `@color/alerta_reclamacao_text` ("Reclamação Pendente"). Visibilidade:
     - `View.VISIBLE` se `cliente.possuiReclamacoes() == true`.
     - `View.GONE` se `cliente.possuiReclamacoes() == false`.

#### Comportamentos de Código e Classes Requeridas:
- `ClienteAdapter`:
  - Herda de `RecyclerView.Adapter<ClienteViewHolder>` ou `ListAdapter<Cliente, ClienteViewHolder>`.
  - Callback de clique: `onClienteClick: (Cliente) -> Unit`.
  - Utiliza `ItemClienteBinding`.
  - Aplica as regras de negócio: `cliente.resumoAndamento()` e `cliente.possuiReclamacoes()`.
- `ClientesViewModel`:
  - Observa `AppRepository.getClientes()`.
  - Mantém lista total e lista filtrada por busca.
  - Método `filtrar(query: String)`:
    - Se `query.isBlank()`, emite a lista completa de clientes.
    - Se preenchido, filtra onde `cliente.nome.contains(query, ignoreCase = true)` ou `cliente.contato.contains(query, ignoreCase = true)`.
  - Expõe `StateFlow<List<Cliente>>` ou LiveData.
- `ClientesActivity`:
  - Infla layout `ActivityClientesBinding`.
  - Conecta `etBuscaCliente.addTextChangedListener` ou `doOnTextChanged { text, ... -> viewModel.filtrar(text.toString()) }`.
  - Conecta `bottomNavigation`:
    - Aba selecionada: `R.id.nav_clientes`.
    - Ao selecionar `R.id.nav_agenda`: inicia ou navega de volta para `AgendaActivity` com flags para não duplicar pilhas (`FLAG_ACTIVITY_REORDER_TO_FRONT`).
  - Callback de clique no card:
    - Dispara `Intent` para `PerfilClienteActivity`.
    - Passa extra: `EXTRA_CLIENTE_ID` = `cliente.id`.
    - Chama `startActivity(intent)`.

---

### 3.3 Item 005: US03 - Consultar Perfil do Cliente (`005-feat-perfil-cliente-us03`)

- **User Story**:  
  *"Como colaborador em campo, quero acessar o perfil detalhado do cliente selecionado para visualizar contatos, relatórios e reclamações antes da visita."*
- **Prioridade no Backlog**: Média / Próximo Incremento (Milestone v0.2).
- **Status Atual no Backlog**: `plan` (Tarefas: 001 `plan`, 002 `plan`).
- **Activity Alvo**: `PerfilClienteActivity`.

#### Componentes de UI e Layouts:
1. **Layout Principal (`activity_perfil_cliente.xml`)**:
   - `toolbarPerfil` (`MaterialToolbar`): Título "Perfil do Cliente", com botão de navegação Up / seta de voltar habilitada (`app:navigationIcon`).
   - Seção Cadastral (`cardDadosCadastrais`):
     - `tvNomeCliente` (`TextView`): Nome completo do cliente.
     - `tvContato` (`TextView`): Contato telefônico / responsável.
     - `tvIdCliente` (`TextView`): Identificador do cliente (ex: "ID: cli_001").
   - Seção de Indicadores de Uso (`cardIndicadoresUso`):
     - `tvAndamentoUso` (`TextView`): Descrição do andamento do uso.
     - `tvUsabilidade` (`TextView`): Avaliação de usabilidade.
     - `tvResumoFormatado` (`TextView`): Resumo retornado por `cliente.resumoAndamento()`.
   - Seção de Alertas de Reclamação (`cardAlertaReclamacao`):
     - Banner / Card em vermelho suave (`@color/alerta_reclamacao_bg`), borda de destaque.
     - `tvTituloAlerta` (`TextView`): "Atenção: Cliente possui reclamações registradas".
     - Controlado por `cliente.possuiReclamacoes()`:
       - Se `false`: card pode ficar invisível (`View.GONE`) ou exibir status positivo ("Nenhuma pendência ou reclamação").
   - Seção de Histórico de Reclamações (`cardHistoricoReclamacoes`):
     - `rvReclamacoes` (`RecyclerView`) ou `LinearLayout` container dinâmico de reclamações.
     - `tvSemReclamacoes` (`TextView`): Mensagem visível quando a lista `reclamacoes` estiver vazia.

#### Comportamentos de Código e Classes Requeridas:
- `PerfilClienteViewModel`:
  - Recebe `clienteId: String`.
  - Consulta `AppRepository.getClienteById(clienteId)`.
  - Expõe `StateFlow<Cliente?>` ou LiveData.
- `PerfilClienteActivity`:
  - Infla `ActivityPerfilClienteBinding`.
  - Obtém `clienteId = intent.getStringExtra(EXTRA_CLIENTE_ID)`.
  - Configura clique da navegação na Toolbar para finalizar a Activity: `toolbar.setNavigationOnClickListener { finish() }`.
  - Observa o cliente carregado:
    - Preenche os campos textuais.
    - Avalia `cliente.possuiReclamacoes()` para gerenciar o alerta.
    - Se houver reclamações, renderiza cada item da lista `cliente.reclamacoes`.

---

### 3.4 Item 006: US04 - Preencher Formulário Técnico de Campo (`006-feat-formulario-campo-us04`)

- **User Story**:  
  *"Como colaborador em campo, quero preencher os dados do atendimento realizado durante a visita técnica."*
- **Prioridade no Backlog**: Alta (Milestone v0.2).
- **Status Atual no Backlog**: `plan` (Tarefas: 001 `plan`, 002 `plan`).
- **Activity Alvo**: `FormularioAtendimentoActivity`.

#### Componentes de UI e Layouts:
1. **Layout Principal (`activity_formulario_atendimento.xml`)**:
   - `toolbarFormulario` (`MaterialToolbar`): Título "Relatório de Atendimento", ícone de voltar.
   - `scrollView` com container vertical.
   - **Cabeçalho Contextual da Visita** (`cardContextoVisita` - somente leitura):
     - `tvContextoCliente`: Nome do cliente selecionado.
     - `tvContextoHorario`: Horário agendado da visita.
     - `tvContextoTipo`: Tipo agendado da visita.
   - **Campo 1: Tipo de Atendimento Realizado**:
     - `tilTipoAtendimento` (`TextInputLayout`) com `actvTipoAtendimento` (`AutoCompleteTextView` estilo dropdown) OU `Spinner` / seletor.
     - Opções: "Preventiva", "Corretiva", "Troca de Filtro/Refil", "Treinamento".
     - Valor inicial padrão preenchido com base no tipo da visita recebida.
   - **Campo 2: Condição do Equipamento**:
     - `rgCondicaoEquipamento` (`RadioGroup`):
       - `rbCondicaoNormal` (`RadioButton`): "Operando Normal" (`Atendimento.CONDICAO_NORMAL`)
       - `rbCondicaoRestricao` (`RadioButton`): "Operando com Restrição" (`Atendimento.CONDICAO_RESTRICAO`)
       - `rbCondicaoInoperante` (`RadioButton`): "Inoperante" (`Atendimento.CONDICAO_INOPERANTE`)
   - **Campo 3: Parecer Técnico / Observações (Obrigatório)**:
     - `tilParecerTecnico` (`TextInputLayout` com `app:errorEnabled="true"`):
     - `etParecerTecnico` (`TextInputEditText`): Multilinhas (`minLines="3"`, `inputType="textMultiLine"`), hint "Descreva detalhadamente o serviço executado...".
   - **Campo 4: Houve Reclamação Registrada?**:
     - `switchHouveReclamacao` (`MaterialSwitch`) ou `cbHouveReclamacao` (`CheckBox`):
     - Texto: "Registrar queixa/reclamação formal do cliente".
   - **Botão de Ação**:
     - `btnFinalizarAtendimento` (`MaterialButton`): Texto "Finalizar e Registrar Atendimento", cor primária em destaque.

#### Regras de Validação e Submissão:
- **Validações Obrigatórias**:
  1. `etParecerTecnico`: Não pode estar vazio nem conter apenas espaços em branco (`parecer.trim().isNotEmpty()`). Se inválido, definir erro inline no `tilParecerTecnico`: `"O parecer técnico é obrigatório."`.
  2. `rgCondicaoEquipamento`: Uma das opções de rádio deve estar obrigatoriamente selecionada (`checkedRadioButtonId != -1`).
  3. Tipo de Atendimento: Deve ter valor válido selecionado.
- **Fluxo de Persistência no ViewModel (`FormularioViewModel`)**:
  - Na validação positiva:
    ```kotlin
    val atendimento = Atendimento(
        id = UUID.randomUUID().toString(),
        visitaId = visitaId,
        clienteId = clienteId,
        clienteNome = clienteNome,
        dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
        tipoAtendimento = tipoSelecionado,
        condicaoEquipamento = condicaoSelecionada,
        parecerTecnico = parecer,
        houveReclamacao = switchReclamacao.isChecked
    )
    repository.salvarAtendimento(atendimento)
    ```
  - Disparar navegação para `ConfirmacaoActivity` com Intent extras:
    - `EXTRA_ATENDIMENTO_ID` = `atendimento.id`
    - `EXTRA_CLIENTE_NOME` = `atendimento.clienteNome`
    - `EXTRA_TIPO_ATENDIMENTO` = `atendimento.tipoAtendimento`
    - `EXTRA_DATA_HORA` = `atendimento.dataHora`
    - `EXTRA_CONDICAO_EQUIPAMENTO` = `atendimento.condicaoEquipamento`
    - `EXTRA_HOUVE_RECLAMACAO` = `atendimento.houveReclamacao`

---

### 3.5 Item 007: US05 - Tela Cheia de Confirmação de Registro (`007-feat-confirmacao-registro-us05`)

- **User Story**:  
  *"Como colaborador, quero confirmar que o atendimento do formulário foi salvo e registrado com sucesso."*
- **Prioridade no Backlog**: Alta (Milestone v0.2).
- **Status Atual no Backlog**: `plan` (Tarefas: 001 `plan`, 002 `plan`).
- **Activity Alvo**: `ConfirmacaoActivity`.

#### Componentes de UI e Layouts:
1. **Layout Principal (`activity_confirmacao.xml`)**:
   - `mainConfirmacao` (`ConstraintLayout`): Tela cheia com fundo limpo.
   - `ivSucessoIcon` (`ImageView`): Ícone de checkmark / sucesso comemorativo em destaque circular (cor verde positiva `@color/status_concluida_text` ou container).
   - `tvTituloConfirmacao` (`TextView`): "Atendimento Registrado com Sucesso!" (22sp bold, centralizado).
   - `tvSubtituloConfirmacao` (`TextView`): "Os dados foram salvos e sincronizados com a carteira de pós-venda da Aqua Sul."
   - `cardResumo` (`MaterialCardView`):
     - `tvResumoCliente` (`TextView`): "Cliente: Hospital São Lucas"
     - `tvResumoTipo` (`TextView`): "Tipo: Visita Preventiva"
     - `tvResumoDataHora` (`TextView`): "Data/Hora: 10/10/2026 10:45"
     - `tvResumoStatusVisita` (`TextView`): "Status da Visita: Concluída"
   - `btnVoltarAgenda` (`MaterialButton`): Botão largo e proeminente: "Voltar para a Agenda".

#### Contrato de Navegação e Limpeza de Pilha (Back Stack):
- **Ação do Botão `btnVoltarAgenda`**:
  - Deve instanciar uma Intent apontando para a `AgendaActivity`.
  - Aplicar flags obrigatórias de limpeza:
    ```kotlin
    val intent = Intent(this, AgendaActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    startActivity(intent)
    finish()
    ```
  - Alternativa com reset total de pilha: `Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK`.
- **Efeito Comportamental**:
  - Elimina a `FormularioAtendimentoActivity` e a `ConfirmacaoActivity` da pilha de histórico.
  - Ao retornar para a `AgendaActivity`, ela é reexibida observando o repositório, de modo que a visita correspondente aparece com o badge **"Concluída"** (verde).
  - Se o usuário pressionar o botão "Voltar" do sistema Android enquanto estiver na Agenda, o aplicativo deve ser minimizado/encerrado, **nunca** reabrindo a tela de confirmação ou formulário.

---

## 4. Matriz de Navegação e Contratos de Intent Extras

| Rota de Origem | Rota de Destino | Disparo | Flags de Intent | Extras Trafegados |
|----------------|-----------------|---------|-----------------|-------------------|
| `AgendaActivity` | `ClientesActivity` | Toque na aba `nav_clientes` do `BottomNavigationView` | `FLAG_ACTIVITY_REORDER_TO_FRONT` ou navegação direta | N/A |
| `ClientesActivity` | `AgendaActivity` | Toque na aba `nav_agenda` do `BottomNavigationView` | `FLAG_ACTIVITY_REORDER_TO_FRONT` ou navegação direta | N/A |
| `AgendaActivity` | `FormularioAtendimentoActivity` | Clique no card da visita (`cardVisita`) | Padrão | `EXTRA_VISITA_ID` (String)<br>`EXTRA_CLIENTE_ID` (String)<br>`EXTRA_CLIENTE_NOME` (String)<br>`EXTRA_DATA_HORARIO` (String)<br>`EXTRA_TIPO_VISITA` (String) |
| `ClientesActivity` | `PerfilClienteActivity` | Clique no card do cliente (`cardCliente`) | Padrão | `EXTRA_CLIENTE_ID` (String) |
| `PerfilClienteActivity` | `ClientesActivity` | Toque no botão de retorno da Toolbar (`NavigationOnClickListener`) | `finish()` padrão | N/A |
| `FormularioAtendimentoActivity` | `ConfirmacaoActivity` | Clique em "Finalizar e Registrar Atendimento" com campos válidos | Padrão | `EXTRA_ATENDIMENTO_ID` (String)<br>`EXTRA_CLIENTE_NOME` (String)<br>`EXTRA_TIPO_ATENDIMENTO` (String)<br>`EXTRA_DATA_HORA` (String)<br>`EXTRA_CONDICAO_EQUIPAMENTO` (String)<br>`EXTRA_HOUVE_RECLAMACAO` (Boolean) |
| `ConfirmacaoActivity` | `AgendaActivity` | Clique no botão "Voltar para a Agenda" | `FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP` | N/A |

### Constantes Padronizadas de Extras:
```kotlin
object IntentExtras {
    const val EXTRA_VISITA_ID = "com.Aquasul.posvenda.VISITA_ID"
    const val EXTRA_CLIENTE_ID = "com.Aquasul.posvenda.CLIENTE_ID"
    const val EXTRA_CLIENTE_NOME = "com.Aquasul.posvenda.CLIENTE_NOME"
    const val EXTRA_DATA_HORARIO = "com.Aquasul.posvenda.DATA_HORARIO"
    const val EXTRA_TIPO_VISITA = "com.Aquasul.posvenda.TIPO_VISITA"
    const val EXTRA_ATENDIMENTO_ID = "com.Aquasul.posvenda.ATENDIMENTO_ID"
    const val EXTRA_TIPO_ATENDIMENTO = "com.Aquasul.posvenda.TIPO_ATENDIMENTO"
    const val EXTRA_DATA_HORA = "com.Aquasul.posvenda.DATA_HORA"
    const val EXTRA_CONDICAO_EQUIPAMENTO = "com.Aquasul.posvenda.CONDICAO_EQUIPAMENTO"
    const val EXTRA_HOUVE_RECLAMACAO = "com.Aquasul.posvenda.HOUVE_RECLAMACAO"
}
```

---

## 5. Tabela de Funcionalidades Descobertas (Features Discovered)

| # | Categoria | Funcionalidade | Descrição | Entradas (Inputs) | Saídas (Outputs) | Comportamento de Erro | Descoberto Via |
|---|-----------|----------------|-----------|-------------------|------------------|----------------------|----------------|
| 1 | Domínio | `Cliente.resumoAndamento()` | Formata resumo textual do andamento de uso e usabilidade | Instância de `Cliente` com `andamentoUso` e `usabilidade` | String formatada: `"Uso: X \| Usabilidade: Y"` | Valores nulos/vazios produzem string com campos vazios | `.backlogmd/work/001.../002-implementar-classes-dominio-mocks.md` |
| 2 | Domínio | `Cliente.possuiReclamacoes()` | Determina se o cliente tem pendências de reclamação | Instância de `Cliente` | Booleano (`true` se lista não estiver vazia, senão `false`) | Lista vazia retorna `false` sem exceção | `.backlogmd/work/001.../002-implementar-classes-dominio-mocks.md` |
| 3 | Domínio | `Visita.isPendente()` / `isConcluida()` | Helpers de validação de estado da visita | Instância de `Visita` | Booleano | Status inesperado avalia como `false` | `Visita.kt` |
| 4 | Persistência | Leitura e Fallback de Visitas | Carrega visitas do DataStore Preferences ou inicializa com mocks | Leitura da chave `KEY_VISITAS` | `Flow<List<Visita>>` | `IOException` emite preferências vazias capturadas via `.catch` | `AppRepository.kt` |
| 5 | Persistência | Leitura e Fallback de Clientes | Carrega clientes do DataStore Preferences ou inicializa com mocks | Leitura da chave `KEY_CLIENTES` | `Flow<List<Cliente>>` | Fallback para `MockDataProvider.clientesIniciais` | `AppRepository.kt` |
| 6 | Persistência | Consulta Cliente por ID | Filtra cliente específico pelo identificador único | `id: String` | `Flow<Cliente?>` | Retorna `null` se não encontrar | `AppRepository.kt` |
| 7 | Persistência | Salvar Atendimento com Efeito Cascata | Grava atendimento, conclui visita e adiciona reclamação se houver | Instância de `Atendimento` | Persistência no DataStore de 3 listas | Erro de IO tratado pelo DataStore | `AppRepository.kt` |
| 8 | Persistência | Reset para Mocks | Restaura estado original simulado das visitas e clientes | Invocação de método de teste | Gravação dos mocks padrão | Tratamento assíncrono suspend | `AppRepository.kt` |
| 9 | UI / Agenda | Renderização de Visitas | Lista atendimentos diários com ViewBinding e CardView | Lista de `Visita` do ViewModel | Exibição em `RecyclerView` com badges | Lista vazia exibe layout `layoutEmpty` | `activity_agenda.xml` / `item_visita.xml` |
| 10 | UI / Agenda | Badge de Status Dinâmico | Alterna cores do badge (Amarelo = Pendente, Verde = Concluída) | `visita.status` | Mudança de background e cor de texto | Default para estilo pendente se desconhecido | `item_visita.xml` / `colors.xml` |
| 11 | UI / Agenda | Navegação para Formulário | Clique no card da visita abre tela de preenchimento com contexto | Toque em `cardVisita` | Abertura de `FormularioAtendimentoActivity` com extras | Bloqueio de cliques repetidos (debounce) | `002-feat-agenda-visitas-us01/004-configurar-bottom-navigation-agenda.md` |
| 12 | UI / Navegação | BottomNavigationView | Alterna abas principais entre Agenda e Clientes | Seleção de item de menu | Troca de Activity com item correto selecionado | Ignora seleção redundante na mesma tela | `res/menu/bottom_nav_menu.xml` |
| 13 | UI / Clientes | Listagem de Clientes | Apresenta clientes com contatos e indicadores rápidos | Lista de `Cliente` do ViewModel | Cards renderizados com resumo e badge | Lista vazia exibe mensagem de estado vazio | `003-feat-lista-clientes-us02/001-criar-layout-item-e-activity-clientes.md` |
| 14 | UI / Clientes | Badge de Alerta de Reclamação | Exibe advertência vermelha "Reclamação Pendente" | `cliente.possuiReclamacoes()` | Visibilidade `VISIBLE` ou `GONE` | Oculta badge se `false` | `003-feat-lista-clientes-us02/002-implementar-adapter-clientes-filtro.md` |
| 15 | UI / Clientes | Busca em Tempo Real | Filtra clientes por nome dinamicamente com TextWatcher | Digitação no `TextInputEditText` | Emissão de lista filtrada sem travar UI | Query sem correspondência exibe estado vazio | `003-feat-lista-clientes-us02/003-implementar-busca-textwatcher-viewmodel.md` |
| 16 | UI / Clientes | Navegação para Perfil | Clique no card do cliente abre detalhes | Toque em `cardCliente` | Abertura de `PerfilClienteActivity` com `clienteId` | Se ID for inválido, Perfil exibe erro ou finaliza | `003-feat-lista-clientes-us02/004-configurar-bottom-navigation-clientes.md` |
| 17 | UI / Perfil | Exibição Cadastral e Histórico | Apresenta dados de contato, uso, usabilidade e queixas | `clienteId` via Intent | Preenchimento dos cards de detalhes | Cliente não encontrado exibe estado amigável | `005-feat-perfil-cliente-us03/001-criar-ui-perfil-cliente.md` |
| 18 | UI / Perfil | Alerta Condicional de Reclamações | Destaque visual caso existam queixas registradas | `cliente.possuiReclamacoes()` | Card de advertência destacado com lista | Se vazio, esconde card ou mostra mensagem positiva | `005-feat-perfil-cliente-us03/002-integrar-dados-e-alertas-reclamacao.md` |
| 19 | UI / Perfil | Retorno via Toolbar | Finaliza tela de perfil retornando à listagem | Toque no ícone Up da Toolbar | Executa `finish()` | Sem efeito colateral | `005-feat-perfil-cliente-us03/001-criar-ui-perfil-cliente.md` |
| 20 | UI / Formulário | Cabeçalho Contextual | Exibe dados da visita agendada em modo somente-leitura | Intent extras da visita | Cards com dados pré-preenchidos | Extras ausentes exibem fallbacks seguros | `006-feat-formulario-campo-us04/001-criar-ui-formulario-campo.md` |
| 21 | UI / Formulário | Seletor de Tipo de Atendimento | Dropdown com 4 opções de tipo | Seleção do usuário | Valor selecionado para o atendimento | Default inicial baseado na visita | `006-feat-formulario-campo-us04/001-criar-ui-formulario-campo.md` |
| 22 | UI / Formulário | Condição do Equipamento | RadioGroup com 3 opções padrão | Seleção de RadioButton | String correspondente da constante | Nenhum rádio selecionado bloqueia submissão | `006-feat-formulario-campo-us04/001-criar-ui-formulario-campo.md` |
| 23 | UI / Formulário | Validação de Parecer Técnico | Campo multilinhas obrigatório | Texto digitado no `TextInputEditText` | Aceite ou erro inline no `TextInputLayout` | Se vazio ou espaços: exibe mensagem de erro | `006-feat-formulario-campo-us04/002-implementar-validacao-e-salvamento-viewmodel.md` |
| 24 | UI / Formulário | Switch de Reclamação | Alternador booleano para registrar queixa formal | Interação com Switch | Booleano `houveReclamacao` | Mantém `false` como padrão | `006-feat-formulario-campo-us04/001-criar-ui-formulario-campo.md` |
| 25 | UI / Formulário | Submissão e Persistência | Valida campos, cria entidade e salva no DataStore | Clique em "Finalizar e Registrar" | Transição para tela de confirmação | Campos inválidos abortam gravação | `006-feat-formulario-campo-us04/002-implementar-validacao-e-salvamento-viewmodel.md` |
| 26 | UI / Confirmação | Feedback Comemorativo | Tela cheia com ícone comemorativo de sucesso | Conclusão do registro do atendimento | Layout com ícone grande e textos de sucesso | N/A | `007-feat-confirmacao-registro-us05/001-criar-ui-confirmacao-registro.md` |
| 27 | UI / Confirmação | Resumo do Atendimento Registrado | Card com informações resumidas do atendimento salvo | Intent extras com dados do registro | Exibição do cliente, tipo, data/hora e status | Extras ausentes tratam com strings seguras | `007-feat-confirmacao-registro-us05/001-criar-ui-confirmacao-registro.md` |
| 28 | UI / Confirmação | Retorno com Limpeza de Pilha | Botão "Voltar para a Agenda" desempilha telas | Clique no botão proeminente | Retorna à AgendaActivity limpa e atualizada | Pressionar voltar físico na Agenda fecha o app | `007-feat-confirmacao-registro-us05/002-implementar-navegacao-limpeza-pilha.md` |
| 29 | Documentação | Checklist v0.1 | Documento formal cobrindo os 11 itens acadêmicos de LDDM | Arquivo `docs/CHECKLIST_ENTREGA_V01.md` | Markdown estruturado com evidências | N/A | `004-docs-checklist-entrega-v01/001-estruturar-documento-checklist-v01.md` |
| 30 | Documentação | Diretório de Evidências | Pasta de prints da execução do app (`docs/evidencias/`) | Guia de captura de telas | Arquivos de imagem (`evidencia_01_*.png`) | N/A | `004-docs-checklist-entrega-v01/002-configurar-diretorio-evidencias.md` |

---

## 6. Tabela de Casos de Borda e Validações (Edge Cases)

| # | Funcionalidade | Entrada (Input) | Comportamento Observado / Esperado |
|---|----------------|-----------------|-------------------------------------|
| 1 | Agenda de Visitas | Lista de visitas vazia no repositório | Oculta `rvVisitas`, exibe `layoutEmpty` com ícone de calendário e mensagem "Nenhum atendimento agendado para hoje.". |
| 2 | Agenda de Visitas | Visita com status `CONCLUIDA` | O badge `tvStatusBadge` deve mudar para cor verde (`@color/status_concluida_bg` e texto `@color/status_concluida_text`) com texto "Concluída". |
| 3 | Busca de Clientes | Termo com espaços ou caracteres maiúsculos (ex: `"  HOSPITAL "`) | A busca aplica `.trim()` e `.contains(..., ignoreCase = true)`, encontrando com sucesso "Hospital São Lucas". |
| 4 | Busca de Clientes | Termo inexistente (ex: `"xyz999"`) | O `rvClientes` fica vazio e a mensagem `tvClientesVazio` ("Nenhum cliente encontrado.") torna-se visível. |
| 5 | Busca de Clientes | Campo de busca limpo pelo botão de "clear" (`endIcon`) | Restaura instantaneamente a lista completa dos 6 clientes simulados. |
| 6 | Lista de Clientes | Cliente sem reclamações (`reclamacoes = emptyList()`) | `possuiReclamacoes()` retorna `false`. O badge `tvAlertaReclamacao` no card do cliente fica com `visibility = GONE`. |
| 7 | Perfil de Cliente | Cliente com múltiplas reclamações cadastradas | `possuiReclamacoes()` retorna `true`. O banner de alerta fica vermelho visível e todas as reclamações são listadas no histórico. |
| 8 | Perfil de Cliente | `clienteId` nulo ou inválido passado via Intent | O ViewModel/Activity detecta valor nulo, exibe Toast/Snackbar de erro e encerra com `finish()` sem crash. |
| 9 | Formulário Técnico | Submissão com Parecer Técnico em branco (`""` ou `"   "`) | `tilParecerTecnico.error` é definido com mensagem inline `"O parecer técnico é obrigatório."`, o foco vai para o campo e o salvamento é abortado. |
| 10 | Formulário Técnico | Submissão sem selecionar condição no `RadioGroup` | Exibe mensagem de validação orientando a seleção de uma das 3 condições do equipamento antes de salvar. |
| 11 | Formulário Técnico | Atendimento finalizado com `houveReclamacao = true` | `AppRepository.salvarAtendimento(...)` anexa `"Registrada em atendimento técnico: $parecerTecnico"` na lista do cliente e atualiza o DataStore. |
| 12 | Formulário Técnico | Atendimento finalizado com `houveReclamacao = false` | Visita é marcada como `CONCLUIDA`, atendimento é salvo, e a lista de reclamações do cliente permanece inalterada. |
| 13 | Confirmação | Clique no botão físico de "Voltar" do aparelho na tela de confirmação | Deve ter o mesmo comportamento do botão na tela (limpar pilha e voltar para a Agenda ou desabilitar retorno para o formulário já submetido). |
| 14 | Confirmação | Retorno à Agenda via "Voltar para a Agenda" | A visita recém-atendida agora é re-renderizada na Agenda como "Concluída". O histórico anterior de telas é descartado. |
| 15 | Primeira Execução | App aberto pela primeira vez em instalação limpa (DataStore vazio) | `AppRepository` intercepta ausência de chaves, grava automaticamente os 6 clientes e 4 visitas de `MockDataProvider`, e carrega a tela sem delay perceptível. |

---

## 7. Inventário de Tarefas e Governança do Backlog (`.backlogmd/work/`)

Conforme a regra **R6** do `ORIGINAL_REQUEST.md`, após a implementação de cada história de usuário, todos os arquivos de tarefa correspondentes e os arquivos `index.md` em `.backlogmd/work/` devem ser atualizados:
- `status: done`
- `assignee: ""`
- Todos os critérios de aceite marcados com `- [x]`.

### Mapeamento Completo de Itens e Tarefas a Promover para `done`:

| Item do Backlog | Tarefa ID e Nome | Status Atual | Status Alvo |
|-----------------|-------------------|--------------|-------------|
| **002-feat-agenda-visitas-us01** | `index.md` | `in-progress` | `done` |
| | `001-criar-layout-item-e-activity-agenda.md` | `done` | `done` |
| | `002-implementar-adapter-visitas.md` | `open` | `done` |
| | `003-implementar-viewmodel-agenda.md` | `open` | `done` |
| | `004-configurar-bottom-navigation-agenda.md` | `open` | `done` |
| **003-feat-lista-clientes-us02** | `index.md` | `open` | `done` |
| | `001-criar-layout-item-e-activity-clientes.md` | `open` | `done` |
| | `002-implementar-adapter-clientes-filtro.md` | `open` | `done` |
| | `003-implementar-busca-textwatcher-viewmodel.md` | `open` | `done` |
| | `004-configurar-bottom-navigation-clientes.md` | `open` | `done` |
| **005-feat-perfil-cliente-us03** | `index.md` | `plan` | `done` |
| | `001-criar-ui-perfil-cliente.md` | `plan` | `done` |
| | `002-integrar-dados-e-alertas-reclamacao.md` | `plan` | `done` |
| **006-feat-formulario-campo-us04** | `index.md` | `plan` | `done` |
| | `001-criar-ui-formulario-campo.md` | `plan` | `done` |
| | `002-implementar-validacao-e-salvamento-viewmodel.md` | `plan` | `done` |
| **007-feat-confirmacao-registro-us05** | `index.md` | `plan` | `done` |
| | `001-criar-ui-confirmacao-registro.md` | `plan` | `done` |
| | `002-implementar-navegacao-limpeza-pilha.md` | `plan` | `done` |

---

## 8. Comandos de Compilação e Validação do Gradle

Para a verificação e testes manuais rápidos solicitados pelo usuário e pipelines de validação:

1. **Compilação das fontes Kotlin/Java de Debug**:
   ```powershell
   .\gradlew.bat compileDebugSources
   ```
2. **Execução de Testes Unitários de Debug**:
   ```powershell
   .\gradlew.bat testDebugUnitTest
   ```
3. **Montagem do APK de Debug**:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. **Instalação no emulador / dispositivo conectado**:
   ```powershell
   .\gradlew.bat installDebug
   ```
