package com.crrc.pojo.vo;

import lombok.Data;

/**
 * 职位在岗人员查询结果。
 */
@Data
public class PositionIncumbentDTO {
    /** 职位编码 */
    private String positionCode;

    /** 工号/档案号 */
    private String userCode;

    /** 姓名 */
    private String userName;
}
