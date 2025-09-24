package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class SysUserRequestDTO {


    /**
     * 角色codes
     */
    private List<String> roleCodes;

    /**
     * 部门编码
     */
    private String orgCode;

}
