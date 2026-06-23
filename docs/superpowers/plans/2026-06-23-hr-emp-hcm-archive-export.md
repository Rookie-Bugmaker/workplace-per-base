# HrEmpHcmArchive 导出接口实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 HrEmpHcmArchiveController 中新增 POST /export 导出接口，支持按查询条件导出员工历史档案数据为 xlsx 文件。

**Architecture:** 在实体类上添加 `@ExcelIgnoreUnannotated` + `@ExcelProperty` 注解定义 Excel 列，Service 层新增 `exportByCondition()` 方法复用已有查询逻辑，Controller 层新增导出端点使用 EasyExcel 写入 xlsx。

**Tech Stack:** Spring Boot, MyBatis-Plus, EasyExcel (com.alibaba.excel), javax.servlet

---

## 文件结构

| 操作 | 文件路径 | 职责 |
|------|---------|------|
| 修改 | `src/main/java/com/crrc/entity/HrEmpHcmArchive.java` | 添加 `@ExcelIgnoreUnannotated` + 116 个字段的 `@ExcelProperty` |
| 修改 | `src/main/java/com/crrc/service/HrEmpHcmArchiveService.java` | 新增 `exportByCondition()` 方法签名 |
| 修改 | `src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java` | 新增 `exportByCondition()` 实现 |
| 修改 | `src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java` | 新增 `/export` 接口 |

---

### Task 1: 实体类添加 EasyExcel 注解

**Files:**
- Modify: `src/main/java/com/crrc/entity/HrEmpHcmArchive.java`

- [ ] **Step 1: 添加 import 和类级别注解**

在 `HrEmpHcmArchive.java` 顶部添加 import：

```java
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
```

在类声明上方添加 `@ExcelIgnoreUnannotated`：

```java
@Accessors(chain = false)
@TableName("hr_emp_hcm_archive")
@Schema(description = "员工历史档案数据")
@ExcelIgnoreUnannotated
@Data
@NoArgsConstructor
public class HrEmpHcmArchive extends BaseEntity implements Serializable {
```

- [ ] **Step 2: 为 deleted 字段后面的所有业务字段添加 @ExcelProperty**

`deleted` 字段（逻辑删除标记）**不添加** `@ExcelProperty`，保持原样。

为 `da1` 到 `da98`（98个字段）的每个字段添加 `@ExcelProperty("字段名")`。每个字段在现有注解之前添加一行。示例：

