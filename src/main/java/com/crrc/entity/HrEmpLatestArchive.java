package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import java.io.Serializable;

/**
 * 员工最新档案数据实体类
 * 对应数据库表：hr_emp_latest_archive
 *
 * <p>该实体类用于映射员工最新档案数据表，字段命名严格按照 DA1-DA98 列名规范，
 * 每个字段通过 @TableField 显式指定数据库列名（全小写），确保与数据库列精确对应。</p>
 *
 * <p>数据来源：TSDA备份库202512.xlsx 中的历史数据表</p>
 *
 * @author yukun
 * @since 2026-06-24
 */
@Accessors(chain = false)
@TableName("hr_emp_latest_archive")
@Schema(description = "员工最新档案数据")
@ExcelIgnoreUnannotated
@Data
@NoArgsConstructor
public class HrEmpLatestArchive extends BaseEntity implements Serializable {
    /**
     * 逻辑删除标记（0表示未删除，1表示已删除）
     */
    @TableLogic(value = "0")
    @Schema(description = "逻辑删除标记")
    private Integer deleted;

    // ==================== DA1 - DA98 数据字段 ====================

    /**
     * DA1 - 档案号
     */
    @ExcelProperty("DA1")
    @TableId(value = "\"DA1\"", type = IdType.AUTO)
    @Schema(description = "DA1 - 档案号")
    private String da1;

    /**
     * DA2 - 所在部门
     */
    @ExcelProperty("DA2")
    @TableField("\"DA2\"")
    @Schema(description = "DA2 - 所在部门")
    private String da2;

    /**
     * DA3 - 姓名
     */
    @ExcelProperty("DA3")
    @TableField("\"DA3\"")
    @Schema(description = "DA3 - 姓名")
    private String da3;

    /**
     * DA4 - 出生日期
     */
    @ExcelProperty("DA4")
    @TableField("\"DA4\"")
    @Schema(description = "DA4 - 出生日期")
    private String da4;

    /**
     * DA5 - 性别
     */
    @ExcelProperty("DA5")
    @TableField("\"DA5\"")
    @Schema(description = "DA5 - 性别")
    private String da5;

    /**
     * DA6 - 民族
     */
    @ExcelProperty("DA6")
    @TableField("\"DA6\"")
    @Schema(description = "DA6 - 民族")
    private String da6;

    /**
     * DA7 - 籍贯
     */
    @ExcelProperty("DA7")
    @TableField("\"DA7\"")
    @Schema(description = "DA7 - 籍贯")
    private String da7;

    /**
     * DA8 - 身份证号
     */
    @ExcelProperty("DA8")
    @TableField("\"DA8\"")
    @Schema(description = "DA8 - 身份证号")
    private String da8;

    /**
     * DA9 - 第一学历
     */
    @ExcelProperty("DA9")
    @TableField("\"DA9\"")
    @Schema(description = "DA9 - 第一学历")
    private String da9;

    /**
     * DA10 - 第一学历入学日期
     */
    @ExcelProperty("DA10")
    @TableField("\"DA10\"")
    @Schema(description = "DA10 - 第一学历入学日期")
    private String da10;

    /**
     * DA11 - 第一学历专业
     */
    @ExcelProperty("DA11")
    @TableField("\"DA11\"")
    @Schema(description = "DA11 - 第一学历专业")
    private String da11;

    /**
     * DA12 - 第一学历毕业院校
     */
    @ExcelProperty("DA12")
    @TableField("\"DA12\"")
    @Schema(description = "DA12 - 第一学历毕业院校")
    private String da12;

    /**
     * DA13 - 第一学历教育类型
     */
    @ExcelProperty("DA13")
    @TableField("\"DA13\"")
    @Schema(description = "DA13 - 第一学历教育类型")
    private String da13;

    /**
     * DA14 - 第一学历学位授予日期
     */
    @ExcelProperty("DA14")
    @TableField("\"DA14\"")
    @Schema(description = "DA14 - 第一学历学位授予日期")
    private String da14;

    /**
     * DA15 - 第一学历学位
     */
    @ExcelProperty("DA15")
    @TableField("\"DA15\"")
    @Schema(description = "DA15 - 第一学历学位")
    private String da15;

