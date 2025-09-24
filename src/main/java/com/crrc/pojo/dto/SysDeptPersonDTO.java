package com.crrc.pojo.dto;

import lombok.Data;

/**
 * 部门人员信息
 *
 * @author 谭永强
 * @date 2025-09-18
 */
@Data
public class SysDeptPersonDTO {

    /**
     * 公司ID
     */
    private Long companyId;

    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 人员信息
     */
    private Long personId;
    /**
     * 人员姓名
     */
    private String personName;
    /**
     * 职务
     */
    private String position;
}