```java
    /**
     * DA1 - 档案号
     */
    @ExcelProperty("da1")
    @TableId(value = "id", type = IdType.AUTO)
    @TableField("da1")
    @Schema(description = "DA1 - 档案号")
    private String da1;

    /**
     * DA2 - 所在部门
     */
    @ExcelProperty("da2")
    @TableField("da2")
    @Schema(description = "DA2 - 所在部门")
    private String da2;

    /**
     * DA3 - 姓名
     */
    @ExcelProperty("da3")
    @TableField("da3")
    @Schema(description = "DA3 - 姓名")
    private String da3;

    /**
     * DA4 - 出生日期
     */
    @ExcelProperty("da4")
    @TableField("da4")
    @Schema(description = "DA4 - 出生日期")
    private String da4;

    /**
     * DA5 - 性别
     */
    @ExcelProperty("da5")
    @TableField("da5")
    @Schema(description = "DA5 - 性别")
    private String da5;

    /**
     * DA6 - 民族
     */
    @ExcelProperty("da6")
    @TableField("da6")
    @Schema(description = "DA6 - 民族")
    private String da6;

    /**
     * DA7 - 籍贯
     */
    @ExcelProperty("da7")
    @TableField("da7")
    @Schema(description = "DA7 - 籍贯")
    private String da7;

    /**
     * DA8 - 身份证号
     */
    @ExcelProperty("da8")
    @TableField("da8")
    @Schema(description = "DA8 - 身份证号")
    private String da8;

    /**
     * DA9 - 第一学历
     */
    @ExcelProperty("da9")
    @TableField("da9")
    @Schema(description = "DA9 - 第一学历")
    private String da9;

    /**
     * DA10 - 第一学历入学日期
     */
    @ExcelProperty("da10")
    @TableField("da10")
    @Schema(description = "DA10 - 第一学历入学日期")
    private String da10;

    /**
     * DA11 - 第一学历专业
     */
    @ExcelProperty("da11")
    @TableField("da11")
    @Schema(description = "DA11 - 第一学历专业")
    private String da11;

    /**
     * DA12 - 第一学历毕业院校
     */
    @ExcelProperty("da12")
    @TableField("da12")
    @Schema(description = "DA12 - 第一学历毕业院校")
    private String da12;

    /**
     * DA13 - 第一学历教育类型
     */
    @ExcelProperty("da13")
    @TableField("da13")
    @Schema(description = "DA13 - 第一学历教育类型")
    private String da13;

    /**
     * DA14 - 第一学历学位授予日期
     */
    @ExcelProperty("da14")
    @TableField("da14")
    @Schema(description = "DA14 - 第一学历学位授予日期")
    private String da14;

    /**
     * DA15 - 第一学历学位
     */
    @ExcelProperty("da15")
    @TableField("da15")
    @Schema(description = "DA15 - 第一学历学位")
    private String da15;

    /**
     * DA16 - 第二学历
     */
    @ExcelProperty("da16")
    @TableField("da16")
    @Schema(description = "DA16 - 第二学历")
    private String da16;

    /**
     * DA17 - 第二学历入学日期
     */
    @ExcelProperty("da17")
    @TableField("da17")
    @Schema(description = "DA17 - 第二学历入学日期")
    private String da17;

    /**
     * DA18 - 第二学历专业
     */
    @ExcelProperty("da18")
    @TableField("da18")
    @Schema(description = "DA18 - 第二学历专业")
    private String da18;

    /**
     * DA19 - 第二学历毕业院校
     */
    @ExcelProperty("da19")
    @TableField("da19")
    @Schema(description = "DA19 - 第二学历毕业院校")
    private String da19;

    /**
     * DA20 - 第二学历教育类型
     */
    @ExcelProperty("da20")
    @TableField("da20")
    @Schema(description = "DA20 - 第二学历教育类型")
    private String da20;

    /**
     * DA21 - 第二学历学位授予日期
     */
    @ExcelProperty("da21")
    @TableField("da21")
    @Schema(description = "DA21 - 第二学历学位授予日期")
    private String da21;

    /**
     * DA22 - 第二学历学位
     */
    @ExcelProperty("da22")
    @TableField("da22")
    @Schema(description = "DA22 - 第二学历学位")
    private String da22;

    /**
     * DA23 - 入职方式
     */
    @ExcelProperty("da23")
    @TableField("da23")
    @Schema(description = "DA23 - 入职方式")
    private String da23;

    /**
     * DA24 - 参加工作时间
     */
    @ExcelProperty("da24")
    @TableField("da24")
    @Schema(description = "DA24 - 参加工作时间")
    private String da24;

    /**
     * DA25 - 入职日期
     */
    @ExcelProperty("da25")
    @TableField("da25")
    @Schema(description = "DA25 - 入职日期")
    private String da25;

    /**
     * DA26 - 年龄
     */
    @ExcelProperty("da26")
    @TableField("da26")
    @Schema(description = "DA26 - 年龄")
    private String da26;

    /**
     * DA27 - 合同年限
     */
    @ExcelProperty("da27")
    @TableField("da27")
    @Schema(description = "DA27 - 合同年限")
    private String da27;

    /**
     * DA28 - 岗位名称开始日期
     */
    @ExcelProperty("da28")
    @TableField("da28")
    @Schema(description = "DA28 - 岗位名称开始日期")
    private String da28;

    /**
     * DA29 - 岗位名称结束日期
     */
    @ExcelProperty("da29")
    @TableField("da29")
    @Schema(description = "DA29 - 岗位名称结束日期")
    private String da29;

    /**
     * DA30 - 第一职称
     */
    @ExcelProperty("da30")
    @TableField("da30")
    @Schema(description = "DA30 - 第一职称")
    private String da30;

    /**
     * DA31 - 第一职称评定日期
     */
    @ExcelProperty("da31")
    @TableField("da31")
    @Schema(description = "DA31 - 第一职称评定日期")
    private String da31;

    /**
     * DA32 - 第二职称
     */
    @ExcelProperty("da32")
    @TableField("da32")
    @Schema(description = "DA32 - 第二职称")
    private String da32;

    /**
     * DA33 - 第二职称评定日期
     */
    @ExcelProperty("da33")
    @TableField("da33")
    @Schema(description = "DA33 - 第二职称评定日期")
    private String da33;

    /**
     * DA34 - 身份类型
     */
    @ExcelProperty("da34")
    @TableField("da34")
    @Schema(description = "DA34 - 身份类型")
    private String da34;

    /**
     * DA35 - 人员分类
     */
    @ExcelProperty("da35")
    @TableField("da35")
    @Schema(description = "DA35 - 人员分类")
    private String da35;

    /**
     * DA36 - 人员分类2
     */
    @ExcelProperty("da36")
    @TableField("da36")
    @Schema(description = "DA36 - 人员分类2")
    private String da36;

    /**
     * DA37 - 在岗状态
     */
    @ExcelProperty("da37")
    @TableField("da37")
    @Schema(description = "DA37 - 在岗状态")
    private String da37;

    /**
     * DA38 - 人员类别
     */
    @ExcelProperty("da38")
    @TableField("da38")
    @Schema(description = "DA38 - 人员类别")
    private String da38;

    /**
     * DA39 - 技能等级
     */
    @ExcelProperty("da39")
    @TableField("da39")
    @Schema(description = "DA39 - 技能等级")
    private String da39;

    /**
     * DA40 - 技能级别
     */
    @ExcelProperty("da40")
    @TableField("da40")
    @Schema(description = "DA40 - 技能级别")
    private String da40;

    /**
     * DA41 - 技能等级评定日期
     */
    @ExcelProperty("da41")
    @TableField("da41")
    @Schema(description = "DA41 - 技能等级评定日期")
    private String da41;

    /**
     * DA42 - 职称级别
     */
    @ExcelProperty("da42")
    @TableField("da42")
    @Schema(description = "DA42 - 职称级别")
    private String da42;

    /**
     * DA43 - 职称级别评定日期
     */
    @ExcelProperty("da43")
    @TableField("da43")
    @Schema(description = "DA43 - 职称级别评定日期")
    private String da43;

    /**
     * DA44 - 职称级别状态
     */
    @ExcelProperty("da44")
    @TableField("da44")
    @Schema(description = "DA44 - 职称级别状态")
    private String da44;

    /**
     * DA45 - 婚姻状况
     */
    @ExcelProperty("da45")
    @TableField("da45")
    @Schema(description = "DA45 - 婚姻状况")
    private String da45;

    /**
     * DA46 - 结婚日期
     */
    @ExcelProperty("da46")
    @TableField("da46")
    @Schema(description = "DA46 - 结婚日期")
    private String da46;

    /**
     * DA47 - 健康状况
     */
    @ExcelProperty("da47")
    @TableField("da47")
    @Schema(description = "DA47 - 健康状况")
    private String da47;

    /**
     * DA48 - 身高
     */
    @ExcelProperty("da48")
    @TableField("da48")
    @Schema(description = "DA48 - 身高")
    private String da48;

    /**
     * DA49 - 体重
     */
    @ExcelProperty("da49")
    @TableField("da49")
    @Schema(description = "DA49 - 体重")
    private String da49;

    /**
     * DA50 - 血型
     */
    @ExcelProperty("da50")
    @TableField("da50")
    @Schema(description = "DA50 - 血型")
    private String da50;

    /**
     * DA51 - 家庭住址
     */
    @ExcelProperty("da51")
    @TableField("da51")
    @Schema(description = "DA51 - 家庭住址")
    private String da51;

    /**
     * DA52 - 政治面貌
     */
    @ExcelProperty("da52")
    @TableField("da52")
    @Schema(description = "DA52 - 政治面貌")
    private String da52;

    /**
     * DA53 - 入党日期
     */
    @ExcelProperty("da53")
    @TableField("da53")
    @Schema(description = "DA53 - 入党日期")
    private String da53;

    /**
     * DA54 - 户籍类型
     */
    @ExcelProperty("da54")
    @TableField("da54")
    @Schema(description = "DA54 - 户籍类型")
    private String da54;

    /**
     * DA55 - 户籍所在地
     */
    @ExcelProperty("da55")
    @TableField("da55")
    @Schema(description = "DA55 - 户籍所在地")
    private String da55;

    /**
     * DA56 - 户籍所在地详细
     */
    @ExcelProperty("da56")
    @TableField("da56")
    @Schema(description = "DA56 - 户籍所在地详细")
    private String da56;

    /**
     * DA57 - 现居住地
     */
    @ExcelProperty("da57")
    @TableField("da57")
    @Schema(description = "DA57 - 现居住地")
    private String da57;

    /**
     * DA58 - 手机号码
     */
    @ExcelProperty("da58")
    @TableField("da58")
    @Schema(description = "DA58 - 手机号码")
    private String da58;

    /**
     * DA59 - 工资/档次
     */
    @ExcelProperty("da59")
    @TableField("da59")
    @Schema(description = "DA59 - 工资/档次")
    private String da59;

    /**
     * DA60 - 年龄（档案年龄）
     */
    @ExcelProperty("da60")
    @TableField("da60")
    @Schema(description = "DA60 - 年龄（档案年龄）")
    private String da60;

    /**
     * DA61 - 婚姻状况（档案）
     */
    @ExcelProperty("da61")
    @TableField("da61")
    @Schema(description = "DA61 - 婚姻状况（档案）")
    private String da61;

    /**
     * DA62 - 健康状况（档案）
     */
    @ExcelProperty("da62")
    @TableField("da62")
    @Schema(description = "DA62 - 健康状况（档案）")
    private String da62;

    /**
     * DA63 - 家庭住址（档案）
     */
    @ExcelProperty("da63")
    @TableField("da63")
    @Schema(description = "DA63 - 家庭住址（档案）")
    private String da63;

    /**
     * DA64 - 离职状态
     */
    @ExcelProperty("da64")
    @TableField("da64")
    @Schema(description = "DA64 - 离职状态")
    private String da64;

    /**
     * DA65 - 离职日期
     */
    @ExcelProperty("da65")
    @TableField("da65")
    @Schema(description = "DA65 - 离职日期")
    private String da65;

    /**
     * DA66 - 合同签订日期
     */
    @ExcelProperty("da66")
    @TableField("da66")
    @Schema(description = "DA66 - 合同签订日期")
    private String da66;

    /**
     * DA67 - 合同续订日期
     */
    @ExcelProperty("da67")
    @TableField("da67")
    @Schema(description = "DA67 - 合同续订日期")
    private String da67;

    /**
     * DA68 - 合同终止日期
     */
    @ExcelProperty("da68")
    @TableField("da68")
    @Schema(description = "DA68 - 合同终止日期")
    private String da68;

    /**
     * DA69 - 入职日期（档案）
     */
    @ExcelProperty("da69")
    @TableField("da69")
    @Schema(description = "DA69 - 入职日期（档案）")
    private String da69;

    /**
     * DA70 - 工号
     */
    @ExcelProperty("da70")
    @TableField("da70")
    @Schema(description = "DA70 - 工号")
    private String da70;

    /**
     * DA71 - 员工编号
     */
    @ExcelProperty("da71")
    @TableField("da71")
    @Schema(description = "DA71 - 员工编号")
    private String da71;

    /**
     * DA72 - 合同代码
     */
    @ExcelProperty("da72")
    @TableField("da72")
    @Schema(description = "DA72 - 合同代码")
    private String da72;

    /**
     * DA73 - 转正日期
     */
    @ExcelProperty("da73")
    @TableField("da73")
    @Schema(description = "DA73 - 转正日期")
    private String da73;

    /**
     * DA74 - 紧急联系人
     */
    @ExcelProperty("da74")
    @TableField("da74")
    @Schema(description = "DA74 - 紧急联系人")
    private String da74;

    /**
     * DA75 - 紧急联系人电话
     */
    @ExcelProperty("da75")
    @TableField("da75")
    @Schema(description = "DA75 - 紧急联系人电话")
    private String da75;

    /**
     * DA76 - 紧急联系人邮箱
     */
    @ExcelProperty("da76")
    @TableField("da76")
    @Schema(description = "DA76 - 紧急联系人邮箱")
    private String da76;

    /**
     * DA77 - 紧急联系人姓名
     */
    @ExcelProperty("da77")
    @TableField("da77")
    @Schema(description = "DA77 - 紧急联系人姓名")
    private String da77;

    /**
     * DA78 - 现任职务开始日期
     */
    @ExcelProperty("da78")
    @TableField("da78")
    @Schema(description = "DA78 - 现任职务开始日期")
    private String da78;

    /**
     * DA79 - 现任职务结束日期
     */
    @ExcelProperty("da79")
    @TableField("da79")
    @Schema(description = "DA79 - 现任职务结束日期")
    private String da79;

    /**
     * DA80 - 合同开始日期
     */
    @ExcelProperty("da80")
    @TableField("da80")
    @Schema(description = "DA80 - 合同开始日期")
    private String da80;

    /**
     * DA81 - 合同结束日期
     */
    @ExcelProperty("da81")
    @TableField("da81")
    @Schema(description = "DA81 - 合同结束日期")
    private String da81;

    /**
     * DA82 - 合同类型
     */
    @ExcelProperty("da82")
    @TableField("da82")
    @Schema(description = "DA82 - 合同类型")
    private String da82;

    /**
     * DA83 - 合同次数
     */
    @ExcelProperty("da83")
    @TableField("da83")
    @Schema(description = "DA83 - 合同次数")
    private String da83;

    /**
     * DA84 - 试用期开始日期
     */
    @ExcelProperty("da84")
    @TableField("da84")
    @Schema(description = "DA84 - 试用期开始日期")
    private String da84;

    /**
     * DA85 - 试用期结束日期
     */
    @ExcelProperty("da85")
    @TableField("da85")
    @Schema(description = "DA85 - 试用期结束日期")
    private String da85;

    /**
     * DA86 - 试用期月数
     */
    @ExcelProperty("da86")
    @TableField("da86")
    @Schema(description = "DA86 - 试用期月数")
    private String da86;

    /**
     * DA87 - 是否试用期
     */
    @ExcelProperty("da87")
    @TableField("da87")
    @Schema(description = "DA87 - 是否试用期")
    private String da87;

    /**
     * DA88 - 培训开始日期
     */
    @ExcelProperty("da88")
    @TableField("da88")
    @Schema(description = "DA88 - 培训开始日期")
    private String da88;

    /**
     * DA89 - 培训结束日期
     */
    @ExcelProperty("da89")
    @TableField("da89")
    @Schema(description = "DA89 - 培训结束日期")
    private String da89;

    /**
     * DA90 - 培训内容
     */
    @ExcelProperty("da90")
    @TableField("da90")
    @Schema(description = "DA90 - 培训内容")
    private String da90;

    /**
     * DA91 - 培训费用
     */
    @ExcelProperty("da91")
    @TableField("da91")
    @Schema(description = "DA91 - 培训费用")
    private String da91;

    /**
     * DA92 - 培训证书
     */
    @ExcelProperty("da92")
    @TableField("da92")
    @Schema(description = "DA92 - 培训证书")
    private String da92;

    /**
     * DA93 - 入职方式（档案）
     */
    @ExcelProperty("da93")
    @TableField("da93")
    @Schema(description = "DA93 - 入职方式（档案）")
    private String da93;

    /**
     * DA94 - 入职日期（档案）
     */
    @ExcelProperty("da94")
    @TableField("da94")
    @Schema(description = "DA94 - 入职日期（档案）")
    private String da94;

    /**
     * DA95 - 入职来源
     */
    @ExcelProperty("da95")
    @TableField("da95")
    @Schema(description = "DA95 - 入职来源")
    private String da95;

    /**
     * DA96 - 工号（档案）
     */
    @ExcelProperty("da96")
    @TableField("da96")
    @Schema(description = "DA96 - 工号（档案）")
    private String da96;

    /**
     * DA97 - 部门代码
     */
    @ExcelProperty("da97")
    @TableField("da97")
    @Schema(description = "DA97 - 部门代码")
    private String da97;

    /**
     * DA98 - 状态标识
     */
    @ExcelProperty("da98")
    @TableField("da98")
    @Schema(description = "DA98 - 状态标识")
    private String da98;
```

