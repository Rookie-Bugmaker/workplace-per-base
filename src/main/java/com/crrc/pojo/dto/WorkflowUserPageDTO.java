package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 工作流审批人
 *
 * @author 谭永强
 * @date 2025-09-18
 */
@Data
public class WorkflowUserPageDTO {

    /**
     * 数据总条数
     */
    private Long total;
    /**
     * 当前页数据
     */
    private List<SysUserDTO> rows;
}
