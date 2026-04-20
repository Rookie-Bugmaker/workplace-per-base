---
paths:
  - "src/main/java/**/*.java"
  - "src/test/java/**/*.java"
---

# Java backend rules

- 优先沿用现有 Spring、Service、Mapper、Domain、DTO 的组织方式。
- 提取公共方法时，方法命名必须体现业务语义，不使用模糊命名。
- 除非有明确收益且不影响语义，否则避免跨层重构和大规模搬移。
- 对旧逻辑的空值、异常、覆盖顺序、默认值处理保持一致。
- 新增辅助方法时，优先服务于“可读性提升”和“职责拆分”，不要为了抽象而抽象。