- [ ] **Step 3: 为非 DA 字段添加 @ExcelProperty**

在 `// ==================== 非 DA 字段 ====================` 区块之后，为以下 19 个字段逐一添加 `@ExcelProperty`：

```java
    /**
     * NF - 年份
     */
    @ExcelProperty("nf")
    @TableField("nf")
    @Schema(description = "NF - 年份")
    private String nf;

    /**
     * YF - 月份
     */
    @ExcelProperty("yf")
    @TableField("yf")
    @Schema(description = "YF - 月份")
    private String yf;

    /**
     * C_TYPE - 类型
     */
    @ExcelProperty("cType")
    @TableField("c_type")
    @Schema(description = "C_TYPE - 类型")
    private String cType;

    /**
     * C_BEGIN_DATE - 开始日期
     */
    @ExcelProperty("cBeginDate")
    @TableField("c_begin_date")
    @Schema(description = "C_BEGIN_DATE - 开始日期")
    private String cBeginDate;

    /**
     * C_END_DATE - 结束日期
     */
    @ExcelProperty("cEndDate")
    @TableField("c_end_date")
    @Schema(description = "C_END_DATE - 结束日期")
    private String cEndDate;

    /**
     * C_TYPE_ONE - 类型一
     */
    @ExcelProperty("cTypeOne")
    @TableField("c_type_one")
    @Schema(description = "C_TYPE_ONE - 类型一")
    private String cTypeOne;

    /**
     * C_TYPE_TWO - 类型二
     */
    @ExcelProperty("cTypeTwo")
    @TableField("c_type_two")
    @Schema(description = "C_TYPE_TWO - 类型二")
    private String cTypeTwo;

    /**
     * C_TYPE_THREE - 类型三
     */
    @ExcelProperty("cTypeThree")
    @TableField("c_type_three")
    @Schema(description = "C_TYPE_THREE - 类型三")
    private String cTypeThree;

    /**
     * C_GRADE - 等级
     */
    @ExcelProperty("cGrade")
    @TableField("c_grade")
    @Schema(description = "C_GRADE - 等级")
    private String cGrade;

    /**
     * C_ZXGJ - 最高级别
     */
    @ExcelProperty("cZxgj")
    @TableField("c_zxgj")
    @Schema(description = "C_ZXGJ - 最高级别")
    private String cZxgj;

    /**
     * TD_BEGIN_DATE - TD开始日期
     */
    @ExcelProperty("tdBeginDate")
    @TableField("td_begin_date")
    @Schema(description = "TD_BEGIN_DATE - TD开始日期")
    private String tdBeginDate;

    /**
     * TD_END_DATE - TD结束日期
     */
    @ExcelProperty("tdEndDate")
    @TableField("td_end_date")
    @Schema(description = "TD_END_DATE - TD结束日期")
    private String tdEndDate;

    /**
     * DA2_1_IN - DA2_1输入
     */
    @ExcelProperty("da21In")
    @TableField("da2_1_in")
    @Schema(description = "DA2_1_IN - DA2_1输入")
    private String da21In;

    /**
     * DA2_IN - DA2输入
     */
    @ExcelProperty("da2In")
    @TableField("da2_in")
    @Schema(description = "DA2_IN - DA2输入")
    private String da2In;

    /**
     * A26 - A26字段
     */
    @ExcelProperty("a26")
    @TableField("a26")
    @Schema(description = "A26 - A26字段")
    private String a26;

    /**
     * JOB_ONE - 工作一
     */
    @ExcelProperty("jobOne")
    @TableField("job_one")
    @Schema(description = "JOB_ONE - 工作一")
    private String jobOne;

    /**
     * JOB_TWO - 工作二
     */
    @ExcelProperty("jobTwo")
    @TableField("job_two")
    @Schema(description = "JOB_TWO - 工作二")
    private String jobTwo;

    /**
     * JOB_THREE - 工作三
     */
    @ExcelProperty("jobThree")
    @TableField("job_three")
    @Schema(description = "JOB_THREE - 工作三")
    private String jobThree;
```

