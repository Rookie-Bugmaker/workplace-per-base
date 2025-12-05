package com.crrc.pojo.vo;

import lombok.Data;

/**
 * @author yukun
 */
@Data
public class PostCatalogInfoVO {
    /**
     * 职类
     */
    String categoryName;
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
