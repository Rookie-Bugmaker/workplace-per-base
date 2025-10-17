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
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    /**
     * 职类
     */
    String category;
    /**
     * 职类编码
     */
    String categoryCode;
    /**
     * 职群
     */
    String groupName;
    /**
     * 职群编码
     */
    String groupCode;
    /**
     * 职种
     */
    String typeName;
    /**
     * 职种编码
     */
    String typeCode;
}
