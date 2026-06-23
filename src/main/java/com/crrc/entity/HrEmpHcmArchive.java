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
import java.time.LocalDate;

/**
 * 员工历史档案数据实体类
 * 对应数据库表：hr_emp_hcm_archive
 *
 * <p>该实体类用于映射员工历史档案数据表，包含 DA1-DA98 共98个基础字段，
 * 以及 NF、YF、C_TYPE 等额外字段。主键为 userCode（对应 DA2 字段）。</p>
 *
 * <p>数据来源：tempdoc.txt 中的历史数据表</p>
 *
 * @author AI Generated
 * @since 2026-06-23
 */
@Accessors(chain = false)
@TableName("hr_emp_hcm_archive")
@Schema(description = "员工历史档案数据")
@Data
@NoArgsConstructor
public class HrEmpHcmArchive extends BaseEntity implements Serializable {

    /**
     * 员工档案号（DA2字段，主键）
     * 对应数据库字段：user_code
     * 数据来源：DA2 列
     */
    @TableId(value = "user_code", type = IdType.INPUT)
    @Schema(description = "员工档案号（DA2）")
    private String userCode;

    /**
     * 逻辑删除标记（0表示未删除，1表示已删除）
     * 对应数据库字段：deleted
     */
    @TableLogic(value = "0")
    @Schema(description = "逻辑删除标记")
    private Integer deleted;

    /**
     * 员工姓名（DA4字段）
     * 对应数据库字段：name
     * 数据来源：DA4 列
     */
    @Schema(description = "员工姓名（DA4）")
    private String name;

    /**
     * 部门编码（DA3字段）
     * 对应数据库字段：dept
     * 数据来源：DA3 列
     */
    @Schema(description = "部门编码（DA3）")
    private String dept;

    /**
     * 性别（DA6字段）
     * 对应数据库字段：gender
     * 数据来源：DA6 列
     */
    @Schema(description = "性别（DA6）")
    private String gender;

    /**
     * 民族（DA7字段）
     * 对应数据库字段：nation
     * 数据来源：DA7 列
     */
    @Schema(description = "民族（DA7）")
    private String nation;

    /**
     * 籍贯（DA8字段）
     * 对应数据库字段：native_place
     * 数据来源：DA8 列
     */
    @Schema(description = "籍贯（DA8）")
    private String nativePlace;

    /**
     * 出生日期（DA5字段）
     * 对应数据库字段：birth_date
     * 数据来源：DA5 列
     */
    @Schema(description = "出生日期（DA5）")
    private LocalDate birthDate;

    /**
     * 入职日期（DA16字段）
     * 对应数据库字段：work_date
     * 数据来源：DA16 列
     */
    @Schema(description = "入职日期（DA16）")
    private LocalDate workDate;

    /**
     * 政治面貌（DA28字段）
     * 对应数据库字段：political
     * 数据来源：DA28 列
     */
    @Schema(description = "政治面貌（DA28）")
    private String political;

    /**
     * 学历（DA22字段）
     * 对应数据库字段：education
     * 数据来源：DA22 列
     */
    @Schema(description = "学历（DA22）")
    private String education;

    /**
     * DA1 - 序号
     * 对应数据库字段：da1
     * 数据来源：DA1 列
     */
    @Schema(description = "DA1 - 序号")
    private String da1;

    /**
     * DA9 - 身份证号
     * 对应数据库字段：da9
     * 数据来源：DA9 列
     */
    @Schema(description = "DA9 - 身份证号")
    private String da9;

    /**
     * DA10 - 学历类型
     * 对应数据库字段：da10
     * 数据来源：DA10 列
     */
    @Schema(description = "DA10")
    private String da10;

    /**
     * DA11 - 毕业日期
     * 对应数据库字段：da11
     * 数据来源：DA11 列
     */
    @Schema(description = "DA11")
    private String da11;

    /**
     * DA12 - 专业
     * 对应数据库字段：da12
     * 数据来源：DA12 列
     */
    @Schema(description = "DA12")
    private String da12;

