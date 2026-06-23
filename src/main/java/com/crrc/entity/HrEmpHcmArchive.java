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

import java.io.Serializable;

/**
 * 员工历史档案数据实体类
 * 对应数据库表：hr_emp_hcm_archive
 *
 * <p>该实体类用于映射员工历史档案数据表，字段命名严格按照 DA1-DA98 列名规范，
 * 每个字段通过 @TableField 显式指定数据库列名（全小写），确保与数据库列精确对应。</p>
 *
 * <p>数据来源：TSDA备份库202512.xlsx 中的历史数据表</p>
 *
 * @author yukun
 * @since 2026-06-23
 */
@Accessors(chain = false)
@TableName("hr_emp_hcm_archive")
@Schema(description = "员工历史档案数据")
@Data
@NoArgsConstructor
public class HrEmpHcmArchive extends BaseEntity implements Serializable {
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
    @TableId(value = "id", type = IdType.AUTO)
    @TableField("da1")
    @Schema(description = "DA1 - 档案号")
    private String da1;

    /**
     * DA2 - 所在部门
     */
    @TableField("da2")
    @Schema(description = "DA2 - 所在部门")
    private String da2;

    /**
     * DA3 - 姓名
     */
    @TableField("da3")
    @Schema(description = "DA3 - 姓名")
    private String da3;

    /**
     * DA4 - 出生日期
     */
    @TableField("da4")
    @Schema(description = "DA4 - 出生日期")
    private String da4;

    /**
     * DA5 - 性别
     */
    @TableField("da5")
    @Schema(description = "DA5 - 性别")
    private String da5;

    /**
     * DA6 - 民族
     */
    @TableField("da6")
    @Schema(description = "DA6 - 民族")
    private String da6;

    /**
     * DA7 - 籍贯
     */
    @TableField("da7")
    @Schema(description = "DA7 - 籍贯")
    private String da7;

    /**
     * DA8 - 身份证号
     */
    @TableField("da8")
    @Schema(description = "DA8 - 身份证号")
    private String da8;

    /**
     * DA9 - 第一学历
     */
    @TableField("da9")
    @Schema(description = "DA9 - 第一学历")
    private String da9;

    /**
     * DA10 - 第一学历入学日期
     */
    @TableField("da10")
    @Schema(description = "DA10 - 第一学历入学日期")
    private String da10;

    /**
     * DA11 - 第一学历专业
     */
    @TableField("da11")
    @Schema(description = "DA11 - 第一学历专业")
    private String da11;

    /**
     * DA12 - 第一学历毕业院校
     */
    @TableField("da12")
    @Schema(description = "DA12 - 第一学历毕业院校")
    private String da12;

    /**
     * DA13 - 第一学历教育类型
     */
    @TableField("da13")
    @Schema(description = "DA13 - 第一学历教育类型")
    private String da13;

    /**
     * DA14 - 第一学历学位授予日期
     */
    @TableField("da14")
    @Schema(description = "DA14 - 第一学历学位授予日期")
    private String da14;

    /**
     * DA15 - 第一学历学位
     */
    @TableField("da15")
    @Schema(description = "DA15 - 第一学历学位")
    private String da15;

    /**
     * DA16 - 第二学历
     */
    @TableField("da16")
    @Schema(description = "DA16 - 第二学历")
    private String da16;

    /**
     * DA17 - 第二学历入学日期
     */
    @TableField("da17")
    @Schema(description = "DA17 - 第二学历入学日期")
    private String da17;

    /**
     * DA18 - 第二学历专业
     */
    @TableField("da18")
    @Schema(description = "DA18 - 第二学历专业")
    private String da18;

    /**
     * DA19 - 第二学历毕业院校
     */
    @TableField("da19")
    @Schema(description = "DA19 - 第二学历毕业院校")
    private String da19;

    /**
     * DA20 - 第二学历教育类型
     */
    @TableField("da20")
    @Schema(description = "DA20 - 第二学历教育类型")
    private String da20;

    /**
     * DA21 - 第二学历学位授予日期
     */
    @TableField("da21")
    @Schema(description = "DA21 - 第二学历学位授予日期")
    private String da21;

    /**
     * DA22 - 第二学历学位
     */
    @TableField("da22")
    @Schema(description = "DA22 - 第二学历学位")
    private String da22;

    /**
     * DA23 - 入职方式
     */
    @TableField("da23")
    @Schema(description = "DA23 - 入职方式")
    private String da23;

    /**
     * DA24 - 参加工作时间
     */
    @TableField("da24")
    @Schema(description = "DA24 - 参加工作时间")
    private String da24;

    /**
     * DA25 - 入职日期
     */
    @TableField("da25")
    @Schema(description = "DA25 - 入职日期")
    private String da25;

