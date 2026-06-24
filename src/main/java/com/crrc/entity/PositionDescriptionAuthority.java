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
 * 职位说明书-工作权限子表。
 */
@Data
@TableName("position_description_authority")
@Accessors(chain = false)
@Schema(description = "职位说明书-工作权限")
@NoArgsConstructor
public class PositionDescriptionAuthority extends BaseEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;

    @Schema(description = "职位说明书ID")
    private Long descriptionId;

    @Schema(description = "工作权限")
    private String authorityContent;

    @Schema(description = "排序号")
    private Integer sortNo;
}