    /**
     * DA13 - 毕业院校
     * 对应数据库字段：da13
     * 数据来源：DA13 列
     */
    @Schema(description = "DA13")
    private String da13;

    /**
     * DA14 - 教育类型
     * 对应数据库字段：da14
     * 数据来源：DA14 列
     */
    @Schema(description = "DA14")
    private String da14;

    /**
     * DA15 - 学位授予日期
     * 对应数据库字段：da15
     * 数据来源：DA15 列
     */
    @Schema(description = "DA15")
    private String da15;

    /**
     * DA16 - 学位
     * 对应数据库字段：da16
     * 数据来源：DA16 列
     */
    @Schema(description = "DA16")
    private String da16;

    /**
     * DA17 - 第二学历开始日期
     * 对应数据库字段：da17
     * 数据来源：DA17 列
     */
    @Schema(description = "DA17")
    private String da17;

    /**
     * DA18 - 第二学历
     * 对应数据库字段：da18
     * 数据来源：DA18 列
     */
    @Schema(description = "DA18")
    private String da18;

    /**
     * DA19 - 第二学历专业
     * 对应数据库字段：da19
     * 数据来源：DA19 列
     */
    @Schema(description = "DA19")
    private String da19;

    /**
     * DA20 - 第二学历毕业院校
     * 对应数据库字段：da20
     * 数据来源：DA20 列
     */
    @Schema(description = "DA20")
    private String da20;

    /**
     * DA21 - 第二学历教育类型
     * 对应数据库字段：da21
     * 数据来源：DA21 列
     */
    @Schema(description = "DA21")
    private String da21;

    /**
     * DA22 - 第二学历学位授予日期
     * 对应数据库字段：da22
     * 数据来源：DA22 列
     */
    @Schema(description = "DA22")
    private String da22;

    /**
     * DA23 - 第二学历学位
     * 对应数据库字段：da23
     * 数据来源：DA23 列
     */
    @Schema(description = "DA23")
    private String da23;

    /**
     * DA24 - 参加工作时间
     * 对应数据库字段：da24
     * 数据来源：DA24 列
     */
    @Schema(description = "DA24")
    private String da24;

    /**
     * DA25 - 入职日期
     * 对应数据库字段：da25
     * 数据来源：DA25 列
     */
    @Schema(description = "DA25")
    private String da25;

    /**
     * DA26 - 工龄间断时间
     * 对应数据库字段：da26
     * 数据来源：DA26 列
     */
    @Schema(description = "DA26")
    private String da26;

    /**
     * DA27 - 岗位名称
     * 对应数据库字段：da27
     * 数据来源：DA27 列
     */
    @Schema(description = "DA27")
    private String da27;

    /**
     * DA28 - 岗位名称2
     * 对应数据库字段：da28
     * 数据来源：DA28 列
     */
    @Schema(description = "DA28")
    private String da28;

    /**
     * DA29 - 职称
     * 对应数据库字段：da29
     * 数据来源：DA29 列
     */
    @Schema(description = "DA29")
    private String da29;

    /**
     * DA30 - 职称评定日期
     * 对应数据库字段：da30
     * 数据来源：DA30 列
     */
    @Schema(description = "DA30")
    private String da30;

    /**
     * DA31 - 职称
     * 对应数据库字段：da31
     * 数据来源：DA31 列
     */
    @Schema(description = "DA31")
    private String da31;

    /**
     * DA32 - 职称评定日期
     * 对应数据库字段：da32
     * 数据来源：DA32 列
     */
    @Schema(description = "DA32")
    private String da32;

    /**
     * DA33 - 身份类型
     * 对应数据库字段：da33
     * 数据来源：DA33 列
     */
    @Schema(description = "DA33")
    private String da33;

    /**
     * DA34 - 人员分类
     * 对应数据库字段：da34
     * 数据来源：DA34 列
     */
    @Schema(description = "DA34")
    private String da34;

