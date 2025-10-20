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
 * @author yukun
 */
@Data
@TableName("post_catalog_info")
@Schema(description = "中国中车职类职群职种信息")
@NoArgsConstructor
@Accessors(chain = false)
public class PostCatalogInfo extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "id")
    private Long id;

    @TableLogic(value = "0")
    private Integer deleted;
    /**
     * 父亲id
     */
    @Schema(description = "父亲id")
    private Long parentId;
    /**
     * 1: 职类; 2: 职群; 3: 职种
     */
    @Schema(description = "节点level")
    private String nodeLevel;
    /**
     * 名称
     */
    @Schema(description = "节点名称")
    private String name;
    /**
     * 编码
     */
    @Schema(description = "节点编码")
    private String code;
}
