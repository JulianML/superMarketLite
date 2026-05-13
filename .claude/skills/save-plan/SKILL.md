---
description: >
  Documents and saves work, development or product plans in the plans/ directory.
  The plan is saved in Plan_WordOneWordTwoWordThree.md format, where the three words
  summarize the plan in English. Use it when the user says things like:
  "save this plan", "document this plan", "log this plan", "create a plan",
  "save the plan", "save plan", "log plan", "note this plan",
  "record the plan", "write the plan for", etc.
  Accepts optional argument with the plan title or topic.
allowed-tools: Read, Write, Glob, PowerShell
---

## Instructions

### 1. Gather information

If the user hasn't provided enough context, ask briefly:
- **What is going to be done?** (plan objective)
- **Why?** (motivation or problem it solves)
- **What are the main steps or phases?**
- **Are there any constraints, dependencies or risks?**

Don't ask everything at once if part of the information is already in the conversation — extract it yourself.

### 2. Generate the three-word name

Summarize the plan in exactly **three words in English**.

- They must be descriptive and concrete, not generic (avoid words like "Plan", "New", "General").
- Use nouns or infinitive verbs that capture the essence of the plan.
- Write each word in PascalCase (first letter uppercase, no special characters).
- Join them with underscores for the filename.

Examples:
- "Migrate authentication to JWT with refresh tokens" → `Auth_Migration_JWT`
- "Redesign the product catalog for mobile" → `Catalog_Mobile_Redesign`
- "Integrate Stripe payments at checkout" → `Stripe_Payment_Integration`

The filename will be: `Plan_WordOne_WordTwo_WordThree.md`

### 3. Create the plan file

Path: `plans/Plan_{WordOne}_{WordTwo}_{WordThree}.md`

Use this template (fill each section with real information; don't leave sections empty):

```markdown
# Plan: {Full plan title}

- **Date:** {today's date in YYYY-MM-DD format}
- **Status:** Draft
- **Author:** {user's name if known, otherwise omit this field}

## Objective

{Describe in 2-3 sentences what is to be achieved and why it matters.}

## Context

{Current situation, problem that motivates the plan, relevant constraints.}

## Steps / Phases

{Ordered list of steps or phases. Include success criteria if available.}

1. ...
2. ...
3. ...

## Dependencies and risks

{What this plan needs to work and what could go wrong. Omit if not applicable.}

## Success criteria

{How we will know the plan was completed successfully.}

## Additional notes

{Any other relevant data: references, related decisions, links. Omit if there is nothing.}
```

### 4. Update the plan index

Read `plans/README.md`. If it doesn't exist, create it with this header:

```markdown
# market project plans

This directory contains the documented work plans for the project.

| File | Description | Status | Date |
|------|-------------|--------|------|
```

Add a row at the end of the table:
```
| [Plan_{words}](Plan_{words}.md) | {Full title} | Draft | {date} |
```

### 5. Confirm to the user

Report:
- The path of the created file
- The three-word name chosen and why it summarizes the plan well
- A 1-2 line summary of what was recorded

Don't show the full contents unless the user asks for it.