    /**
     * DA35 - 人员分类2
     * 对应数据库字段：da35
     * 数据来源：DA35 列
     */
    @Schema(description = "DA35")
    private String da35;

    /**
     * DA36 - 人员分类3
     * 对应数据库字段：da36
     * 数据来源：DA36 列
     */
    @Schema(description = "DA36")
    private String da36;

    /**
     * DA37 - 现任职务
     * 对应数据库字段：da37
     * 数据来源：DA37 列
     */
    @Schema(description = "DA37")
    private String da37;

    /**
     * DA38 - 现任职务开始日期
     * 对应数据库字段：da38
     * 数据来源：DA38 列
     */
    @Schema(description = "DA38")
    private String da38;

    /**
     * DA39 - 现任职务结束日期
     * 对应数据库字段：da39
     * 数据来源：DA39 列
     */
    @Schema(description = "DA39")
    private String da39;

    /**
     * DA40 - 在岗状态
     * 对应数据库字段：da40
     * 数据来源：DA40 列
     */
    @Schema(description = "DA40")
    private String da40;

    /**
     * DA41 - 人员类别
     * 对应数据库字段：da41
     * 数据来源：DA41 列
     */
    @Schema(description = "DA41")
    private String da41;

    /**
     * DA42 - 技能等级
     * 对应数据库字段：da42
     * 数据来源：DA42 列
     */
    @Schema(description = "DA42")
    private String da42;

    /**
     * DA43 - 技能等级评定日期
     * 对应数据库字段：da43
     * 数据来源：DA43 列
     */
    @Schema(description = "DA43")
    private String da43;

    /**
     * DA44 - 技能等级
     * 对应数据库字段：da44
     * 数据来源：DA44 列
     */
    @Schema(description = "DA44")
    private String da44;

    /**
     * DA45 - 技能等级评定日期
     * 对应数据库字段：da45
     * 数据来源：DA45 列
     */
    @Schema(description = "DA45")
    private String da45;

    /**
     * DA46 - 婚姻状况
     * 对应数据库字段：da46
     * 数据来源：DA46 列
     */
    @Schema(description = "DA46")
    private String da46;

    /**
     * DA47 - 结婚日期
     * 对应数据库字段：da47
     * 数据来源：DA47 列
     */
    @Schema(description = "DA47")
    private String da47;

    /**
     * DA48 - 健康状况
     * 对应数据库字段：da48
     * 数据来源：DA48 列
     */
    @Schema(description = "DA48")
    private String da48;

    /**
     * DA49 - 身高
     * 对应数据库字段：da49
     * 数据来源：DA49 列
     */
    @Schema(description = "DA49")
    private String da49;

    /**
     * DA50 - 体重
     * 对应数据库字段：da50
     * 数据来源：DA50 列
     */
    @Schema(description = "DA50")
    private String da50;

    /**
     * DA51 - 血型
     * 对应数据库字段：da51
     * 数据来源：DA51 列
     */
    @Schema(description = "DA51")
    private String da51;

    /**
     * DA52 - 家庭住址
     * 对应数据库字段：da52
     * 数据来源：DA52 列
     */
    @Schema(description = "DA52")
    private String da52;

    /**
     * DA53 - 家庭住址详细
     * 对应数据库字段：da53
     * 数据来源：DA53 列
     */
    @Schema(description = "DA53")
    private String da53;

    /**
     * DA54 - 户籍类型
     * 对应数据库字段：da54
     * 数据来源：DA54 列
     */
    @Schema(description = "DA54")
    private String da54;

    /**
     * DA55 - 户籍所在地
     * 对应数据库字段：da55
     * 数据来源：DA55 列
     */
    @Schema(description = "DA55")
    private String da55;

    /**
     * DA56 - 户籍所在地详细
     * 对应数据库字段：da56
     * 数据来源：DA56 列
     */
    @Schema(description = "DA56")
    private String da56;

