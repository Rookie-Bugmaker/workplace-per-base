# HrEmpHcmArchive 导出接口设计

## 背景

`HrEmpHcmArchiveController` 目前提供分页查询、列表查询和单条查询三个只读接口，缺少数据导出能力。需要新增一个导出接口，支持根据查询条件导出数据，无查询条件时导出全部数据。

## 需求

- 在 `HrEmpHcmArchiveController` 中新增导出接口
- 导出接口接受与分页查询相同的查询条件（`HrEmpHcmArchive` 实体）
- 查询条件为空时，导出全部数据
- 导出全部 113 个业务字段，列名直接使用字段名（`da1`、`da2` 等）
- 导出格式为 xlsx
- 遵循项目中已有的导出接口模式（EasyExcel 同步导出）

## 设计决策

**实体注解方案（方案一）**：为 `HrEmpHcmArchive` 的全部 113 个字段添加 `@ExcelProperty` 注解，类级别添加 `@ExcelIgnoreUnannotated`。

选择理由：用户明确要求列名直接使用字段名，不添加中文映射。使用 `@ExcelProperty("da1")` 注解与项目标准模式一致，EasyExcel 自动从注解提取列头和顺序，代码最简洁。

## 实现细节

### 1. 实体修改 — HrEmpHcmArchive

**文件**：`src/main/java/com/crrc/entity/HrEmpHcmArchive.java`

- 类级别添加 `@ExcelIgnoreUnannotated`
- 全部 113 个业务字段添加 `@ExcelProperty("字段名")`
- BaseEntity 继承字段（`createBy`、`createTime`、`updateBy`、`updateTime`、`deleted`、`dynamicFields` 等）不添加注解，自动被 `@ExcelIgnoreUnannotated` 排除
- 不指定 `index`，EasyExcel 按字段声明顺序输出

示例：

```java
@ExcelIgnoreUnannotated
@TableName("hr_emp_hcm_archive")
public class HrEmpHcmArchive extends BaseEntity {

    @ExcelProperty("da1")
    @TableId(value = "da1", type = IdType.AUTO)
    @Schema(description = "DA1-档案号")
    private String da1;

    @ExcelProperty("da2")
    @TableField("da2")
    @Schema(description = "DA2-所在部门")
    private String da2;

    // ... 其余 111 个字段同理
}
```

### 2. Service 接口 — HrEmpHcmArchiveService

**文件**：`src/main/java/com/crrc/service/HrEmpHcmArchiveService.java`

新增方法：

```java
/**
 * 根据查询条件导出员工历史档案数据
 *
 * @param queryEntity 查询条件实体，非空字段作为过滤条件
 * @return 符合条件的档案数据列表
 */
List<HrEmpHcmArchive> exportByCondition(HrEmpHcmArchive queryEntity);
```

### 3. Service 实现 — HrEmpHcmArchiveServiceImpl

**文件**：`src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java`

新增实现方法，复用已有的 `buildQueryWrapper()` 私有方法：

```java
@Override
public List<HrEmpHcmArchive> exportByCondition(HrEmpHcmArchive queryEntity) {
    LambdaQueryWrapper<HrEmpHcmArchive> wrapper = buildQueryWrapper(queryEntity);
    return this.list(wrapper);
}
```

### 4. Controller 导出接口 — HrEmpHcmArchiveController

**文件**：`src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java`

新增 `POST /hrEmpHcmArchive/export` 接口。

**关键设计点：** 数据查询在设置 response 头之前执行，确保查询异常时能正常返回 JSON 错误响应（此时 response 未提交），写入异常则交给框架处理（与其他导出方法的 `throws IOException` 签名一致）。

```java
@PostMapping("/export")
@Operation(summary = "导出员工历史档案数据")
@AuditLog(value = "导出员工历史档案数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
public ResponseResult<List<HrEmpHcmArchive>> exportHrEmpHcmArchive(
        @Validated @RequestBody(required = false) HrEmpHcmArchive queryEntity,
        HttpServletResponse response) throws IOException {

    queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

    List<HrEmpHcmArchive> dataList;
    try {
        dataList = hrEmpHcmArchiveService.exportByCondition(queryEntity);
    } catch (Exception e) {
        log.error("导出员工历史档案数据-查询异常", e);
        return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
    }

    // 查询成功后，设置响应头并写入 Excel
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");
    String fileName = URLEncoder.encode(
            "员工历史档案数据-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()),
            "UTF-8").replaceAll("\\+", "%20");
    response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

    EasyExcel.write(response.getOutputStream(), HrEmpHcmArchive.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .autoCloseStream(true)
            .excelType(ExcelTypeEnum.XLSX)
            .sheet("员工历史档案数据")
            .doWrite(dataList);

    return null;
}
```

## 数据流

```
客户端 POST /hrEmpHcmArchive/export {queryEntity?}
  → Controller.exportHrEmpHcmArchive()
    → 设置 response 头（xlsx + 文件名）
    → Service.exportByCondition(queryEntity)
      → buildQueryWrapper(queryEntity) → LambdaQueryWrapper
      → this.list(wrapper) → List<HrEmpHcmArchive>
    → EasyExcel.write(stream, HrEmpHcmArchive.class)
      → 从 @ExcelProperty 读取列头
      → 从 @ExcelIgnoreUnannotated 排除未注解字段
      → 写入 xlsx 文件流
  → 客户端下载 xlsx 文件
```

## 异常处理

- 查询条件为空时，`buildQueryWrapper` 构建空 wrapper，查询全部数据
- 查询阶段异常：在 response 头提交前捕获，返回 `ResponseResult(500, "查询异常: ...")` 的 JSON 响应
- 写入阶段异常（如 IO 中断）：response 头已提交，无法返回 JSON，异常向上抛出交给框架统一处理（方法签名 `throws IOException`，与项目中其他导出方法一致）
- 使用 `@AuditLog` 记录导出操作日志，`operateType = OperateType.TYPE_EXPORT`

## 影响范围

- `HrEmpHcmArchive.java`：添加类级别 `@ExcelIgnoreUnannotated` + 113 个字段的 `@ExcelProperty` 注解
- `HrEmpHcmArchiveService.java`：新增 `exportByCondition()` 方法签名
- `HrEmpHcmArchiveServiceImpl.java`：新增 `exportByCondition()` 实现
- `HrEmpHcmArchiveController.java`：新增 `/export` 接口

## 不涉及

- 不修改现有查询逻辑（`buildQueryWrapper`、`queryByCondition`、`pageByCondition`）
- 不修改 BaseEntity
- 不新增 DTO/VO 类
- 不修改 Mapper 层
- 不新增异步导出机制
