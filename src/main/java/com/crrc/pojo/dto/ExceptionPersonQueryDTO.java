package com.crrc.pojo.dto;


import lombok.Data;

@Data
public class ExceptionPersonQueryDTO {

    /**
     * 档案号
     */
    private String userCode;

    /**
     * 姓名
     */
    private String name;

    /**
     *  部门名称
     */
    private String deptName;

    /**
     * 部门编码
     */
    private String dept;

    /**
     * 公司名称
     */
    private String orgName;

    /**
     * 公司编码
     */
    private String org;
}