    /**
     * DA16 - 第二学历
     */
    @ExcelProperty("DA16")
    @TableField("\"DA16\"")
    @Schema(description = "DA16 - 第二学历")
    private String da16;

    /**
     * DA17 - 第二学历入学日期
     */
    @ExcelProperty("DA17")
    @TableField("\"DA17\"")
    @Schema(description = "DA17 - 第二学历入学日期")
    private String da17;

    /**
     * DA18 - 第二学历专业
     */
    @ExcelProperty("DA18")
    @TableField("\"DA18\"")
    @Schema(description = "DA18 - 第二学历专业")
    private String da18;

    /**
     * DA19 - 第二学历毕业院校
     */
    @ExcelProperty("DA19")
    @TableField("\"DA19\"")
    @Schema(description = "DA19 - 第二学历毕业院校")
    private String da19;

    /**
     * DA20 - 第二学历教育类型
     */
    @ExcelProperty("DA20")
    @TableField("\"DA20\"")
    @Schema(description = "DA20 - 第二学历教育类型")
    private String da20;

    /**
     * DA21 - 第二学历学位授予日期
     */
    @ExcelProperty("DA21")
    @TableField("\"DA21\"")
    @Schema(description = "DA21 - 第二学历学位授予日期")
    private String da21;

    /**
     * DA22 - 第二学历学位
     */
    @ExcelProperty("DA22")
    @TableField("\"DA22\"")
    @Schema(description = "DA22 - 第二学历学位")
    private String da22;

    /**
     * DA23 - 入职方式
     */
    @ExcelProperty("DA23")
    @TableField("\"DA23\"")
    @Schema(description = "DA23 - 入职方式")
    private String da23;

    /**
     * DA24 - 参加工作时间
     */
    @ExcelProperty("DA24")
    @TableField("\"DA24\"")
    @Schema(description = "DA24 - 参加工作时间")
    private String da24;

    /**
     * DA25 - 入职日期
     */
    @ExcelProperty("DA25")
    @TableField("\"DA25\"")
    @Schema(description = "DA25 - 入职日期")
    private String da25;

    /**
     * DA26 - 年龄
     */
    @ExcelProperty("DA26")
    @TableField("\"DA26\"")
    @Schema(description = "DA26 - 年龄")
    private String da26;

    /**
     * DA27 - 合同年限
     */
    @ExcelProperty("DA27")
    @TableField("\"DA27\"")
    @Schema(description = "DA27 - 合同年限")
    private String da27;

    /**
     * DA28 - 岗位名称开始日期
     */
    @ExcelProperty("DA28")
    @TableField("\"DA28\"")
    @Schema(description = "DA28 - 岗位名称开始日期")
    private String da28;

    /**
     * DA29 - 岗位名称结束日期
     */
    @ExcelProperty("DA29")
    @TableField("\"DA29\"")
    @Schema(description = "DA29 - 岗位名称结束日期")
    private String da29;

    /**
     * DA30 - 第一职称
     */
    @ExcelProperty("DA30")
    @TableField("\"DA30\"")
    @Schema(description = "DA30 - 第一职称")
    private String da30;

    /**
     * DA31 - 第一职称评定日期
     */
    @ExcelProperty("DA31")
    @TableField("\"DA31\"")
    @Schema(description = "DA31 - 第一职称评定日期")
    private String da31;

    /**
     * DA32 - 第二职称
     */
    @ExcelProperty("DA32")
    @TableField("\"DA32\"")
    @Schema(description = "DA32 - 第二职称")
    private String da32;

    /**
     * DA33 - 第二职称评定日期
     */
    @ExcelProperty("DA33")
    @TableField("\"DA33\"")
    @Schema(description = "DA33 - 第二职称评定日期")
    private String da33;

    /**
     * DA34 - 身份类型
     */
    @ExcelProperty("DA34")
    @TableField("\"DA34\"")
    @Schema(description = "DA34 - 身份类型")
    private String da34;

    /**
     * DA35 - 人员分类
     */
    @ExcelProperty("DA35")
    @TableField("\"DA35\"")
    @Schema(description = "DA35 - 人员分类")
    private String da35;

    /**
     * DA36 - 人员分类2
     */
    @ExcelProperty("DA36")
    @TableField("\"DA36\"")
    @Schema(description = "DA36 - 人员分类2")
    private String da36;