    /**
     * DA57 - 现居住地
     * 对应数据库字段：da57
     * 数据来源：DA57 列
     */
    @Schema(description = "DA57")
    private String da57;

    /**
     * DA58 - 手机号码
     * 对应数据库字段：da58
     * 数据来源：DA58 列
     */
    @Schema(description = "DA58")
    private String da58;

    /**
     * DA59 - 个人邮箱
     * 对应数据库字段：da59
     * 数据来源：DA59 列
     */
    @Schema(description = "DA59")
    private String da59;

    /**
     * DA60 - 工作邮箱
     * 对应数据库字段：da60
     * 数据来源：DA60 列
     */
    @Schema(description = "DA60")
    private String da60;

    /**
     * DA61 - 紧急联系人
     * 对应数据库字段：da61
     * 数据来源：DA61 列
     */
    @Schema(description = "DA61")
    private String da61;

    /**
     * DA62 - 紧急联系人电话
     * 对应数据库字段：da62
     * 数据来源：DA62 列
     */
    @Schema(description = "DA62")
    private String da62;

    /**
     * DA63 - 银行卡号
     * 对应数据库字段：da63
     * 数据来源：DA63 列
     */
    @Schema(description = "DA63")
    private String da63;

    /**
     * DA64 - 开户行
     * 对应数据库字段：da64
     * 数据来源：DA64 列
     */
    @Schema(description = "DA64")
    private String da64;

    /**
     * DA65 - 社保账号
     * 对应数据库字段：da65
     * 数据来源：DA65 列
     */
    @Schema(description = "DA65")
    private String da65;

    /**
     * DA66 - 公积金账号
     * 对应数据库字段：da66
     * 数据来源：DA66 列
     */
    @Schema(description = "DA66")
    private String da66;

    /**
     * DA67 - 个税账号
     * 对应数据库字段：da67
     * 数据来源：DA67 列
     */
    @Schema(description = "DA67")
    private String da67;

    /**
     * DA68 - 工号
     * 对应数据库字段：da68
     * 数据来源：DA68 列
     */
    @Schema(description = "DA68")
    private String da68;

    /**
     * DA69 - 员工编号
     * 对应数据库字段：da69
     * 数据来源：DA69 列
     */
    @Schema(description = "DA69")
    private String da69;

    /**
     * DA70 - 入职方式
     * 对应数据库字段：da70
     * 数据来源：DA70 列
     */
    @Schema(description = "DA70")
    private String da70;

    /**
     * DA71 - 入职来源
     * 对应数据库字段：da71
     * 数据来源：DA71 列
     */
    @Schema(description = "DA71")
    private String da71;

    /**
     * DA72 - 转正日期
     * 对应数据库字段：da72
     * 数据来源：DA72 列
     */
    @Schema(description = "DA72")
    private String da72;

    /**
     * DA73 - 离职日期
     * 对应数据库字段：da73
     * 数据来源：DA73 列
     */
    @Schema(description = "DA73")
    private String da73;

    /**
     * DA74 - 离职原因
     * 对应数据库字段：da74
     * 数据来源：DA74 列
     */
    @Schema(description = "DA74")
    private String da74;

    /**
     * DA75 - 离职类型
     * 对应数据库字段：da75
     * 数据来源：DA75 列
     */
    @Schema(description = "DA75")
    private String da75;

    /**
     * DA76 - 退休日期
     * 对应数据库字段：da76
     * 数据来源：DA76 列
     */
    @Schema(description = "DA76")
    private String da76;

    /**
     * DA77 - 退休类型
     * 对应数据库字段：da77
     * 数据来源：DA77 列
     */
    @Schema(description = "DA77")
    private String da77;

    /**
     * DA78 - 退休原因
     * 对应数据库字段：da78
     * 数据来源：DA78 列
     */
    @Schema(description = "DA78")
    private String da78;

    /**
     * DA79 - 返聘日期
     * 对应数据库字段：da79
     * 数据来源：DA79 列
     */
    @Schema(description = "DA79")
    private String da79;

