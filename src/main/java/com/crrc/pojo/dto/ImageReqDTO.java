package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageReqDTO {
    /**
     * 人员工号列表
     */
    List<String> uuidList;
}
