---
name: safe-refactor
description: Safely refactor complex code while strictly preserving existing behavior. Use for methods, classes, or modules that need clearer structure without changing semantics.
argument-hint: [file-or-class] [method-or-task]
disable-model-invocation: true
allowed-tools: Read Grep Glob Edit MultiEdit Write Bash(mvn test *) Bash(./mvnw test *) Bash(gradle test *) Bash(./gradlew test *)
---

# Safe Refactor

You are performing a strict semantics-preserving refactor.

Target: $ARGUMENTS

## Non-negotiable constraints
- Treat this as an equivalent refactor, not a feature rewrite.
- Do not change business rules, field meaning, return semantics, exception semantics, null-handling semantics, default-value semantics, or precedence semantics.
- If any behavior is uncertain, preserve the original behavior and explicitly report the uncertainty.
- Prefer minimal changes and avoid unrelated renames, formatting-only edits, or broad architectural restructuring.

## Refactor goals
1. Make the target code clearer by splitting responsibilities and reducing unnecessary nesting or duplication.
2. Improve readability while preserving exact behavior.
3. Keep the edit surface as small as reasonably possible.
4. Leave a clear mapping between old logic and new structure.

## Required execution flow

### Phase 1: analysis only
Do not modify code yet.
- Read the target file and directly related call sites.
- Identify the responsibilities inside the target method/class/module.
- List behavior invariants that must stay unchanged.
- Identify duplication, deep nesting, mixed responsibilities, and unclear control flow.
- Read [checklist.md](checklist.md).
- Produce:
    1. responsibility breakdown
    2. invariants to preserve
    3. minimal refactor plan
    4. risks and verification points

### Phase 2: implementation
Only after the analysis is complete, implement the refactor.
- Keep edits minimal.
- Extract helper methods only when they improve clarity and preserve behavior.
- Keep a clear correspondence from old logic to new methods or blocks.
- Avoid changing unrelated code.

### Phase 3: verification
After editing:
- compare old and new logic branch by branch
- verify null handling, exceptions, defaults, and precedence
- run relevant tests if available
- if tests are unavailable, produce a manual validation checklist covering the main branches

## Output format
1. Responsibility breakdown
2. Invariants
3. Refactor plan
4. Files changed
5. Remaining risks
6. Verification results