    /**
     * DA37 - 在岗状态
     */
    @ExcelProperty("DA37")
    @TableField("\"DA37\"")
    @Schema(description = "DA37 - 在岗状态")
    private String da37;

    /**
     * DA38 - 人员类别
     */
    @ExcelProperty("DA38")
    @TableField("\"DA38\"")
    @Schema(description = "DA38 - 人员类别")
    private String da38;

    /**
     * DA39 - 技能等级
     */
    @ExcelProperty("DA39")
    @TableField("\"DA39\"")
    @Schema(description = "DA39 - 技能等级")
    private String da39;

    /**
     * DA40 - 技能级别
     */
    @ExcelProperty("DA40")
    @TableField("\"DA40\"")
    @Schema(description = "DA40 - 技能级别")
    private String da40;

    /**
     * DA41 - 技能等级评定日期
     */
    @ExcelProperty("DA41")
    @TableField("\"DA41\"")
    @Schema(description = "DA41 - 技能等级评定日期")
    private String da41;

    /**
     * DA42 - 职称级别
     */
    @ExcelProperty("DA42")
    @TableField("\"DA42\"")
    @Schema(description = "DA42 - 职称级别")
    private String da42;

    /**
     * DA43 - 职称级别评定日期
     */
    @ExcelProperty("DA43")
    @TableField("\"DA43\"")
    @Schema(description = "DA43 - 职称级别评定日期")
    private String da43;

    /**
     * DA44 - 职称级别状态
     */
    @ExcelProperty("DA44")
    @TableField("\"DA44\"")
    @Schema(description = "DA44 - 职称级别状态")
    private String da44;

    /**
     * DA45 - 婚姻状况
     */
    @ExcelProperty("DA45")
    @TableField("\"DA45\"")
    @Schema(description = "DA45 - 婚姻状况")
    private String da45;

    /**
     * DA46 - 结婚日期
     */
    @ExcelProperty("DA46")
    @TableField("\"DA46\"")
    @Schema(description = "DA46 - 结婚日期")
    private String da46;

    /**
     * DA47 - 健康状况
     */
    @ExcelProperty("DA47")
    @TableField("\"DA47\"")
    @Schema(description = "DA47 - 健康状况")
    private String da47;

    /**
     * DA48 - 身高
     */
    @ExcelProperty("DA48")
    @TableField("\"DA48\"")
    @Schema(description = "DA48 - 身高")
    private String da48;

    /**
     * DA49 - 体重
     */
    @ExcelProperty("DA49")
    @TableField("\"DA49\"")
    @Schema(description = "DA49 - 体重")
    private String da49;

    /**
     * DA50 - 血型
     */
    @ExcelProperty("DA50")
    @TableField("\"DA50\"")
    @Schema(description = "DA50 - 血型")
    private String da50;

    /**
     * DA51 - 家庭住址
     */
    @ExcelProperty("DA51")
    @TableField("\"DA51\"")
    @Schema(description = "DA51 - 家庭住址")
    private String da51;

    /**
     * DA52 - 政治面貌
     */
    @ExcelProperty("DA52")
    @TableField("\"DA52\"")
    @Schema(description = "DA52 - 政治面貌")
    private String da52;

    /**
     * DA53 - 入党日期
     */
    @ExcelProperty("DA53")
    @TableField("\"DA53\"")
    @Schema(description = "DA53 - 入党日期")
    private String da53;

    /**
     * DA54 - 户籍类型
     */
    @ExcelProperty("DA54")
    @TableField("\"DA54\"")
    @Schema(description = "DA54 - 户籍类型")
    private String da54;

    /**
     * DA55 - 户籍所在地
     */
    @ExcelProperty("DA55")
    @TableField("\"DA55\"")
    @Schema(description = "DA55 - 户籍所在地")
    private String da55;

    /**
     * DA56 - 户籍所在地详细
     */
    @ExcelProperty("DA56")
    @TableField("\"DA56\"")
    @Schema(description = "DA56 - 户籍所在地详细")
    private String da56;

    /**
     * DA57 - 现居住地
     */
    @ExcelProperty("DA57")
    @TableField("\"DA57\"")
    @Schema(description = "DA57 - 现居住地")
    private String da57;

