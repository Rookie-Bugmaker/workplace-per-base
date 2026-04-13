---
name: review-diff
description: Review the current git diff for Java code against Google Java Style and correctness risks. Use when checking staged or unstaged Java changes before commit or PR submission.
argument-hint: [diff-target]
disable-model-invocation: true
context: fork
agent: Explore
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash(git status *)
  - Bash(git diff *)
  - Bash(git show *)
  - Bash(git rev-parse *)
---

# Review Google Java Style Against Git Diff

Review target: $ARGUMENTS

## Scope
Review only the git diff for the requested target.
If no explicit target is provided, default to reviewing the staged diff first. If there is no staged diff, review the working tree diff.

Use these commands to collect the review input:
- staged diff: !`git diff --cached --unified=0`
- unstaged diff fallback: !`git diff --unified=0`

If the user passes an explicit argument, interpret it as one of:
- a revision range such as `origin/main...HEAD`
- a commit such as `HEAD~1`
- a file path after `--`
  Then gather the diff using a matching git diff command.

## Review standard
Use Google Java Style as the style baseline for Java code.
In addition to style, perform a project-level hardcoding review and correctness review.
Focus on concrete violations and high-signal findings.

Check at minimum:
1. wildcard imports
2. wrapped imports
3. lines that unnecessarily exceed 100 columns
4. missing braces for if/else/for/do/while
5. multiple statements on one line
6. indentation inconsistent with 2-space block indentation
7. naming or method extraction that reduces readability
8. obvious correctness risks introduced by the diff
9. null-handling, exception-handling, default-value, and precedence changes
10. unnecessary refactors mixed into the same diff
11. hardcoded sensitive values
12. hardcoded environment or infrastructure values
13. hardcoded business codes, status values, tenant/org/user identifiers
14. magic numbers or repeated string literals that should be extracted

## Hardcoding review rules
Treat the following as high-signal hardcoding findings when they appear in the diff:

### Must fix hardcoding
- credentials, secrets, tokens, private keys, access keys
- usernames, passwords, connection strings with secrets
- fixed production or test endpoints such as full URLs, IPs, ports, MQ addresses, bucket names, topic names, queue names
- hardcoded tenant IDs, org IDs, user IDs, app IDs, customer IDs, region IDs
- hardcoded file system paths or OS-specific absolute paths
- hardcoded business status/type/code values inside branching logic when an enum, constant, dictionary, or config-based source should be used
- hardcoded dates or time windows that change behavior and are not clearly intentional
- SQL fragments or permission rules assembled from hardcoded business constants when the project already has a safer abstraction

### Should fix hardcoding
- unexplained magic numbers in business logic
- repeated string literals used as keys, statuses, or discriminator values
- duplicated error codes, message keys, or cache keys that should be named constants
- ad hoc literals that reduce readability even if they do not immediately break behavior

### Do not flag as hardcoding by default
- well-named local constants with obvious one-off meaning
- protocol-required literals that are unlikely to change
- annotations, framework-required fixed names, or test data in test code unless they create real risk
- existing unchanged literals outside the diff, unless needed to explain a new issue

## Important boundaries
- Review only what is in the diff, but read surrounding files when needed for context.
- Do not suggest style changes outside the diff unless they are required to explain a finding.
- Prefer precise findings over broad commentary.
- Do not praise the code.
- Do not rewrite the whole patch unless the user explicitly asks.
- Separate style issues from correctness issues.
- If something is uncertain, say why it is uncertain.

Read [checklist.md](checklist.md) before writing the review.

## Output format
When reporting findings:
- clearly label each item as one of: style / hardcoding / correctness
- prioritize correctness and hardcoding over pure formatting
Output exactly these sections:

1. Summary
- one short paragraph
- overall risk: low / medium / high

2. Must fix
- only issues that are clearly wrong, risky, or non-compliant
- each item must include:
    - severity
    - file path
    - affected symbol or hunk
    - why it is a problem
    - suggested fix direction

3. Should fix
- worthwhile but not blocking items
- same structure as above

4. Notes
- uncertainties
- missing context
- follow-up checks worth doing

## Review quality bar
A good review:
- cites the exact file and changed area
- distinguishes style from behavior
- avoids vague statements like “can be optimized”
- avoids requesting unrelated rewrites
- flags semantic drift even if formatting looks fine