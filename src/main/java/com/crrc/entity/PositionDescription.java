package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 职位说明书主表。
 */
@Data
@TableName("position_description")
@Accessors(chain = false)
@Schema(description = "职位说明书")
@NoArgsConstructor
public class PositionDescription extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic(value = "0")
    @Schema(description = "删除标记")
    private Integer deleted;

    @Schema(description = "职位编码")
    @QueryMethod(method = QueryType.EQ)
    private String positionCode;

    @Schema(description = "职位名称")
    @QueryMethod(method = QueryType.LIKE)
    private String positionName;

    @Schema(description = "版本")
    @QueryMethod(method = QueryType.EQ)
    private String version;

    @Schema(description = "生效日期")
    @QueryMethod(method = QueryType.EQ)
    private LocalDate effectiveDate;

    @Schema(description = "撤销日期")
    @QueryMethod(method = QueryType.EQ)
    private LocalDate revocationDate;

    @Schema(description = "所属组织单元")
    @QueryMethod(method = QueryType.EQ)
    private String orgUnit;

    @Schema(description = "职类")
    @QueryMethod(method = QueryType.EQ)
    private String jobCategory;

    @Schema(description = "职群")
    @QueryMethod(method = QueryType.EQ)
    private String jobGroup;

    @Schema(description = "职种")
    @QueryMethod(method = QueryType.EQ)
    private String jobType;

    @Schema(description = "职层")
    @QueryMethod(method = QueryType.EQ)
    private String jobLevel;

    @Schema(description = "工作地点")
    @QueryMethod(method = QueryType.LIKE)
    private String workPlace;

    @Schema(description = "工作内容")
    @QueryMethod(method = QueryType.LIKE)
    private String workContent;

    @Schema(description = "标准职级")
    @QueryMethod(method = QueryType.EQ)
    private String jobGrade;

    @Schema(description = "编制人数")
    private Integer headcountQuota;

    @Schema(description = "控编模式")
    @QueryMethod(method = QueryType.EQ)
    private String controlMode;

    @Schema(description = "直接上级职位编码")
    @QueryMethod(method = QueryType.EQ)
    private String directSuperiorPositionCode;

    @Schema(description = "直接上级职位名称")
    @QueryMethod(method = QueryType.LIKE)
    private String directSuperiorPositionName;

    @Schema(description = "直接下级职位编码，多个逗号分隔")
    private String directSubordinatePositionCodes;

    @Schema(description = "直接下级职位名称，多个逗号分隔")
    private String directSubordinatePositionNames;

    @Schema(description = "职位定位")
    private String positionPurpose;

    @Schema(description = "工作环境")
    private String workEnvironment;

    @Schema(description = "任职资格")
    private String qualification;

    @Schema(description = "所需知识")
    private String requiredKnowledge;

    @Schema(description = "状态：DRAFT草稿，APPROVING审批中，REJECTED已驳回，PUBLISHED已发布，DISABLED已停用")
    @QueryMethod(method = QueryType.EQ)
    private String status;

    @Schema(description = "Flowable流程实例ID")
    private String procInstId;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "发布人")
    private Long publishBy;

    @Schema(description = "是否当前最新版本：1是，0否")
    @QueryMethod(method = QueryType.EQ)
    private Integer latestFlag;
}
