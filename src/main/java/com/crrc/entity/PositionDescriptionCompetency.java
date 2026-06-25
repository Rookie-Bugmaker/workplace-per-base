package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 职位说明书-能力要求子表。
 */
@Data
@TableName("position_description_competency")
@Accessors(chain = false)
@Schema(description = "职位说明书-能力要求")
@NoArgsConstructor
public class PositionDescriptionCompetency extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;

    @Schema(description = "职位说明书ID")
    private Long descriptionId;

    @Schema(description = "能力类别")
    private String competencyCategory;

    @Schema(description = "能力名称")
    private String competencyName;

    @Schema(description = "能力等级要求")
    private String competencyLevel;

    @Schema(description = "排序号")
    private Integer sortNo;
}
