---
description: >
  Audits a plan from a senior programmer perspective and saves the audit report
  in plans/audits/ with a unique identifier, metadata, and a link to the audited plan.
  Use it when the user says things like:
  "audit this plan", "audit the plan", "review the plan as senior", "audit plan",
  "do an audit", "audit plans/Plan_Xyz.md", "check this plan", "senior review of the plan",
  "analyze the plan", "evaluate the plan", etc.
  Accepts optional argument with the plan filename or title to audit.
allowed-tools: Read, Write, Glob, PowerShell
---

## Instructions

### 1. Identify the plan to audit

If the user provided a plan name or path as an argument, use it.
Otherwise, list available plans with `Glob` on `plans/Plan_*.md` and ask the user which one to audit.

Read the full content of the target plan file before proceeding.

### 2. Determine the next audit number

Use `Glob` to list files in `plans/audits/`:

```
plans/audits/AUDIT-*.md
```

- If none exist, the number is `001`.
- If they exist, find the highest number and add 1 (3-digit zero-padded: `001`, `002`, ...).
- If the directory doesn't exist, create it.

### 3. Generate the audit slug

Take the plan filename (e.g. `Plan_Cart_RabbitMQ_Async.md`) and strip the `Plan_` prefix and `.md` extension to produce the slug (e.g. `Cart_RabbitMQ_Async`).

The audit filename will be: `plans/audits/AUDIT-{NNN}-{slug}.md`

### 4. Perform the audit — senior programmer perspective

Read the plan carefully and evaluate it across these dimensions:

**Technical soundness**
- Are the proposed technologies a good fit for the stated problem?
- Are there better or simpler alternatives that were not considered?
- Are there architectural smells, over-engineering, or under-engineering?

**Completeness**
- Are there steps that are missing or under-specified?
- Are there implicit assumptions that should be made explicit?
- Are all dependencies (infra, libraries, schema changes) accounted for?

**Risk assessment**
- Are the identified risks real and the mitigations adequate?
- Are there unidentified risks (security, performance, data integrity, ops)?

**Feasibility and sequencing**
- Is the ordering of phases/steps logical?
- Are there circular dependencies or missing prerequisites?
- Is the scope realistic given what the plan claims to deliver?

**Success criteria**
- Are the success criteria measurable and complete?
- Do they cover failure/edge cases as well as the happy path?

### 5. Create the audit file

Path: `plans/audits/AUDIT-{NNN}-{slug}.md`

Use this template (fill every section with real content derived from the audit; never leave sections empty):

```markdown
# AUDIT-{NNN}: {Plan full title}

- **ID:** AUDIT-{NNN}
- **Date:** {today's date in YYYY-MM-DD format}
- **Auditor:** Senior Programmer
- **Plan:** [{plan filename}](../{plan filename})
- **Verdict:** {one of: Approved / Approved with conditions / Needs revision / Rejected}

## Summary

{2-4 sentences: overall quality of the plan, main strengths, and the most critical concern.}

## Strengths

{Bulleted list of things the plan does well — at least 2 items.}

- ...

## Findings

### Blockers
{Issues that must be resolved before implementation begins. Leave empty only if there are none — write "None." in that case.}

- ...

### Major concerns
{Significant problems that are not hard blockers but carry meaningful risk if unaddressed.}

- ...

### Minor observations
{Small improvements, clarifications, or stylistic suggestions. Optional.}

- ...

## Risk matrix

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| {risk description} | Low / Medium / High | Low / Medium / High | {mitigation} |

## Recommendations

{Ordered list of concrete actions the plan author should take before or during implementation.}

1. ...
2. ...

## Verdict rationale

{One short paragraph explaining why this verdict was chosen and what would change it (e.g., "Approved with conditions: move to Approved once items 1 and 3 above are addressed.").}
```

### 6. Update the audit index

Read `plans/audits/README.md`. If it does not exist, create it with this header:

```markdown
# Plan Audits

This directory contains senior programmer audits of project plans.

| ID | Plan | Verdict | Date |
|----|------|---------|------|
```

Add a row at the end of the table:
```
| [AUDIT-{NNN}](AUDIT-{NNN}-{slug}.md) | [{plan title}](../{plan filename}) | {Verdict} | {date} |
```

### 7. Confirm to the user

Report:
- The path of the created audit file
- The assigned audit ID
- The verdict and a 1-2 line summary of the most critical finding

Do not print the full audit content unless the user asks for it.