    /**
     * DA58 - 手机号码
     */
    @ExcelProperty("DA58")
    @TableField("\"DA58\"")
    @Schema(description = "DA58 - 手机号码")
    private String da58;

    /**
     * DA59 - 工资/档次
     */
    @ExcelProperty("DA59")
    @TableField("\"DA59\"")
    @Schema(description = "DA59 - 工资/档次")
    private String da59;

    /**
     * DA60 - 年龄（档案年龄）
     */
    @ExcelProperty("DA60")
    @TableField("\"DA60\"")
    @Schema(description = "DA60 - 年龄（档案年龄）")
    private String da60;

    /**
     * DA61 - 婚姻状况（档案）
     */
    @ExcelProperty("DA61")
    @TableField("\"DA61\"")
    @Schema(description = "DA61 - 婚姻状况（档案）")
    private String da61;

    /**
     * DA62 - 健康状况（档案）
     */
    @ExcelProperty("DA62")
    @TableField("\"DA62\"")
    @Schema(description = "DA62 - 健康状况（档案）")
    private String da62;

    /**
     * DA63 - 家庭住址（档案）
     */
    @ExcelProperty("DA63")
    @TableField("\"DA63\"")
    @Schema(description = "DA63 - 家庭住址（档案）")
    private String da63;

    /**
     * DA64 - 离职状态
     */
    @ExcelProperty("DA64")
    @TableField("\"DA64\"")
    @Schema(description = "DA64 - 离职状态")
    private String da64;

    /**
     * DA65 - 离职日期
     */
    @ExcelProperty("DA65")
    @TableField("\"DA65\"")
    @Schema(description = "DA65 - 离职日期")
    private String da65;

    /**
     * DA66 - 合同签订日期
     */
    @ExcelProperty("DA66")
    @TableField("\"DA66\"")
    @Schema(description = "DA66 - 合同签订日期")
    private String da66;

    /**
     * DA67 - 合同续订日期
     */
    @ExcelProperty("DA67")
    @TableField("\"DA67\"")
    @Schema(description = "DA67 - 合同续订日期")
    private String da67;

    /**
     * DA68 - 合同终止日期
     */
    @ExcelProperty("DA68")
    @TableField("\"DA68\"")
    @Schema(description = "DA68 - 合同终止日期")
    private String da68;

    /**
     * DA69 - 入职日期（档案）
     */
    @ExcelProperty("DA69")
    @TableField("\"DA69\"")
    @Schema(description = "DA69 - 入职日期（档案）")
    private String da69;

    /**
     * DA70 - 工号
     */
    @ExcelProperty("DA70")
    @TableField("\"DA70\"")
    @Schema(description = "DA70 - 工号")
    private String da70;

    /**
     * DA71 - 员工编号
     */
    @ExcelProperty("DA71")
    @TableField("\"DA71\"")
    @Schema(description = "DA71 - 员工编号")
    private String da71;

    /**
     * DA72 - 合同代码
     */
    @ExcelProperty("DA72")
    @TableField("\"DA72\"")
    @Schema(description = "DA72 - 合同代码")
    private String da72;

    /**
     * DA73 - 转正日期
     */
    @ExcelProperty("DA73")
    @TableField("\"DA73\"")
    @Schema(description = "DA73 - 转正日期")
    private String da73;

    /**
     * DA74 - 紧急联系人
     */
    @ExcelProperty("DA74")
    @TableField("\"DA74\"")
    @Schema(description = "DA74 - 紧急联系人")
    private String da74;

    /**
     * DA75 - 紧急联系人电话
     */
    @ExcelProperty("DA75")
    @TableField("\"DA75\"")
    @Schema(description = "DA75 - 紧急联系人电话")
    private String da75;

    /**
     * DA76 - 紧急联系人邮箱
     */
    @ExcelProperty("DA76")
    @TableField("\"DA76\"")
    @Schema(description = "DA76 - 紧急联系人邮箱")
    private String da76;

    /**
     * DA77 - 紧急联系人姓名
     */
    @ExcelProperty("DA77")
    @TableField("\"DA77\"")
    @Schema(description = "DA77 - 紧急联系人姓名")
    private String da77;