    /**
     * DA80 - 返聘原因
     * 对应数据库字段：da80
     * 数据来源：DA80 列
     */
    @Schema(description = "DA80")
    private String da80;

    /**
     * DA81 - 合同开始日期
     * 对应数据库字段：da81
     * 数据来源：DA81 列
     */
    @Schema(description = "DA81")
    private String da81;

    /**
     * DA82 - 合同结束日期
     * 对应数据库字段：da82
     * 数据来源：DA82 列
     */
    @Schema(description = "DA82")
    private String da82;

    /**
     * DA83 - 合同类型
     * 对应数据库字段：da83
     * 数据来源：DA83 列
     */
    @Schema(description = "DA83")
    private String da83;

    /**
     * DA84 - 合同次数
     * 对应数据库字段：da84
     * 数据来源：DA84 列
     */
    @Schema(description = "DA84")
    private String da84;

    /**
     * DA85 - 试用期开始日期
     * 对应数据库字段：da85
     * 数据来源：DA85 列
     */
    @Schema(description = "DA85")
    private String da85;

    /**
     * DA86 - 试用期结束日期
     * 对应数据库字段：da86
     * 数据来源：DA86 列
     */
    @Schema(description = "DA86")
    private String da86;

    /**
     * DA87 - 试用期月数
     * 对应数据库字段：da87
     * 数据来源：DA87 列
     */
    @Schema(description = "DA87")
    private String da87;

    /**
     * DA88 - 培训开始日期
     * 对应数据库字段：da88
     * 数据来源：DA88 列
     */
    @Schema(description = "DA88")
    private String da88;

    /**
     * DA89 - 培训结束日期
     * 对应数据库字段：da89
     * 数据来源：DA89 列
     */
    @Schema(description = "DA89")
    private String da89;

    /**
     * DA90 - 培训内容
     * 对应数据库字段：da90
     * 数据来源：DA90 列
     */
    @Schema(description = "DA90")
    private String da90;

    /**
     * DA91 - 培训费用
     * 对应数据库字段：da91
     * 数据来源：DA91 列
     */
    @Schema(description = "DA91")
    private String da91;

    /**
     * DA92 - 培训证书
     * 对应数据库字段：da92
     * 数据来源：DA92 列
     */
    @Schema(description = "DA92")
    private String da92;

    /**
     * DA93 - 奖惩日期
     * 对应数据库字段：da93
     * 数据来源：DA93 列
     */
    @Schema(description = "DA93")
    private String da93;

    /**
     * DA94 - 奖惩类型
     * 对应数据库字段：da94
     * 数据来源：DA94 列
     */
    @Schema(description = "DA94")
    private String da94;

    /**
     * DA95 - 奖惩原因
     * 对应数据库字段：da95
     * 数据来源：DA95 列
     */
    @Schema(description = "DA95")
    private String da95;

    /**
     * DA96 - 奖惩内容
     * 对应数据库字段：da96
     * 数据来源：DA96 列
     */
    @Schema(description = "DA96")
    private String da96;

    /**
     * DA97 - 奖惩金额
     * 对应数据库字段：da97
     * 数据来源：DA97 列
     */
    @Schema(description = "DA97")
    private String da97;

    /**
     * DA98 - 备注
     * 对应数据库字段：da98
     * 数据来源：DA98 列
     */
    @Schema(description = "DA98")
    private String da98;

    /**
     * NF - 年份
     * 对应数据库字段：nf
     * 数据来源：NF 列
     */
    @Schema(description = "NF - 年份")
    private String nf;

    /**
     * YF - 月份
     * 对应数据库字段：yf
     * 数据来源：YF 列
     */
    @Schema(description = "YF - 月份")
    private String yf;

    /**
     * C_TYPE - 类型
     * 对应数据库字段：c_type
     * 数据来源：C_TYPE 列
     */
    @Schema(description = "C_TYPE - 类型")
    private String cType;

