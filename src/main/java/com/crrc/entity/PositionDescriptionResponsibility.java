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
 * 职位说明书-职位职责子表。
 */
@Data
@TableName("position_description_responsibility")
@Accessors(chain = false)
@Schema(description = "职位说明书-职位职责")
@NoArgsConstructor
public class PositionDescriptionResponsibility extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;

    @Schema(description = "职位说明书ID")
    private Long descriptionId;

    @Schema(description = "职责名称")
    private String responsibilityName;

    @Schema(description = "职责描述")
    private String responsibilityDesc;

    @Schema(description = "排序号")
    private Integer sortNo;
}
