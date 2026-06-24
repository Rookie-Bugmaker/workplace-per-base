# HrEmpLatestArchive 查询与导出 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 `hr_emp_latest_archive` 表新增完整的查询与导出功能（Entity / Mapper / Service / ServiceImpl / Controller），逻辑与现有 `HrEmpHcmArchive` 模块完全一致。

**Architecture:** 采用"完整复制 + 批量重命名"方案。将现有 `HrEmpHcmArchive` 的 5 个源文件逐一复制，通过文本替换将类名、表名、API 路径和注释中的"历史档案"改为"最新档案"。不修改原有模块任何代码，不引入 DTO/VO/XML mapper。

**Tech Stack:** Spring Boot + MyBatis-Plus-Join + EasyExcel + Swagger/OpenAPI v3 + Lombok

**Spec 文件:** `docs/superpowers/specs/2026-06-24-hr-emp-latest-archive-design.md`

---

## File Structure

| 层级 | 源文件（只读，不修改） | 新建文件 |
|------|------------------------|----------|
| Entity | `src/main/java/com/crrc/entity/HrEmpHcmArchive.java` | `src/main/java/com/crrc/entity/HrEmpLatestArchive.java` |
| Mapper | `src/main/java/com/crrc/mapper/HrEmpHcmArchiveMapper.java` | `src/main/java/com/crrc/mapper/HrEmpLatestArchiveMapper.java` |
| Service | `src/main/java/com/crrc/service/HrEmpHcmArchiveService.java` | `src/main/java/com/crrc/service/HrEmpLatestArchiveService.java` |
| ServiceImpl | `src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java` | `src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java` |
| Controller | `src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java` | `src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java` |

**替换规则（每个文件统一应用）：**

| # | 查找 | 替换为 | 影响范围 |
|---|------|--------|----------|
| 1 | `HrEmpHcmArchive` | `HrEmpLatestArchive` | 类名、import、类型引用、方法调用 |
| 2 | `hr_emp_hcm_archive` | `hr_emp_latest_archive` | @TableName 注解值 |
| 3 | `hrEmpHcmArchive` | `hrEmpLatestArchive` | @RequestMapping、变量名、方法名 |
| 4 | `员工历史档案数据` | `员工最新档案数据` | Javadoc、@Tag、@Operation、日志 |
| 5 | `员工历史档案` | `员工最新档案` | Javadoc、日志、AuditLog |
| 6 | `历史档案` | `最新档案` | Entity 类级 Javadoc |

**替换顺序：** 必须按 1→2→3→4→5→6 顺序执行，避免交叉替换（如规则 1 不会影响规则 2 的小写形式）。

---

## Task 1: 创建 Entity — HrEmpLatestArchive.java

**Files:**
- Create: `src/main/java/com/crrc/entity/HrEmpLatestArchive.java`（从 `HrEmpHcmArchive.java` 复制并替换）

- [ ] **Step 1: 复制源文件**

```bash
cp src/main/java/com/crrc/entity/HrEmpHcmArchive.java src/main/java/com/crrc/entity/HrEmpLatestArchive.java
```

- [ ] **Step 2: 执行文本替换（6 条 sed 命令，按顺序）**

```bash
FILE="src/main/java/com/crrc/entity/HrEmpLatestArchive.java"

# 规则1：类名 HrEmpHcmArchive → HrEmpLatestArchive
sed -i 's/HrEmpHcmArchive/HrEmpLatestArchive/g' "$FILE"

# 规则2：表名 hr_emp_hcm_archive → hr_emp_latest_archive
sed -i 's/hr_emp_hcm_archive/hr_emp_latest_archive/g' "$FILE"

# 规则3：变量/路径 hrEmpHcmArchive → hrEmpLatestArchive（Entity 中无此模式，但保持统一）
sed -i 's/hrEmpHcmArchive/hrEmpLatestArchive/g' "$FILE"

# 规则4：全短语"员工历史档案数据" → "员工最新档案数据"
sed -i 's/员工历史档案数据/员工最新档案数据/g' "$FILE"

# 规则5：短语"员工历史档案" → "员工最新档案"（不含"数据"后缀的场合）
sed -i 's/员工历史档案/员工最新档案/g' "$FILE"

# 规则6：类级 Javadoc 中的"历史档案" → "最新档案"
sed -i 's/历史档案/最新档案/g' "$FILE"
```