- [ ] **Step 4: 验证编译通过**

Run: `cd` to project root, then:
```
mvnw.cmd compile -pl . -q
```
Expected: BUILD SUCCESS，无编译错误。

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/crrc/entity/HrEmpHcmArchive.java
git commit -m "feat(entity): add @ExcelProperty annotations to HrEmpHcmArchive for Excel export"
```

---

### Task 2: Service 层添加 exportByCondition 方法

**Files:**
- Modify: `src/main/java/com/crrc/service/HrEmpHcmArchiveService.java:77` (接口末尾)
- Modify: `src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java:198` (buildQueryWrapper 方法之前)

- [ ] **Step 1: 在 Service 接口中添加方法签名**

在 `HrEmpHcmArchiveService.java` 的 `pageByCondition` 方法之后、接口闭合括号 `}` 之前，添加：

```java
    /**
     * 根据查询条件导出员工历史档案数据
     *
     * <p>根据传入的实体对象，自动拼装非空字段的查询条件，返回所有匹配的员工历史档案列表。
     * 该方法专用于导出功能，查询条件与 queryByCondition 一致。
     * 如果查询实体为空，则查询所有数据。</p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件。
     *                    如果传入 null，则查询所有数据
     * @return 匹配的员工历史档案列表，如果没有匹配数据返回空列表
     */
    List<HrEmpHcmArchive> exportByCondition(HrEmpHcmArchive queryEntity);