    /**
     * DA78 - 现任职务开始日期
     */
    @ExcelProperty("DA78")
    @TableField("\"DA78\"")
    @Schema(description = "DA78 - 现任职务开始日期")
    private String da78;

    /**
     * DA79 - 现任职务结束日期
     */
    @ExcelProperty("DA79")
    @TableField("\"DA79\"")
    @Schema(description = "DA79 - 现任职务结束日期")
    private String da79;

    /**
     * DA80 - 合同开始日期
     */
    @ExcelProperty("DA80")
    @TableField("\"DA80\"")
    @Schema(description = "DA80 - 合同开始日期")
    private String da80;

    /**
     * DA81 - 合同结束日期
     */
    @ExcelProperty("DA81")
    @TableField("\"DA81\"")
    @Schema(description = "DA81 - 合同结束日期")
    private String da81;

    /**
     * DA82 - 合同类型
     */
    @ExcelProperty("DA82")
    @TableField("\"DA82\"")
    @Schema(description = "DA82 - 合同类型")
    private String da82;

    /**
     * DA83 - 合同次数
     */
    @ExcelProperty("DA83")
    @TableField("\"DA83\"")
    @Schema(description = "DA83 - 合同次数")
    private String da83;

    /**
     * DA84 - 试用期开始日期
     */
    @ExcelProperty("DA84")
    @TableField("\"DA84\"")
    @Schema(description = "DA84 - 试用期开始日期")
    private String da84;

    /**
     * DA85 - 试用期结束日期
     */
    @ExcelProperty("DA85")
    @TableField("\"DA85\"")
    @Schema(description = "DA85 - 试用期结束日期")
    private String da85;

    /**
     * DA86 - 试用期月数
     */
    @ExcelProperty("DA86")
    @TableField("\"DA86\"")
    @Schema(description = "DA86 - 试用期月数")
    private String da86;

    /**
     * DA87 - 是否试用期
     */
    @ExcelProperty("DA87")
    @TableField("\"DA87\"")
    @Schema(description = "DA87 - 是否试用期")
    private String da87;

    /**
     * DA88 - 培训开始日期
     */
    @ExcelProperty("DA88")
    @TableField("\"DA88\"")
    @Schema(description = "DA88 - 培训开始日期")
    private String da88;

    /**
     * DA89 - 培训结束日期
     */
    @ExcelProperty("DA89")
    @TableField("\"DA89\"")
    @Schema(description = "DA89 - 培训结束日期")
    private String da89;

    /**
     * DA90 - 培训内容
     */
    @ExcelProperty("DA90")
    @TableField("\"DA90\"")
    @Schema(description = "DA90 - 培训内容")
    private String da90;

    /**
     * DA91 - 培训费用
     */
    @ExcelProperty("DA91")
    @TableField("\"DA91\"")
    @Schema(description = "DA91 - 培训费用")
    private String da91;

    /**
     * DA92 - 培训证书
     */
    @ExcelProperty("DA92")
    @TableField("\"DA92\"")
    @Schema(description = "DA92 - 培训证书")
    private String da92;

    /**
     * DA93 - 入职方式（档案）
     */
    @ExcelProperty("DA93")
    @TableField("\"DA93\"")
    @Schema(description = "DA93 - 入职方式（档案）")
    private String da93;

    /**
     * DA94 - 入职日期（档案）
     */
    @ExcelProperty("DA94")
    @TableField("\"DA94\"")
    @Schema(description = "DA94 - 入职日期（档案）")
    private String da94;

    /**
     * DA95 - 入职来源
     */
    @ExcelProperty("DA95")
    @TableField("\"DA95\"")
    @Schema(description = "DA95 - 入职来源")
    private String da95;

    /**
     * DA96 - 工号（档案）
     */
    @ExcelProperty("DA96")
    @TableField("\"DA96\"")
    @Schema(description = "DA96 - 工号（档案）")
    private String da96;

    /**
     * DA97 - 部门代码
     */
    @ExcelProperty("DA97")
    @TableField("\"DA97\"")
    @Schema(description = "DA97 - 部门代码")
    private String da97;

    /**
     * DA98 - 状态标识
     */
    @ExcelProperty("DA98")
    @TableField("\"DA98\"")
    @Schema(description = "DA98 - 状态标识")
    private String da98;

    // ==================== 非 DA 字段 ====================

