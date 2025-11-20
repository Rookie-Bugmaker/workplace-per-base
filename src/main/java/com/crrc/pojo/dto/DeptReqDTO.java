package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeptReqDTO {
    private List<Long> companyIds;
    private List<Long> deptIds;
    private String keywords;
}