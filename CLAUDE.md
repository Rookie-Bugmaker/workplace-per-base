# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Refactor policy
- 默认将业务代码重构视为等价重构。
- 未经明确要求，不得改变业务语义、字段含义、返回值语义、异常语义、空值处理语义、默认值语义、优先级语义。
- 如果某段逻辑是否可改不明确，默认保持不动，并明确标注风险点。
- 优先采用最小改动面，避免无关重命名、无关格式化、无关抽象。

## Verification
- 复杂重构必须先分析职责与不变量，再实施修改。
- 修改完成后，必须显式进行前后语义一致性检查。
- 如果项目中存在测试或可运行验证命令，优先执行相关验证。
- 如果缺少自动化测试，至少给出覆盖主要分支的校验清单。

## Workflow
- 遇到复杂方法、复杂类或复杂模块的等价重构时，优先使用 `/safe-refactor` skill。
-
## Review workflow
- 对 Java 代码的提交评审，优先使用 `/review-diff`。
- review 以当前 git diff 为主，不做整仓库泛化点评。
- review 时必须区分 style、hardcoding 与 correctness 三类问题。
- 对硬编码保持高敏感度，尤其是敏感信息、环境配置、租户身份标识、业务状态码、魔法数字和重复字面量。
- 没有明确证据时，不要臆断问题；要说明不确定性来源。

## Bug fix policy
- For non-trivial Java bug fixes, prefer `/fix-bug-safe`.
- A bug fix is not complete unless an automated Java test captures the bug and passes after the fix.
- Keep bug-fix changes narrow. Do not mix broad refactors into a bug fix.
- Preserve null-handling, exception semantics, default-value semantics, and precedence semantics unless the requested fix explicitly changes them.
- Comments should explain non-obvious intent, edge cases, or invariants only.

## Java verification
- Prefer the repository's existing Maven Wrapper or Gradle Wrapper when present.
- Run the narrowest relevant Java test first, then nearby tests for regression coverage.