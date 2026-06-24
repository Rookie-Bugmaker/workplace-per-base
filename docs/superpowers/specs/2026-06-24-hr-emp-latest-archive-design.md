# HrEmpLatestArchive 查询与导出 设计文档

## 背景

现有备份库 `hr_emp_hcm_archive` 已实现完整的查询与导出功能（Entity → Mapper → Service → ServiceImpl → Controller）。现新增备份库 `hr_emp_latest_archive`，需要提供与 `hr_emp_hcm_archive` 完全一致的查询与导出能力。

## 目标

为 `hr_emp_latest_archive` 表新增以下功能，逻辑与 `hr_emp_hcm_archive` 完全一致：

1. 按档案号精确查询（`/get`）
2. 条件列表查询（`/query`）
3. 条件分页查询（`/page`）
4. Excel/CSV 导出（`/export`）

## 实现方案

采用 **完整复制 + 批量重命名** 方案。将现有 `HrEmpHcmArchive` 模块的 5 个文件逐一复制，仅替换类名、表名和 API 路径，其余内容（字段定义、注解、业务逻辑）均保持不变。

### 选型理由

- 用户明确要求"逻辑完全一致"，独立复制最符合语义
- 符合项目现有模式（每表一套完整代码）
- 两张表未来可能独立演进，独立代码更灵活
- 避免过度抽象引入额外复杂度

## 命名映射

| 层级 | 原文件 | 新文件 |
|------|--------|--------|
| Entity | `HrEmpHcmArchive.java` | `HrEmpLatestArchive.java` |
| Mapper | `HrEmpHcmArchiveMapper.java` | `HrEmpLatestArchiveMapper.java` |
| Service | `HrEmpHcmArchiveService.java` | `HrEmpLatestArchiveService.java` |
| ServiceImpl | `HrEmpHcmArchiveServiceImpl.java` | `HrEmpLatestArchiveServiceImpl.java` |
| Controller | `HrEmpHcmArchiveController.java` | `HrEmpLatestArchiveController.java` |

| 替换项 | 原值 | 新值 |
|--------|------|------|
| 表名 | `hr_emp_hcm_archive` | `hr_emp_latest_archive` |
| API 路径前缀 | `/hrEmpHcmArchive` | `/hrEmpLatestArchive` |

## 各层设计

### 1. Entity — `HrEmpLatestArchive`

- `@TableName("hr_emp_latest_archive")`
- 字段与 `HrEmpHcmArchive` 完全一致：
  - DA1\~DA98：98 个 String 类型数据字段
  - 非DA字段：nf, yf, cType, cBeginDate, cEndDate, cTypeOne, cTypeTwo, cTypeThree, cGrade, cZxgj, tdBeginDate, tdEndDate, da21In, da2In, a26, jobOne, jobTwo, jobThree
- `@TableId(value = "\"DA1\"", type = IdType.AUTO)` — DA1 为主键
- 所有 `@TableField` 使用双引号包裹列名（如 `"\"DA1\""`）
- `@ExcelProperty` + `@ExcelIgnoreUnannotated` 导出注解保持原样
- `@TableLogic` 逻辑删除注解保持原样
- Lombok `@Data`、`@NoArgsConstructor`、`@Accessors(chain = false)`
- Swagger `@Schema` 接口文档注解

### 2. Mapper — `HrEmpLatestArchiveMapper`

- 继承 `MPJBaseMapper<HrEmpLatestArchive>`
- `@Mapper` 注解
- 无自定义方法，无 XML mapper 文件
- 完全依赖 MPJBaseMapper 提供的默认方法

### 3. Service 接口 — `HrEmpLatestArchiveService`

- 继承 `IService<HrEmpLatestArchive>`
- 定义 3 个方法：

| 方法签名 | 说明 |
|----------|------|
| `HrEmpLatestArchive getByDa1(String da1)` | 按档案号精确查询单条 |
| `List<HrEmpLatestArchive> queryByCondition(HrEmpLatestArchive)` | 条件查询（不分页） |
| `IPage<HrEmpLatestArchive> pageByCondition(IPage, HrEmpLatestArchive)` | 条件分页查询 |

### 4. ServiceImpl — `HrEmpLatestArchiveServiceImpl`

- 继承 `WorkServiceImpl<HrEmpLatestArchiveMapper, HrEmpLatestArchive>`
- 实现 `HrEmpLatestArchiveService`

#### 方法实现

- `getByDa1(String da1)`：使用 `LambdaQueryWrapper.eq(::getDa1)`，参数为空时返回 null
- `queryByCondition(HrEmpLatestArchive)`：调用 `buildQueryWrapper()` 构建条件后执行 `selectList`
- `pageByCondition(IPage, HrEmpLatestArchive)`：调用 `buildQueryWrapper()` 构建条件后执行 `selectPage`

#### buildQueryWrapper 查询条件

| 字段 | 含义 | 查询方式 |
|------|------|----------|
| da1 | 档案号 | `like` 模糊 |
| da3 | 姓名 | `like` 模糊 |
| da2 | 部门 | `like` 模糊 |
| da5 | 性别 | `eq` 精确 |
| da6 | 民族 | `eq` 精确 |
| da7 | 籍贯 | `like` 模糊 |
| da4 | 出生日期 | `eq` 精确 |
| da24 | 参加工作时间 | `eq` 精确 |
| da52 | 政治面貌 | `eq` 精确 |
| da9 | 第一学历 | `eq` 精确 |

所有条件均为动态条件，字段为空时不加入查询。

### 5. Controller — `HrEmpLatestArchiveController`

- `@RestController`，`@RequestMapping("/hrEmpLatestArchive")`
- 注入 `HrEmpLatestArchiveServiceImpl`（直接注入实现类，与现有模式一致）

#### 接口列表

| HTTP 方法 | 路径 | 功能 | 参数 |
|-----------|------|------|------|
| POST | `/page` | 分页查询 | `pageNo`（默认1）、`pageSize`（默认10）、查询条件实体 |
| POST | `/query` | 列表查询（不分页） | 查询条件实体 |
| GET | `/get` | 按档案号精确查询 | `da1`（String） |
| POST | `/export` | 导出 Excel/CSV | 查询条件实体、导出格式（xlsx/xls/csv） |

#### 注解

- 所有接口使用 `@AuditLog` 记录审计日志
- 使用 Swagger `@Operation` / `@Tag` 注解，描述相应调整为 LatestArchive
- 导出接口支持 xlsx、xls、csv 三种格式

## 不变性保证

以下内容在复制过程中不得改变：

- 字段类型、字段名、`@TableField` 列名映射
- `@ExcelProperty` 导出注解及顺序
- `buildQueryWrapper` 中的查询方式（like/eq）和条件判断逻辑
- 空值处理语义（参数为空时不加入条件、`getByDa1` 返回 null）
- 分页默认值（pageNo=1, pageSize=10）
- 导出格式处理逻辑
- `@TableLogic` 逻辑删除行为
- `@AuditLog` 审计日志行为

## 不在范围内

- 不修改现有 `HrEmpHcmArchive` 模块的任何代码
- 不创建 DTO/VO 类（与现有模式一致，直接使用实体类）
- 不创建 XML mapper 文件（与现有模式一致，使用 `LambdaQueryWrapper`）
- 不创建数据库建表脚本（假设表已存在）