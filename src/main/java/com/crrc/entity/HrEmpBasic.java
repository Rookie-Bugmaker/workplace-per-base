package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 员工基本信息表
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_basic")
@Schema(description = "员工基本信息表")
@Data
@NoArgsConstructor
public class HrEmpBasic extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @Schema(description = "备注", example = "demo")
    @QueryMethod(method = QueryType.LIKE)
    @TableField(exist = false)
    private String remark;

    @Schema(description = "所属部门id", example = "1")
    @TableField(exist = false)
    private Long orgId;

    @Schema(description = "所属部门编码", example = "0132")
    @TableField(exist = false)
    private String orgCode;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"性别"}, index = 2)
    @Schema(description = "性别")
    private String gender;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"民族"}, index = 3)
    @Schema(description = "民族")
    private String nation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"籍贯名称"}, index = 4)
    @Schema(description = "籍贯名称")
    private String nativePlaceName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"国籍"}, index = 5)
    @Schema(description = "国籍")
    private String nationality;

    @ExcelProperty(value = {"出生日期"}, index = 6)
    @Schema(description = "出生日期")
    private LocalDate birthDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"年龄"}, index = 7)
    @Schema(description = "年龄")
    private Integer age;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出生地"}, index = 8)
    @Schema(description = "出生地")
    private String birthPlace;
    
    @QueryMethod(method = QueryType.EQ)
    private String political;

    @TableField(exist = false)
    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "政治面貌")
    @ExcelProperty(value = {"政治面貌"}, index = 9)
    private String politicalValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"}, index = 10)
    @Schema(description = "姓名")
    private String name;

    @ExcelProperty(value = {"加入党组织时间"}, index = 11)
    @Schema(description = "加入党组织时间")
    private LocalDate partyJoinDate;


    @ExcelProperty(value = {"开始工作日期"}, index = 12)
    @Schema(description = "开始工作日期")
    private LocalDate workDate;


    @ExcelProperty(value = {"入司日期"}, index = 13)
    @Schema(description = "入司日期")
    private LocalDate entryDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"员工来源"}, index = 14)
    @Schema(description = "员工来源")
    private String empSource;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"婚姻状况"}, index = 15)
    @Schema(description = "婚姻状况")
    private String maritalStatus;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"身份类型"}, index = 16)
    @Schema(description = "身份类型")
    /**
     * 001 干 002 工 一定要按001、002、003...顺序增加， 否则TSDA会出错
     */
    private String identityType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务层级"}, index = 17)
    @Schema(description = "职务层级")
    private String empJob;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职级层级"}, index = 18)
    @Schema(description = "职级层级")
    private String empRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"校招届数"}, index = 19)
    @Schema(description = "校招届数")
    private String campusPeriod;


	@QueryMethod(method = QueryType.EQ)
	@ExcelProperty(value = {"入职日期"}, index = 20)
	@Schema(description = "入职日期")
	private LocalDate hireDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"籍贯编码"}, index = 21)
    @Schema(description = "籍贯编码")
    private String nativePlaceCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"}, index = 22)
    @Schema(description = "部门")
    private String dept;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"成本中心"}, index = 23)
    @Schema(description = "成本中心")
    private String costCenter;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"}, index = 24)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类1"}, index = 25)
    @Schema(description = "人员分类1")
    private String category1;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类4"}, index = 26)
    @Schema(description = "人员分类4")
    private String category4;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类7"}, index = 27)
    @Schema(description = "人员分类7")
    private String category7;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类8"}, index = 28)
    @Schema(description = "人员分类8")
    private String category8;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员在岗状态"}, index = 29)
    @Schema(description = "人员在岗状态，和任职信息中的在岗状态实际上相同，这里仅仅用于存储并展示")
    private String status;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"入伍时间"}, index = 30)
    @Schema(description = "入伍时间")
    private LocalDate enlistmentDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"曾用名"}, index = 31)
    @Schema(description = "曾用名")
    private String formerName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"提中干时间"}, index = 32)
    @Schema(description = "提中干时间")
    private LocalDate promotionTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"死亡时间"}, index = 33)
    @Schema(description = "死亡时间")
    private LocalDate deathTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工龄间断时间"}, index = 34)
    @Schema(description = "工龄间断时间")
    private String serviceBreakTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否离职标记"}, index = 35)
    @Schema(description = "是否离职标记，0为离职，1为在职")
    private String isEmployed;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员类别"}, index = 36)
    @Schema(description = "人员类别：校社京博工挂他临")
    private String empType;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件挂载Id"}, index = 37)
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"用户图片的文件id"}, index = 38)
    @Schema(description = "用户图片的文件id")
    private String profilePicture;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"用户人脸识别（考勤打卡）的图片id"}, index = 39)
    @Schema(description = "用户人脸识别（考勤打卡）的图片id")
    private String attendanceImageId;
}
