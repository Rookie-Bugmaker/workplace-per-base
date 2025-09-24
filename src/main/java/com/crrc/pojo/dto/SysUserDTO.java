package com.crrc.pojo.dto;

import lombok.Data;

/**
 * 用户信息
 * 注意：此类用于【配置文件且配置分类=审批人列表】使用，此类定义的响应字段必须与配置文件中的【值列】、【编码列】、【描述】一致
 *
 * @author 谭永强
 * @date 2025-09-18
 */
@Data
public class SysUserDTO {

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 姓名
     */
    private String fullName;
    /**
     * 人员ID
     */
    private Long userRelationId;
}
