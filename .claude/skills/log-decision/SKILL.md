---
description: >
  Documents architecture, design or product decisions in ADR (Architecture Decision Record) format
  inside docs/decisions/. Use it when the user says things like:
  "document this decision", "log this decision", "save this decision", "add an ADR",
  "I want to record this", "note that we decided", "register the context of",
  "log decision", "log this decision", "write the ADR for", etc.
  Accepts optional argument with the decision title.
allowed-tools: Read, Write, Glob, PowerShell
---

## Instructions

### 1. Gather information

If the user hasn't provided enough context, ask briefly:
- **What was decided?** (the concrete decision)
- **Why?** (context and motivation)
- **What alternatives were discarded and why?**
- **What are the consequences?** (benefits, risks, technical debt)

Don't ask everything at once if part of the information is already implicit in the conversation — extract it yourself.

### 2. Determine the next ADR number

Use `Glob` to list files in `docs/decisions/`:

```
docs/decisions/ADR-*.md
```

- If none exist, the number is `001`.
- If they exist, find the highest number and add 1 (formatted with 3 digits: `001`, `002`, ...).
- If the directory doesn't exist, create it before continuing.

### 3. Generate the title slug

Convert the title to lowercase, replace spaces and special characters with hyphens.
Example: "Use Kafka for inventory events" → `use-kafka-for-inventory-events`

### 4. Create the ADR file

Path: `docs/decisions/ADR-{NNN}-{slug}.md`

Use this template (fill each section with real information; don't leave sections empty with "N/A"):

```markdown
# ADR-{NNN}: {Title}

- **Status:** Accepted
- **Date:** {today's date in YYYY-MM-DD format}
- **Author:** {user's name if known, otherwise omit this field}

## Context

{Describe the problem or situation that motivated this decision. Include relevant technical, business or team constraints.}

## Decision

{State the decision clearly and directly.}

## Alternatives considered

{List the options that were evaluated and why they were discarded. If there were no formal alternatives, explain why this was the obvious choice.}

## Consequences

### Positive
{Expected benefits}

### Negative / Risks
{Costs, risks or technical debt introduced by this decision}

## References
{Related tickets, PRs, conversations or documents. Omit this section if there are no references.}
```

### 5. Update the index

Read `docs/decisions/README.md`. If it doesn't exist, create it with this header:

```markdown
# Decision Log (ADR)

This directory contains the Architecture Decision Records for the **market** project.
Each file documents a significant decision: what was decided, why, and what the consequences are.

| # | Title | Status | Date |
|---|-------|--------|------|
```

Add a row at the end of the table:
```
| [ADR-{NNN}](ADR-{NNN}-{slug}.md) | {Title} | Accepted | {date} |
```

### 6. Confirm to the user

Report:
- The path of the created file
- The assigned ADR number
- A 1-2 line summary of what was recorded

Don't show the full file contents unless the user asks for it.