**替换后验证要点：**
- `@TableName("hr_emp_latest_archive")` ✓
- `class HrEmpLatestArchive extends BaseEntity implements Serializable` ✓
- `@TableId(value = "\"DA1\"", type = IdType.AUTO)` — DA 字段列名不变 ✓
- `@ExcelProperty("DA1")` 等导出注解不变 ✓
- 所有 98 个 DA 字段 + 非 DA 字段定义不变 ✓
- Javadoc 描述为"员工最新档案数据实体类" ✓
- `@Schema(description = "员工最新档案数据")` ✓

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/crrc/entity/HrEmpLatestArchive.java
git commit -m "feat(entity): add HrEmpLatestArchive entity mapping to hr_emp_latest_archive table"
```

---

## Task 2: 创建 Mapper — HrEmpLatestArchiveMapper.java

**Files:**
- Create: `src/main/java/com/crrc/mapper/HrEmpLatestArchiveMapper.java`

- [ ] **Step 1: 创建文件，内容如下**

```java
package com.crrc.mapper;

import com.crrc.entity.HrEmpLatestArchive;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工最新档案数据 Mapper 接口
 *
 * <p>该接口继承 MPJBaseMapper，获得 MyBatis-Plus 的所有基础查询能力，包括：
 * <ul>
 *     <li>selectList(Wrapper) - 根据条件查询列表</li>
 *     <li>selectPage(IPage, Wrapper) - 根据条件分页查询</li>
 *     <li>selectOne(Wrapper) - 根据条件查询单条记录</li>
 *     <li>selectCount(Wrapper) - 根据条件统计数量</li>
 * </ul>
 * </p>
 *
 * <p>所有查询操作均通过 ServiceImpl 使用 LambdaQueryWrapper 构建，
 * 不使用 XML 映射文件，符合项目规范。</p>
 *
 * @author yukun
 * @since 2026-06-24
 */
@Mapper
public interface HrEmpLatestArchiveMapper extends MPJBaseMapper<HrEmpLatestArchive> {

    // 继承自 MPJBaseMapper 的默认方法已满足所有查询需求
    // 无需额外定义自定义方法

}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/crrc/mapper/HrEmpLatestArchiveMapper.java
git commit -m "feat(mapper): add HrEmpLatestArchiveMapper extending MPJBaseMapper"
```

---

## Task 3: 创建 Service 接口 — HrEmpLatestArchiveService.java

**Files:**
- Create: `src/main/java/com/crrc/service/HrEmpLatestArchiveService.java`

- [ ] **Step 1: 创建文件，内容如下**

```java
package com.crrc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpLatestArchive;

import java.util.List;

/**
 * 员工最新档案数据 服务接口
 *
 * <p>该接口定义了员工最新档案数据的查询方法，继承 IService 获得 MyBatis-Plus
 * 的标准 CRUD 方法。本模块仅提供查询功能，不提供增删改操作。</p>
 *
 * <p>查询方法包括：
 * <ul>
 *     <li>getByDa1 - 根据档案号（DA1）精确查询单条记录</li>
 *     <li>queryByCondition - 根据条件查询列表（不分页）</li>
 *     <li>pageByCondition - 根据条件分页查询</li>
 * </ul>
 * </p>
 *
 * <p>所有查询方法都支持动态条件拼装，根据传入实体对象的非空字段自动构建查询条件。</p>
 *
 * @author yukun
 * @since 2026-06-24
 */
public interface HrEmpLatestArchiveService extends IService<HrEmpLatestArchive> {

    /**
     * 根据档案号精确查询
     *
     * <p>使用档案号（DA1 字段）进行精确查询，
     * 返回匹配的单条员工最新档案记录。</p>
     *
     * @param da1 员工档案号（DA1字段），不能为空
     * @return 员工最新档案信息，如果不存在返回 null
     */
    HrEmpLatestArchive getByDa1(String da1);

    /**
     * 条件查询（不分页）
     *
     * <p>根据传入的实体对象，自动拼装非空字段的查询条件，返回所有匹配的员工最新档案列表。
     * 支持的查询条件包括：
     * <ul>
     *     <li>da1 - 档案号（模糊查询）</li>
     *     <li>da3 - 姓名（模糊查询）</li>
     *     <li>da2 - 所在部门（模糊查询）</li>
     *     <li>da5 - 性别（精确查询）</li>
     *     <li>da6 - 民族（精确查询）</li>
     *     <li>da7 - 籍贯（模糊查询）</li>
     *     <li>da4 - 出生日期（精确查询）</li>
     *     <li>da24 - 参加工作时间（精确查询）</li>
     *     <li>da52 - 政治面貌（精确查询）</li>
     *     <li>da9 - 第一学历（精确查询）</li>
     * </ul>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。
     *                    如果传入 null，则查询所有数据
     * @return 匹配的员工最新档案列表，如果没有匹配数据返回空列表
     */
    List<HrEmpLatestArchive> queryByCondition(HrEmpLatestArchive queryEntity);

