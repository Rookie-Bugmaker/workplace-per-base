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
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document
public class HrEmpBasic extends BaseEntity implements Serializable {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    @Id
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
    @ExcelProperty(value = {"工号"})
    @Schema(description = "工号")
    private String userCode;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"性别"})
    @Schema(description = "性别")
    private String gender;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"民族"})
    @Schema(description = "民族")
    private String nation;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"籍贯名称"})
    @Schema(description = "籍贯名称")
    private String nativePlaceName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"国籍"})
    @Schema(description = "国籍")
    private String nationality;

    @ExcelProperty(value = {"出生日期"})
    @Schema(description = "出生日期")
    private LocalDate birthDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"年龄"})
    @Schema(description = "年龄")
    private Integer age;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出生地"})
    @Schema(description = "出生地")
    private String birthPlace;
    
    @QueryMethod(method = QueryType.EQ)
    private String political;

    @TableField(exist = false)
    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "政治面貌")
    @ExcelProperty(value = {"政治面貌"})
    private String politicalValue;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"姓名"})
    @Schema(description = "姓名")
    private String name;

    @ExcelProperty(value = {"加入党组织时间"})
    @Schema(description = "加入党组织时间")
    private LocalDate partyJoinDate;


    @ExcelProperty(value = {"开始工作日期"})
    @Schema(description = "开始工作日期")
    private LocalDate workDate;


    @ExcelProperty(value = {"入司日期"})
    @Schema(description = "入司日期")
    private LocalDate entryDate;

    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"员工来源"})
    @Schema(description = "员工来源")
    private String empSource;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"婚姻状况"})
    @Schema(description = "婚姻状况")
    private String maritalStatus;

    /**
     * 001 干 002 工 一定要按001、002、003...顺序增加， 否则TSDA会出错
     */
    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "身份类型")
    private String identityType;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职务层级"})
    @Schema(description = "职务层级")
    private String empJob;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"职级层级"})
    @Schema(description = "职级层级")
    private String empRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"校招届数"})
    @Schema(description = "校招届数")
    private String campusPeriod;


	@QueryMethod(method = QueryType.EQ)
	@ExcelProperty(value = {"入职日期"})
	@Schema(description = "入职日期")
	private LocalDate hireDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"籍贯编码"})
    @Schema(description = "籍贯编码")
    private String nativePlaceCode;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"部门"})
    @Schema(description = "部门")
    private String dept;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"成本中心"})
    @Schema(description = "成本中心")
    private String costCenter;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类1"})
    @Schema(description = "人员分类1")
    private String category1;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类4"})
    @Schema(description = "人员分类4")
    private String category4;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类7"})
    @Schema(description = "人员分类7")
    private String category7;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员分类8"})
    @Schema(description = "人员分类8")
    private String category8;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员在岗状态"})
    @Schema(description = "人员在岗状态，和任职信息中的在岗状态实际上相同，这里仅仅用于存储并展示")
    private String status;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"入伍时间"})
    @Schema(description = "入伍时间")
    private LocalDate enlistmentDate;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"曾用名"})
    @Schema(description = "曾用名")
    private String formerName;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"提中干时间"})
    @Schema(description = "提中干时间")
    private LocalDate promotionTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"死亡时间"})
    @Schema(description = "死亡时间")
    private LocalDate deathTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工龄间断时间"})
    @Schema(description = "工龄间断时间")
    private String serviceBreakTime;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否离职标记"})
    @Schema(description = "是否离职标记，0为离职，1为在职")
    private String isEmployed;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"人员类别"})
    @Schema(description = "人员类别：校社京博工挂他临")
    private String empType;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "用户图片的文件id")
    private String profilePicture;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "用户人脸识别（考勤打卡）的图片id")
    private String attendanceImageId;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "图片是否正在更新")
    private String isPicUpdating;

    // 只有中干的有职务，来自干部任免令
    // 人力业务人员手动输入
    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职务")
    private String jobTitle;

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "职位层级")
    private String positionLevel;
}