```

- [ ] **Step 2: 在 ServiceImpl 中添加方法实现**

在 `HrEmpHcmArchiveServiceImpl.java` 的 `pageByCondition` 方法之后、`buildQueryWrapper` 方法之前，添加：

```java
    /**
     * 根据查询条件导出员工历史档案数据
     *
     * <p>根据传入的实体对象，动态拼装非空字段的查询条件，返回所有匹配的员工历史档案列表。
     * 该方法专用于导出功能，复用 buildQueryWrapper 构建查询条件，
     * 确保导出逻辑与分页查询、列表查询的过滤条件完全一致。
     * 如果查询实体为空，则查询所有数据。</p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件
     * @return 匹配的员工历史档案列表，如果没有匹配数据返回空列表
     */
    @Override
    public List<HrEmpHcmArchive> exportByCondition(HrEmpHcmArchive queryEntity) {
        // 如果查询实体为空，创建默认实体（查询所有数据）
        queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

        // 复用已有的 buildQueryWrapper 方法构建查询条件
        // 保持与 queryByCondition、pageByCondition 的查询逻辑一致
        LambdaQueryWrapper<HrEmpHcmArchive> wrapper = buildQueryWrapper(queryEntity);

        // 执行查询并返回结果列表
        return baseMapper.selectList(wrapper);
    }