    /**
     * DA26 - 年龄
     */
    @TableField("da26")
    @Schema(description = "DA26 - 年龄")
    private String da26;

    /**
     * DA27 - 合同年限
     */
    @TableField("da27")
    @Schema(description = "DA27 - 合同年限")
    private String da27;

    /**
     * DA28 - 岗位名称开始日期
     */
    @TableField("da28")
    @Schema(description = "DA28 - 岗位名称开始日期")
    private String da28;

    /**
     * DA29 - 岗位名称结束日期
     */
    @TableField("da29")
    @Schema(description = "DA29 - 岗位名称结束日期")
    private String da29;

    /**
     * DA30 - 第一职称
     */
    @TableField("da30")
    @Schema(description = "DA30 - 第一职称")
    private String da30;

    /**
     * DA31 - 第一职称评定日期
     */
    @TableField("da31")
    @Schema(description = "DA31 - 第一职称评定日期")
    private String da31;

    /**
     * DA32 - 第二职称
     */
    @TableField("da32")
    @Schema(description = "DA32 - 第二职称")
    private String da32;

    /**
     * DA33 - 第二职称评定日期
     */
    @TableField("da33")
    @Schema(description = "DA33 - 第二职称评定日期")
    private String da33;

    /**
     * DA34 - 身份类型
     */
    @TableField("da34")
    @Schema(description = "DA34 - 身份类型")
    private String da34;

    /**
     * DA35 - 人员分类
     */
    @TableField("da35")
    @Schema(description = "DA35 - 人员分类")
    private String da35;

    /**
     * DA36 - 人员分类2
     */
    @TableField("da36")
    @Schema(description = "DA36 - 人员分类2")
    private String da36;

    /**
     * DA37 - 在岗状态
     */
    @TableField("da37")
    @Schema(description = "DA37 - 在岗状态")
    private String da37;

    /**
     * DA38 - 人员类别
     */
    @TableField("da38")
    @Schema(description = "DA38 - 人员类别")
    private String da38;

    /**
     * DA39 - 技能等级
     */
    @TableField("da39")
    @Schema(description = "DA39 - 技能等级")
    private String da39;

    /**
     * DA40 - 技能级别
     */
    @TableField("da40")
    @Schema(description = "DA40 - 技能级别")
    private String da40;

    /**
     * DA41 - 技能等级评定日期
     */
    @TableField("da41")
    @Schema(description = "DA41 - 技能等级评定日期")
    private String da41;

    /**
     * DA42 - 职称级别
     */
    @TableField("da42")
    @Schema(description = "DA42 - 职称级别")
    private String da42;

    /**
     * DA43 - 职称级别评定日期
     */
    @TableField("da43")
    @Schema(description = "DA43 - 职称级别评定日期")
    private String da43;

    /**
     * DA44 - 职称级别状态
     */
    @TableField("da44")
    @Schema(description = "DA44 - 职称级别状态")
    private String da44;

    /**
     * DA45 - 婚姻状况
     */
    @TableField("da45")
    @Schema(description = "DA45 - 婚姻状况")
    private String da45;

    /**
     * DA46 - 结婚日期
     */
    @TableField("da46")
    @Schema(description = "DA46 - 结婚日期")
    private String da46;

    /**
     * DA47 - 健康状况
     */
    @TableField("da47")
    @Schema(description = "DA47 - 健康状况")
    private String da47;

    /**
     * DA48 - 身高
     */
    @TableField("da48")
    @Schema(description = "DA48 - 身高")
    private String da48;

    /**
     * DA49 - 体重
     */
    @TableField("da49")
    @Schema(description = "DA49 - 体重")
    private String da49;

    /**
     * DA50 - 血型
     */
    @TableField("da50")
    @Schema(description = "DA50 - 血型")
    private String da50;

    /**
     * DA51 - 家庭住址
     */
    @TableField("da51")
    @Schema(description = "DA51 - 家庭住址")
    private String da51;

    /**
     * DA52 - 政治面貌
     */
    @TableField("da52")
    @Schema(description = "DA52 - 政治面貌")
    private String da52;

    /**
     * DA53 - 入党日期
     */
    @TableField("da53")
    @Schema(description = "DA53 - 入党日期")
    private String da53;

    /**
     * DA54 - 户籍类型
     */
    @TableField("da54")
    @Schema(description = "DA54 - 户籍类型")
    private String da54;

    /**
     * DA55 - 户籍所在地
     */
    @TableField("da55")
    @Schema(description = "DA55 - 户籍所在地")
    private String da55;

    /**
     * DA56 - 户籍所在地详细
     */
    @TableField("da56")
    @Schema(description = "DA56 - 户籍所在地详细")
    private String da56;

    /**
     * DA57 - 现居住地
     */
    @TableField("da57")
    @Schema(description = "DA57 - 现居住地")
    private String da57;