    /**
     * NF - 年份
     */
    @ExcelProperty("NF")
    @TableField("\"NF\"")
    @Schema(description = "NF - 年份")
    private String nf;

    /**
     * YF - 月份
     */
    @ExcelProperty("YF")
    @TableField("\"YF\"")
    @Schema(description = "YF - 月份")
    private String yf;

    /**
     * C_TYPE - 类型
     */
    @ExcelProperty("C_TYPE")
    @TableField("\"C_TYPE\"")
    @Schema(description = "C_TYPE - 类型")
    private String cType;

    /**
     * C_BEGIN_DATE - 开始日期
     */
    @ExcelProperty("C_BEGIN_DATE")
    @TableField("\"C_BEGIN_DATE\"")
    @Schema(description = "C_BEGIN_DATE - 开始日期")
    private String cBeginDate;

    /**
     * C_END_DATE - 结束日期
     */
    @ExcelProperty("C_END_DATE")
    @TableField("\"C_END_DATE\"")
    @Schema(description = "C_END_DATE - 结束日期")
    private String cEndDate;

    /**
     * C_TYPE_ONE - 职业发展通道
     */
    @ExcelProperty("C_TYPE_ONE")
    @TableField("\"C_TYPE_ONE\"")
    @Schema(description = "C_TYPE_ONE - 职业发展通道")
    private String cTypeOne;

    /**
     * C_TYPE_TWO - 空
     */
    @ExcelProperty("C_TYPE_TWO")
    @TableField("\"C_TYPE_TWO\"")
    @Schema(description = "C_TYPE_TWO - 空")
    private String cTypeTwo;

    /**
     * C_TYPE_THREE - 空
     */
    @ExcelProperty("C_TYPE_THREE")
    @TableField("\"C_TYPE_THREE\"")
    @Schema(description = "C_TYPE_THREE - 空")
    private String cTypeThree;

    /**
     * C_GRADE - 等级
     */
    @ExcelProperty("C_GRADE")
    @TableField("\"C_GRADE\"")
    @Schema(description = "C_GRADE - 等级")
    private String cGrade;

    /**
     * C_ZXGJ - 最高级别
     */
    @ExcelProperty("C_ZXGJ")
    @TableField("\"C_ZXGJ\"")
    @Schema(description = "C_ZXGJ - 最高级别")
    private String cZxgj;

    /**
     * TD_BEGIN_DATE - TD开始日期
     */
    @ExcelProperty("TD_BEGIN_DATE")
    @TableField("\"TD_BEGIN_DATE\"")
    @Schema(description = "TD_BEGIN_DATE - TD开始日期")
    private String tdBeginDate;

    /**
     * TD_END_DATE - TD结束日期
     */
    @ExcelProperty("TD_END_DATE")
    @TableField("\"TD_END_DATE\"")
    @Schema(description = "TD_END_DATE - TD结束日期")
    private String tdEndDate;

    /**
     * DA2_1_IN - DA2_1输入
     */
    @ExcelProperty("DA2_1_IN")
    @TableField("\"DA2_1_IN\"")
    @Schema(description = "DA2_1_IN - DA2_1输入")
    private String da21In;

    /**
     * DA2_IN - DA2输入
     */
    @ExcelProperty("DA2_IN")
    @TableField("\"DA2_IN\"")
    @Schema(description = "DA2_IN - DA2输入")
    private String da2In;

    /**
     * A26 - A26字段
     */
    @ExcelProperty("A26")
    @TableField("\"A26\"")
    @Schema(description = "A26 - A26字段")
    private String a26;

    /**
     * JOB_ONE - 职类
     */
    @ExcelProperty("JOB_ONE")
    @TableField("\"JOB_ONE\"")
    @Schema(description = "JOB_ONE - 职类")
    private String jobOne;

    /**
     * JOB_TWO - 职群
     */
    @ExcelProperty("JOB_TWO")
    @TableField("\"JOB_TWO\"")
    @Schema(description = "JOB_TWO - 职群")
    private String jobTwo;

    /**
     * JOB_THREE - 职种
     */
    @ExcelProperty("JOB_THREE")
    @TableField("\"JOB_THREE\"")
    @Schema(description = "JOB_THREE - 职种")
    private String jobThree;
}