```

- [ ] **Step 3: 验证编译通过**

Run:
```
mvnw.cmd compile -pl . -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/crrc/service/HrEmpHcmArchiveService.java src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java
git commit -m "feat(service): add exportByCondition method to HrEmpHcmArchiveService"
```

---

### Task 3: Controller 添加 /export 接口

**Files:**
- Modify: `src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java`

- [ ] **Step 1: 添加 import 语句**

在 `HrEmpHcmArchiveController.java` 的 import 区域，添加以下 import：

```java
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
```

注意：`java.util.List` 已经在现有 import 中，不需要重复添加。

- [ ] **Step 2: 更新类级别 Javadoc**

将类 Javadoc 中的接口列表更新，在现有三个接口之后添加第四个：

```java
 *     <li>POST /hrEmpHcmArchive/export - 导出数据</li>
```

- [ ] **Step 3: 添加导出方法**

在 `getHrEmpHcmArchive` 方法之后、类的闭合括号 `}` 之前，添加导出方法：

```java
    /**
     * 导出员工历史档案数据
     *
     * <p>根据传入的查询条件，将匹配的员工历史档案数据导出为 xlsx 文件。
     * 如果查询条件为空，则导出全部数据。</p>
     *
     * <p>查询条件与分页查询接口 pageHrEmpHcmArchive 相同，支持的字段包括：
     * da1(档案号)、da2(部门)、da3(姓名)、da4(出生日期)、da5(性别)、
     * da6(民族)、da7(籍贯)、da9(第一学历)、da24(参加工作时间)、da52(政治面貌)。</p>
     *
     * <p>导出的 Excel 文件包含所有业务字段（da1~da98 及非 DA 字段），
     * 列名直接使用 Java 字段名（如 da1、da2、cType 等）。</p>
     *
     * <p>异常处理策略：
     * <ul>
     *     <li>查询阶段异常：返回 JSON 格式的错误信息（此时 response 未提交）</li>
     *     <li>写入阶段异常：交给框架统一处理（此时 response 已提交）</li>
     * </ul>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为过滤条件。为 null 时导出全部数据
     * @param response    HTTP 响应对象，用于写入 Excel 文件流
     * @throws IOException 当写入 Excel 文件流发生异常时抛出
     */
    @PostMapping("/export")
    @Operation(summary = "导出员工历史档案数据")
    @AuditLog(value = "导出员工历史档案数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    public ResponseResult<List<HrEmpHcmArchive>> exportHrEmpHcmArchive(
            @Validated @RequestBody(required = false) HrEmpHcmArchive queryEntity,
            HttpServletResponse response) throws IOException {

        // 空条件处理：创建默认实体（导出全部数据）
        queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

        // 先查询数据（在设置 response 头之前，确保查询异常时能返回 JSON 错误响应）
        List<HrEmpHcmArchive> dataList;
        try {
            dataList = hrEmpHcmArchiveService.exportByCondition(queryEntity);
        } catch (Exception e) {
            // 查询阶段异常：response 尚未提交，可以正常返回 JSON
            log.error("导出员工历史档案数据-查询异常", e);
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }

        // 设置响应头：xlsx 内容类型 + UTF-8 编码
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");

        // 生成文件名：包含时间戳，格式为 "员工历史档案数据-yyyyMMddHHmmss.xlsx"
        String fileName = URLEncoder.encode(
                "员工历史档案数据-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()),
                "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 使用 EasyExcel 写入 Excel
        // HrEmpHcmArchive.class 上的 @ExcelProperty 注解自动定义列头和顺序
        // @ExcelIgnoreUnannotated 自动排除未注解的字段（BaseEntity 继承字段、deleted 等）
        EasyExcel.write(response.getOutputStream(), HrEmpHcmArchive.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("员工历史档案数据")
                .doWrite(dataList);

        // 返回 null：Excel 已直接写入 response 流，不再返回 JSON
        return null;
    }
```

- [ ] **Step 4: 验证编译通过**

Run:
```
mvnw.cmd compile -pl . -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java
git commit -m "feat(controller): add /export endpoint to HrEmpHcmArchiveController"
```

---

### Task 4: 编译验证与最终检查

**Files:** 无新文件修改

- [ ] **Step 1: 全量编译验证**

Run:
```
mvnw.cmd compile -pl .
```
Expected: BUILD SUCCESS，无编译错误和警告。

- [ ] **Step 2: 检查注解完整性**

验证实体类中所有 117 个业务字段都已添加 `@ExcelProperty`，且 `deleted` 字段未添加：

```bash
grep -c "@ExcelProperty" src/main/java/com/crrc/entity/HrEmpHcmArchive.java
```
Expected: 输出 `117`（98 个 DA 字段 + 19 个非 DA 字段）

- [ ] **Step 3: 检查 import 完整性**

确认 Controller 中新添加的 6 个 import 都存在：

```bash
grep -c "com.alibaba.excel" src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java
```
Expected: 输出 `3`（EasyExcel, ExcelTypeEnum, LongestMatchColumnWidthStyleStrategy）

```bash
grep -c "javax.servlet" src/main/java/com/crrc/controller/HrEmpHcmArchiveController.java
```
Expected: 输出 `1`（HttpServletResponse）

- [ ] **Step 4: 检查 Service 方法签名一致性**

确认 Service 接口和 ServiceImpl 中的方法签名完全匹配：

```bash
grep "exportByCondition" src/main/java/com/crrc/service/HrEmpHcmArchiveService.java
grep "exportByCondition" src/main/java/com/crrc/service/impl/HrEmpHcmArchiveServiceImpl.java
```
Expected: 两处输出都包含 `List<HrEmpHcmArchive> exportByCondition(HrEmpHcmArchive queryEntity)`
