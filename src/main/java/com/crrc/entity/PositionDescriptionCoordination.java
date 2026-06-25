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
 * 职位说明书-协调内容子表。
 */
@Data
@TableName("position_description_coordination")
@Accessors(chain = false)
@Schema(description = "职位说明书-协调内容")
@NoArgsConstructor
public class PositionDescriptionCoordination extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;

    @Schema(description = "职位说明书ID")
    private Long descriptionId;

    @Schema(description = "协调类型")
    private String coordinationType;

    @Schema(description = "协调对象")
    private String coordinationObject;

    @Schema(description = "协调内容")
    private String coordinationContent;

    @Schema(description = "排序号")
    private Integer sortNo;
}