    /**
     * 条件查询（分页）
     *
     * <p>根据传入的实体对象，自动拼装非空字段的查询条件，并返回分页结果。
     * 支持的查询条件与 queryByCondition 方法相同。</p>
     *
     * @param page 分页参数，包含当前页码（current）和每页大小（size）
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。
     *                    如果传入 null，则查询所有数据
     * @return 分页结果，包含总记录数（total）、当前页数据（records）等
     */
    IPage<HrEmpLatestArchive> pageByCondition(IPage<HrEmpLatestArchive> page, HrEmpLatestArchive queryEntity);
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/crrc/service/HrEmpLatestArchiveService.java
git commit -m "feat(service): add HrEmpLatestArchiveService interface with 3 query methods"
```

---

## Task 4: 创建 ServiceImpl — HrEmpLatestArchiveServiceImpl.java

**Files:**
- Create: `src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java`（从 `HrEmpHcmArchiveServiceImpl.java` 复制并替换）

- [ ] **Step 1: 复制源文件**

```bash
cp src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java
```

- [ ] **Step 2: 执行文本替换（6 条 sed 命令，按顺序）**

```bash
FILE="src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java"

# 规则1：类名 HrEmpHcmArchive → HrEmpLatestArchive
sed -i 's/HrEmpHcmArchive/HrEmpLatestArchive/g' "$FILE"

# 规则2：表名 hr_emp_hcm_archive → hr_emp_latest_archive（ServiceImpl 中无此模式，保持统一）
sed -i 's/hr_emp_hcm_archive/hr_emp_latest_archive/g' "$FILE"

# 规则3：变量/路径 hrEmpHcmArchive → hrEmpLatestArchive
sed -i 's/hrEmpHcmArchive/hrEmpLatestArchive/g' "$FILE"

# 规则4：全短语"员工历史档案数据" → "员工最新档案数据"
sed -i 's/员工历史档案数据/员工最新档案数据/g' "$FILE"

# 规则5：短语"员工历史档案" → "员工最新档案"
sed -i 's/员工历史档案/员工最新档案/g' "$FILE"

# 规则6：类级 Javadoc 中的"历史档案" → "最新档案"
sed -i 's/历史档案/最新档案/g' "$FILE"
```

**替换后验证要点：**
- `class HrEmpLatestArchiveServiceImpl extends WorkServiceImpl<HrEmpLatestArchiveMapper, HrEmpLatestArchive>` ✓
- `implements HrEmpLatestArchiveService` ✓
- `LambdaQueryWrapper<HrEmpLatestArchive>` 泛型参数正确 ✓
- `HrEmpLatestArchive::getDa1` 等方法引用正确 ✓
- `buildQueryWrapper` 中 10 个查询条件（like/eq）逻辑不变 ✓
- 空值处理语义不变（isBlank → return null / new entity）✓
- Javadoc 描述为"员工最新档案数据 服务实现类" ✓
- `@since 2026-06-24`（需手动更新，sed 不涉及）

- [ ] **Step 3: 更新 @since 日期**

```bash
sed -i 's/@since 2026-06-23/@since 2026-06-24/g' "$FILE"
```

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java
git commit -m "feat(service): add HrEmpLatestArchiveServiceImpl with query and buildQueryWrapper logic"
```

---

## Task 5: 创建 Controller — HrEmpLatestArchiveController.java

**Files:**
- Create: `src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java`（从 `HrEmpHcmArchiveController.java` 复制并替换）

- [ ] **Step 1: 复制源文件**

```bash
cp src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java
```

- [ ] **Step 2: 执行文本替换（6 条 sed 命令，按顺序）**

```bash
FILE="src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java"

# 规则1：类名 HrEmpHcmArchive → HrEmpLatestArchive
sed -i 's/HrEmpHcmArchive/HrEmpLatestArchive/g' "$FILE"

# 规则2：表名 hr_emp_hcm_archive → hr_emp_latest_archive（Controller 中无此模式，保持统一）
sed -i 's/hr_emp_hcm_archive/hr_emp_latest_archive/g' "$FILE"

# 规则3：API 路径和变量名 hrEmpHcmArchive → hrEmpLatestArchive
sed -i 's/hrEmpHcmArchive/hrEmpLatestArchive/g' "$FILE"

# 规则4：全短语"员工历史档案数据" → "员工最新档案数据"
sed -i 's/员工历史档案数据/员工最新档案数据/g' "$FILE"

# 规则5：短语"员工历史档案" → "员工最新档案"
sed -i 's/员工历史档案/员工最新档案/g' "$FILE"

# 规则6：类级 Javadoc 中的"历史档案" → "最新档案"
sed -i 's/历史档案/最新档案/g' "$FILE"
```

**替换后验证要点：**
- `@RequestMapping("/hrEmpLatestArchive")` ✓
- `@Tag(name = "员工最新档案数据", description = "员工最新档案数据查询接口")` ✓
- `private HrEmpLatestArchiveServiceImpl hrEmpLatestArchiveService` ✓
- 4 个接口路径不变：`/page`、`/query`、`/get`、`/export` ✓
- `@AuditLog` 注解值更新为"员工最新档案" ✓
- `@Operation` 注解 summary 更新 ✓
- 导出逻辑（EasyExcel、ExcelTypeEnum、Content-Type）不变 ✓
- `EasyExcelFactory.write(..., HrEmpLatestArchive.class)` 导出类引用正确 ✓
- `@since 2026-06-24`

- [ ] **Step 3: 更新 @since 日期**

```bash
sed -i 's/@since 2026-06-23/@since 2026-06-24/g' "$FILE"
```

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java
git commit -m "feat(controller): add HrEmpLatestArchiveController with page/query/get/export endpoints"
```

---

## Task 6: 编译验证

**Files:**
- 无新建文件，仅验证编译通过

- [ ] **Step 1: 检查所有新建文件是否存在**

```bash
ls -la src/main/java/com/crrc/entity/HrEmpLatestArchive.java \
       src/main/java/com/crrc/mapper/HrEmpLatestArchiveMapper.java \
       src/main/java/com/crrc/service/HrEmpLatestArchiveService.java \
       src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java \
       src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java
```

Expected: 5 个文件全部列出，无 "No such file" 错误。

- [ ] **Step 2: 检查是否有遗漏的旧名称引用**

```bash
grep -rn "HcmArchive\|hcm_archive\|hcmArchive" \
  src/main/java/com/crrc/entity/HrEmpLatestArchive.java \
  src/main/java/com/crrc/mapper/HrEmpLatestArchiveMapper.java \
  src/main/java/com/crrc/service/HrEmpLatestArchiveService.java \
  src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java \
  src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java || echo "PASS: 无遗漏旧名称"
```

Expected: 输出 `PASS: 无遗漏旧名称`（grep 返回码 1 = 无匹配 = 正确）。

- [ ] **Step 3: 检查是否有残留的"历史档案"中文文本**

```bash
grep -rn "历史档案" \
  src/main/java/com/crrc/entity/HrEmpLatestArchive.java \
  src/main/java/com/crrc/mapper/HrEmpLatestArchiveMapper.java \
  src/main/java/com/crrc/service/HrEmpLatestArchiveService.java \
  src/main/java/com/crrc/service/impl/HrEmpLatestArchiveServiceImpl.java \
  src/main/java/com/crrc/controller/HrEmpLatestArchiveController.java || echo "PASS: 无残留'历史档案'"
```

Expected: 输出 `PASS: 无残留'历史档案'`。

- [ ] **Step 4: 执行 Maven 编译**

```bash
./mvnw compile -q
```

Expected: 编译成功，无错误输出。如有编译错误，根据错误信息修复对应文件后重新编译。

---

## 验证清单

完成所有 Task 后，以下功能点应与 `HrEmpHcmArchive` 完全一致：

| # | 验证项 | 预期 |
|---|--------|------|
| 1 | `POST /hrEmpLatestArchive/page` 分页查询 | 返回分页结果，默认 pageNo=1, pageSize=10 |
| 2 | `POST /hrEmpLatestArchive/query` 列表查询 | 返回全量匹配列表 |
| 3 | `GET /hrEmpLatestArchive/get?da1=xxx` 精确查询 | 返回单条或 404 |
| 4 | `POST /hrEmpLatestArchive/export?type=xlsx` 导出 | 下载 xlsx 文件 |
| 5 | `POST /hrEmpLatestArchive/export?type=csv` 导出 | 下载 csv 文件 |
| 6 | `POST /hrEmpLatestArchive/export?type=xls` 导出 | 下载 xls 文件 |
| 7 | 10 个字段的动态查询条件 | like/eq 方式与原版一致 |
| 8 | 空值处理 | 参数为空时不加入条件，getByDa1 返回 null |
| 9 | 审计日志 | 所有接口有 @AuditLog 注解 |
| 10 | Swagger 文档 | @Tag 和 @Operation 描述为"最新档案" |
