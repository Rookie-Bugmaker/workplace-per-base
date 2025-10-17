package com.crrc.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author yukun
 */
@Data
public class PostCatalogInfoVO {
    // TODO: 这个类仅做职级职群职种查询测试使用，可以删除
    @TableId(value = "id", type = IdType.AUTO)
    Long id;
    /**
     * 父亲id
     */
    Long parent_id;
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
