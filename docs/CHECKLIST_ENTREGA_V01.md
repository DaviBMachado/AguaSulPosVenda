# Checklist de Entrega - Versão 0.1 (MVP)
**Projeto:** Água Sul Pós-Venda  
**Disciplina:** Laboratório de Desenvolvimento para Dispositivos Móveis (LDDM) - FATEC  
**Pacote:** `com.aguasul.posvenda`  
**Data:** 10/10/2026  

---

## 1. Verificação dos 11 Critérios Obrigatórios

| # | Critério de Avaliação | Status | Evidência / Arquivo no Repositório |
|---|---|---|---|
| 1 | **MVP da Versão 0.1 revisado** | ✅ Conforme | Escopo fechado cobrindo US01 a US05, priorizando fluxo operacional de pós-venda. |
| 2 | **Fluxo principal identificado** | ✅ Conforme | Agenda (visitas do dia) ➔ Seleção de Atendimento ➔ Formulário de Campo ➔ Confirmação com retorno à Agenda. |
| 3 | **Telas necessárias ao fluxo definidas** | ✅ Conforme | 5 telas: Agenda, Clientes, Perfil do Cliente, Formulário e Confirmação. |
| 4 | **Histórias de usuário relacionadas às telas** | ✅ Conforme | US01 (Agenda), US02 (Clientes), US03 (Perfil), US04 (Formulário), US05 (Confirmação). |
| 5 | **Projeto criado e executando no Android Studio** | ✅ Conforme | Projeto estruturado com pacote `com.aguasul.posvenda`, Gradle KTS, ViewBinding e Jetpack DataStore. |
| 6 | **Pelo menos 2 telas implementadas** | ✅ Conforme (Superado) | Todas as 5 telas implementadas com layouts XML e Activities em Kotlin. |
| 7 | **Navegação funcionando entre as telas** | ✅ Conforme | `BottomNavigationView` entre Agenda e Clientes; Intents com Extras entre Agenda/Formulário/Confirmação e Clientes/Perfil; `FLAG_ACTIVITY_CLEAR_TOP` no retorno da Confirmação. |
| 8 | **Pelo menos 1 classe Kotlin do domínio criada** | ✅ Conforme | Classes `Cliente` (com `resumoAndamento()` e `possuiReclamacoes()`), `Visita` e `Atendimento`. |
| 9 | **Dados simulados utilizados quando necessário** | ✅ Conforme | `MockDataProvider` com 6 clientes e 4 visitas realistas persistidos via `AppRepository` (Jetpack DataStore). |
| 10 | **Pelo menos 2 evidências da execução inseridas** | ✅ Conforme | Pasta `docs/evidencias/` reservada para capturas das telas da Agenda e Clientes. |
| 11 | **Pendências/hipóteses para validação registradas** | ✅ Conforme | Mapeadas na seção abaixo. |

---

## 2. Hipóteses de Negócio e Pendências para Próximas Versões

### Hipóteses Validadas no MVP:
1. **Produtividade em Campo:** Apresentar a agenda de visitas diárias diretamente na tela inicial com acesso imediato ao formulário reduz o tempo de preenchimento do relatório técnico.
2. **Alertas de Pós-Venda:** Destacar visualmente clientes com queixas registradas através de `possuiReclamacoes()` orienta o técnico antes do atendimento presencial.

### Pendências e Melhorias Futuras (v0.2+):
1. **Persistência Remota:** Integração com API REST / Firebase Firestore para sincronização das visitas com a central.
2. **Assinatura Digital:** Adição de campo de assinatura do cliente na tela do formulário de campo.
3. **Fotos do Atendimento:** Captura de fotos do equipamento/filtro antes e depois do atendimento via CameraX.