    /**
     * C_BEGIN_DATE - 开始日期
     * 对应数据库字段：c_begin_date
     * 数据来源：C_BEGIN_DATE 列
     */
    @Schema(description = "C_BEGIN_DATE - 开始日期")
    private LocalDate cBeginDate;

    /**
     * C_END_DATE - 结束日期
     * 对应数据库字段：c_end_date
     * 数据来源：C_END_DATE 列
     */
    @Schema(description = "C_END_DATE - 结束日期")
    private LocalDate cEndDate;

    /**
     * C_TYPE_ONE - 类型一
     * 对应数据库字段：c_type_one
     * 数据来源：C_TYPE_ONE 列
     */
    @Schema(description = "C_TYPE_ONE - 类型一")
    private String cTypeOne;

    /**
     * C_TYPE_TWO - 类型二
     * 对应数据库字段：c_type_two
     * 数据来源：C_TYPE_TWO 列
     */
    @Schema(description = "C_TYPE_TWO - 类型二")
    private String cTypeTwo;

    /**
     * C_TYPE_THREE - 类型三
     * 对应数据库字段：c_type_three
     * 数据来源：C_TYPE_THREE 列
     */
    @Schema(description = "C_TYPE_THREE - 类型三")
    private String cTypeThree;

    /**
     * C_GRADE - 等级
     * 对应数据库字段：c_grade
     * 数据来源：C_GRADE 列
     */
    @Schema(description = "C_GRADE - 等级")
    private String cGrade;

    /**
     * C_ZXGJ - 最高级别
     * 对应数据库字段：c_zxgj
     * 数据来源：C_ZXGJ 列
     */
    @Schema(description = "C_ZXGJ - 最高级别")
    private String cZxgj;

    /**
     * TD_BEGIN_DATE - TD开始日期
     * 对应数据库字段：td_begin_date
     * 数据来源：TD_BEGIN_DATE 列
     */
    @Schema(description = "TD_BEGIN_DATE - TD开始日期")
    private LocalDate tdBeginDate;

    /**
     * TD_END_DATE - TD结束日期
     * 对应数据库字段：td_end_date
     * 数据来源：TD_END_DATE 列
     */
    @Schema(description = "TD_END_DATE - TD结束日期")
    private LocalDate tdEndDate;

    /**
     * DA2_1_IN - DA2_1输入
     * 对应数据库字段：da2_1_in
     * 数据来源：DA2_1_IN 列
     *
     * <p>注意：该字段数据库列名含有连续数字和下划线（da2_1_in），
     * 默认驼峰转下划线规则无法正确映射，因此需要显式指定 @TableField。</p>
     */
    @TableField("da2_1_in")
    @Schema(description = "DA2_1_IN")
    private String da21In;

    /**
     * DA2_IN - DA2输入
     * 对应数据库字段：da2_in
     * 数据来源：DA2_IN 列
     *
     * <p>注意：该字段数据库列名含有连续数字和下划线（da2_in），
     * 为确保映射正确性，显式指定 @TableField。</p>
     */
    @TableField("da2_in")
    @Schema(description = "DA2_IN")
    private String da2In;

    /**
     * A26 - A26字段
     * 对应数据库字段：a26
     * 数据来源：A26 列
     */
    @Schema(description = "A26")
    private String a26;

    /**
     * JOB_ONE - 工作一
     * 对应数据库字段：job_one
     * 数据来源：JOB_ONE 列
     */
    @Schema(description = "JOB_ONE - 工作一")
    private String jobOne;

    /**
     * JOB_TWO - 工作二
     * 对应数据库字段：job_two
     * 数据来源：JOB_TWO 列
     */
    @Schema(description = "JOB_TWO - 工作二")
    private String jobTwo;

    /**
     * JOB_THREE - 工作三
     * 对应数据库字段：job_three
     * 数据来源：JOB_THREE 列
     */
    @Schema(description = "JOB_THREE - 工作三")
    private String jobThree;
}