    /**
     * DA58 - 手机号码
     */
    @TableField("da58")
    @Schema(description = "DA58 - 手机号码")
    private String da58;

    /**
     * DA59 - 工资/档次
     */
    @TableField("da59")
    @Schema(description = "DA59 - 工资/档次")
    private String da59;

    /**
     * DA60 - 年龄（档案年龄）
     */
    @TableField("da60")
    @Schema(description = "DA60 - 年龄（档案年龄）")
    private String da60;

    /**
     * DA61 - 婚姻状况（档案）
     */
    @TableField("da61")
    @Schema(description = "DA61 - 婚姻状况（档案）")
    private String da61;

    /**
     * DA62 - 健康状况（档案）
     */
    @TableField("da62")
    @Schema(description = "DA62 - 健康状况（档案）")
    private String da62;

    /**
     * DA63 - 家庭住址（档案）
     */
    @TableField("da63")
    @Schema(description = "DA63 - 家庭住址（档案）")
    private String da63;

    /**
     * DA64 - 离职状态
     */
    @TableField("da64")
    @Schema(description = "DA64 - 离职状态")
    private String da64;

    /**
     * DA65 - 离职日期
     */
    @TableField("da65")
    @Schema(description = "DA65 - 离职日期")
    private String da65;

    /**
     * DA66 - 合同签订日期
     */
    @TableField("da66")
    @Schema(description = "DA66 - 合同签订日期")
    private String da66;

    /**
     * DA67 - 合同续订日期
     */
    @TableField("da67")
    @Schema(description = "DA67 - 合同续订日期")
    private String da67;

    /**
     * DA68 - 合同终止日期
     */
    @TableField("da68")
    @Schema(description = "DA68 - 合同终止日期")
    private String da68;

    /**
     * DA69 - 入职日期（档案）
     */
    @TableField("da69")
    @Schema(description = "DA69 - 入职日期（档案）")
    private String da69;

    /**
     * DA70 - 工号
     */
    @TableField("da70")
    @Schema(description = "DA70 - 工号")
    private String da70;

    /**
     * DA71 - 员工编号
     */
    @TableField("da71")
    @Schema(description = "DA71 - 员工编号")
    private String da71;

    /**
     * DA72 - 合同代码
     */
    @TableField("da72")
    @Schema(description = "DA72 - 合同代码")
    private String da72;

    /**
     * DA73 - 转正日期
     */
    @TableField("da73")
    @Schema(description = "DA73 - 转正日期")
    private String da73;

    /**
     * DA74 - 紧急联系人
     */
    @TableField("da74")
    @Schema(description = "DA74 - 紧急联系人")
    private String da74;

    /**
     * DA75 - 紧急联系人电话
     */
    @TableField("da75")
    @Schema(description = "DA75 - 紧急联系人电话")
    private String da75;

    /**
     * DA76 - 紧急联系人邮箱
     */
    @TableField("da76")
    @Schema(description = "DA76 - 紧急联系人邮箱")
    private String da76;

    /**
     * DA77 - 紧急联系人姓名
     */
    @TableField("da77")
    @Schema(description = "DA77 - 紧急联系人姓名")
    private String da77;

    /**
     * DA78 - 现任职务开始日期
     */
    @TableField("da78")
    @Schema(description = "DA78 - 现任职务开始日期")
    private String da78;

    /**
     * DA79 - 现任职务结束日期
     */
    @TableField("da79")
    @Schema(description = "DA79 - 现任职务结束日期")
    private String da79;

    /**
     * DA80 - 合同开始日期
     */
    @TableField("da80")
    @Schema(description = "DA80 - 合同开始日期")
    private String da80;

    /**
     * DA81 - 合同结束日期
     */
    @TableField("da81")
    @Schema(description = "DA81 - 合同结束日期")
    private String da81;

    /**
     * DA82 - 合同类型
     */
    @TableField("da82")
    @Schema(description = "DA82 - 合同类型")
    private String da82;

    /**
     * DA83 - 合同次数
     */
    @TableField("da83")
    @Schema(description = "DA83 - 合同次数")
    private String da83;

    /**
     * DA84 - 试用期开始日期
     */
    @TableField("da84")
    @Schema(description = "DA84 - 试用期开始日期")
    private String da84;

    /**
     * DA85 - 试用期结束日期
     */
    @TableField("da85")
    @Schema(description = "DA85 - 试用期结束日期")
    private String da85;

    /**
     * DA86 - 试用期月数
     */
    @TableField("da86")
    @Schema(description = "DA86 - 试用期月数")
    private String da86;

    /**
     * DA87 - 是否试用期
     */
    @TableField("da87")
    @Schema(description = "DA87 - 是否试用期")
    private String da87;

