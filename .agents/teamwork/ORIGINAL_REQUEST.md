# Original User Request

## 2026-10-10T04:22:11Z

Implementação completa e orgânica de ponta a ponta do aplicativo Android Aqua Sul Pós-Venda, cobrindo o fluxo integrado desde a Agenda e Lista de Clientes até o Perfil, Formulário Técnico e Confirmação de Registro (itens 002 a 007 do backlog), mantendo o padrão arquitetural MVVM, XML com ViewBinding, Jetpack DataStore e relatórios individuais de validação por item.

Working directory: C:\Users\davib\AndroidStudioProjects\AquaSulPosVenda
Integrity mode: development

## Requirements

### R1. Concluir a Agenda de Visitas (Item 002 / US01)
Finalizar as tarefas pendentes da AgendaActivity: implementar VisitaAdapter (com ViewBinding e callback de clique), AgendaViewModel (observando AppRepository.getVisitas()), inflar e conectar rvVisitas, configurar BottomNavigationView para navegar para a Lista de Clientes e fazer com que o clique no card da visita abra a tela de Formulário (FormularioAtendimentoActivity) enviando os dados da visita via Intent extras.

### R2. Implementar a Lista de Clientes com Busca (Item 003 / US02)
Implementar a ClientesActivity: criar layout, ClienteAdapter (com badge de alerta de reclamação e resumoAndamento()), ClientesViewModel (observando AppRepository.getClientes()), busca em tempo real com TextInputEditText e TextWatcher, BottomNavigationView sincronizado e clique no card de cliente navegando para a tela de Perfil (PerfilClienteActivity) enviando o clienteId.

### R3. Implementar a Consulta de Perfil do Cliente (Item 005 / US03)
Criar a PerfilClienteActivity e seus layouts XML: carregar os dados do cliente correspondente ao clienteId recebido via AppRepository, exibir dados cadastrais e de uso, listar histórico de reclamações e alternar indicadores visuais de alerta usando a regra de negócio cliente.possuiReclamacoes(). Incluir suporte a botão de retorno na barra de ferramentas.

### R4. Implementar o Formulário Técnico de Campo (Item 006 / US04)
Criar a FormularioAtendimentoActivity e seus layouts XML: exibir cabeçalho contextual somente-leitura com os dados da visita selecionada (cliente, data/hora, tipo de visita). Disponibilizar os 4 campos técnicos (Tipo de Atendimento, Condição do Equipamento via RadioGroup, Parecer Técnico obrigatório em TextInputEditText e Switch/CheckBox para registro de reclamação). Implementar validação inline de campos vazios no FormularioViewModel e persistência via AppRepository.salvarAtendimento(...).

### R5. Implementar a Tela Cheia de Confirmação (Item 007 / US05)
Criar a ConfirmacaoActivity e layout XML: exibir feedback visual comemorativo de sucesso, card com resumo do atendimento registrado e botão proeminente "Voltar para a Agenda". Configurar clique do botão para disparar Intent limpando a pilha de navegação (Intent.FLAG_ACTIVITY_CLEAR_TOP / FLAG_ACTIVITY_NEW_TASK), retornando o usuário à AgendaActivity com a lista atualizada.

### R6. Governança Contínua do Backlog e Relatórios Individuais
Para cada item do backlog (002, 003, 005, 006, 007):
- Promover e atualizar status das tarefas e do index.md no .backlogmd/work/ para status: done e marcar os critérios de aceite ([x]).
- Fornecer um relatório individual de conclusão para cada história, detalhando o que foi implementado e listando os comandos exatos de teste/compilação do Gradle para validação manual rápida pelo usuário.

## Acceptance Criteria

### Integridade do Código e Navegação Fluida
- [ ] Todas as Activities (AgendaActivity, ClientesActivity, PerfilClienteActivity, FormularioAtendimentoActivity, ConfirmacaoActivity) criadas e declaradas no AndroidManifest.xml.
- [ ] ViewBinding ativado e utilizado em todas as Activities e Adapters.
- [ ] Transição bidirecional entre Agenda e Clientes funcionando pela BottomNavigationView.
- [ ] Clique no card da Agenda abre o Formulário com contexto da visita preenchido.
- [ ] Clique no card de Clientes abre o Perfil com dados e histórico correspondentes.
- [ ] Envio do formulário salva no DataStore e abre a tela de Confirmação.
- [ ] Botão "Voltar para a Agenda" na Confirmação limpa a pilha de Activities.
- [ ] O projeto compila com sucesso (.\gradlew compileDebugSources).

### Governança e Validação
- [ ] Itens e tarefas de 002, 003, 005, 006 e 007 em .backlogmd/work/ atualizados com status done.
- [ ] Relatórios individuais emitidos contendo instruções e comandos do Gradle para validação.
