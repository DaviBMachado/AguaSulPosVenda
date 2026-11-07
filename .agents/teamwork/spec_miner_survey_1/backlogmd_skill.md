# Backlog Manager Skill Reference (dumped from ~/.gemini/config/skills/backlogmd/SKILL.md)

Single source of truth for `.backlogmd/` is `SPEC.md` in the repo, or the embedded Spec v4.0.5.
Items are directories in `.backlogmd/work/<item-id>-<slug>/` with `index.md` containing `<!-- METADATA -->`, `<!-- DESCRIPTION -->`, and `<!-- CONTEXT -->`.
Tasks are files `<tid>-<task-slug>.md` containing `<!-- METADATA -->`, `<!-- DESCRIPTION -->`, and `<!-- ACCEPTANCE -->`.
Task metadata contains: `task`, `status`, `priority`, `dep`, `assignee`, `requiresHumanReview`, `expiresAt`.
Item metadata contains: `work`, `status`, `assignee`.
Status flow: `plan` -> `open` -> `claimed` -> `in-progress` -> `done` (or `review` -> `done`).
