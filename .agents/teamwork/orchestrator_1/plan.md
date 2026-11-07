# Project Orchestration Plan: Aqua Sul Pós-Venda

## Overall Goal
Complete end-to-end implementation and validation of Aqua Sul Pós-Venda app (items 002-007) adhering to MVVM, ViewBinding, Jetpack DataStore, Coroutines, AndroidManifest registration, BacklogMD governance, and individual test/validation reports.

## Phase 0: Survey & Scope Mapping
- Spawn 3 survey agents (1 spec miner, 2 explorers):
  - Spec Miner: Mine `.backlogmd/work/` items 002, 003, 005, 006, 007 requirements, acceptance criteria, and task checklists.
  - Explorer 1: Inspect Android project structure, Gradle build setup, dependencies, AndroidManifest.xml, DataStore setup, AppRepository, and data models.
  - Explorer 2: Inspect existing UI screens, navigation, layouts, ViewModels, and test harness/runner capability.
- Synthesize findings into `PROJECT.md` (Architecture, Feature Inventory, Milestones, Interface Contracts, Code Layout).
- Create `TEST_INFRA.md`.

## Phase 1: Milestones Execution
- M1: Concluir a Agenda de Visitas (Item 002 / US01)
- M2: Implementar a Lista de Clientes com Busca (Item 003 / US02)
- M3: Implementar a Consulta de Perfil do Cliente (Item 005 / US03)
- M4: Implementar o Formulário Técnico de Campo (Item 006 / US04)
- M5: Implementar a Tela Cheia de Confirmação (Item 007 / US05)
- M6: Governança Contínua do Backlog e Relatórios Individuais

## Phase 2: Dual Track E2E Test Suite & Adversarial Hardening
- Complete verification across all tiers (Feature, Boundary, Combinatorial, End-to-End).
- Final compile and unit test execution via Gradle.
- Backlog governance verification and generation of individual validation reports.
- Completion report sent to Sentinel.
