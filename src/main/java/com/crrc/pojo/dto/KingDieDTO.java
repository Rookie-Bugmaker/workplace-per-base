package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class KingDieDTO {
    /**
     * 人员在岗状态
     * 在岗 1
     * 因病因伤长期休假人员 2
     * 内退-其他内退人员 3
     */
    private String status;

    private String userCode;

    private List<KingDiePersonalOrderDTO> dtoList;
}
