package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yukun
 */
@Data
@TableName("post_catalog_info")
@Schema(description = "中国中车职类职群职种信息")
@NoArgsConstructor
public class PostCatalogInfo extends BaseEntity {
    @TableId(value = "id", type = IdType.INPUT)
    Long id;
    /**
     * 父亲id
     */
    Long parentId;
    /**
     * 1: 职类; 2: 职群; 3: 职种
     */
    String nodeLevel;
    /**
     * 名称
     */
    String name;
    /**
     * 编码
     */
    String code;
}