    /**
     * DA88 - 培训开始日期
     */
    @TableField("da88")
    @Schema(description = "DA88 - 培训开始日期")
    private String da88;

    /**
     * DA89 - 培训结束日期
     */
    @TableField("da89")
    @Schema(description = "DA89 - 培训结束日期")
    private String da89;

    /**
     * DA90 - 培训内容
     */
    @TableField("da90")
    @Schema(description = "DA90 - 培训内容")
    private String da90;

    /**
     * DA91 - 培训费用
     */
    @TableField("da91")
    @Schema(description = "DA91 - 培训费用")
    private String da91;

    /**
     * DA92 - 培训证书
     */
    @TableField("da92")
    @Schema(description = "DA92 - 培训证书")
    private String da92;

    /**
     * DA93 - 入职方式（档案）
     */
    @TableField("da93")
    @Schema(description = "DA93 - 入职方式（档案）")
    private String da93;

    /**
     * DA94 - 入职日期（档案）
     */
    @TableField("da94")
    @Schema(description = "DA94 - 入职日期（档案）")
    private String da94;

    /**
     * DA95 - 入职来源
     */
    @TableField("da95")
    @Schema(description = "DA95 - 入职来源")
    private String da95;

    /**
     * DA96 - 工号（档案）
     */
    @TableField("da96")
    @Schema(description = "DA96 - 工号（档案）")
    private String da96;

    /**
     * DA97 - 部门代码
     */
    @TableField("da97")
    @Schema(description = "DA97 - 部门代码")
    private String da97;

    /**
     * DA98 - 状态标识
     */
    @TableField("da98")
    @Schema(description = "DA98 - 状态标识")
    private String da98;

    // ==================== 非 DA 字段 ====================

    /**
     * NF - 年份
     */
    @TableField("nf")
    @Schema(description = "NF - 年份")
    private String nf;

    /**
     * YF - 月份
     */
    @TableField("yf")
    @Schema(description = "YF - 月份")
    private String yf;

    /**
     * C_TYPE - 类型
     */
    @TableField("c_type")
    @Schema(description = "C_TYPE - 类型")
    private String cType;

    /**
     * C_BEGIN_DATE - 开始日期
     */
    @TableField("c_begin_date")
    @Schema(description = "C_BEGIN_DATE - 开始日期")
    private String cBeginDate;

    /**
     * C_END_DATE - 结束日期
     */
    @TableField("c_end_date")
    @Schema(description = "C_END_DATE - 结束日期")
    private String cEndDate;

    /**
     * C_TYPE_ONE - 类型一
     */
    @TableField("c_type_one")
    @Schema(description = "C_TYPE_ONE - 类型一")
    private String cTypeOne;

    /**
     * C_TYPE_TWO - 类型二
     */
    @TableField("c_type_two")
    @Schema(description = "C_TYPE_TWO - 类型二")
    private String cTypeTwo;

    /**
     * C_TYPE_THREE - 类型三
     */
    @TableField("c_type_three")
    @Schema(description = "C_TYPE_THREE - 类型三")
    private String cTypeThree;

    /**
     * C_GRADE - 等级
     */
    @TableField("c_grade")
    @Schema(description = "C_GRADE - 等级")
    private String cGrade;

    /**
     * C_ZXGJ - 最高级别
     */
    @TableField("c_zxgj")
    @Schema(description = "C_ZXGJ - 最高级别")
    private String cZxgj;

    /**
     * TD_BEGIN_DATE - TD开始日期
     */
    @TableField("td_begin_date")
    @Schema(description = "TD_BEGIN_DATE - TD开始日期")
    private String tdBeginDate;

    /**
     * TD_END_DATE - TD结束日期
     */
    @TableField("td_end_date")
    @Schema(description = "TD_END_DATE - TD结束日期")
    private String tdEndDate;

    /**
     * DA2_1_IN - DA2_1输入
     */
    @TableField("da2_1_in")
    @Schema(description = "DA2_1_IN - DA2_1输入")
    private String da21In;

    /**
     * DA2_IN - DA2输入
     */
    @TableField("da2_in")
    @Schema(description = "DA2_IN - DA2输入")
    private String da2In;

    /**
     * A26 - A26字段
     */
    @TableField("a26")
    @Schema(description = "A26 - A26字段")
    private String a26;

    /**
     * JOB_ONE - 工作一
     */
    @TableField("job_one")
    @Schema(description = "JOB_ONE - 工作一")
    private String jobOne;

    /**
     * JOB_TWO - 工作二
     */
    @TableField("job_two")
    @Schema(description = "JOB_TWO - 工作二")
    private String jobTwo;

    /**
     * JOB_THREE - 工作三
     */
    @TableField("job_three")
    @Schema(description = "JOB_THREE - 工作三")
    private String jobThree;
}
