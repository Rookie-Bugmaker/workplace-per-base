package com.crrc.pojo.dto;

import lombok.Data;

import java.util.Map;

/**
 * 工作流回写数参数对象
 *
 * @author 谭永强
 * @date 2025-09-15
 */
@Data
public class WorkflowWriteBackParamDTO {

    /**
     * 业务主键id
     */
    private String bizId;

    /**
     * 是否低代码配置
     */
    private Boolean lowCodeFlag;

    /**
     * 流程模板id
     */
    private String wfId;

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 低代码ObjectId
     */
    private String objectId;

    /**
     * 流程状态
     * (0未开始、1进行中、2已结束、3已拒绝、4已重置)
     */
    private Integer processInstanceStatus;

    /**
     * 节点编码
     */
    private String nodeCode;

    /**
     * 表单数据
     */
    private Map<String, Object> formData;
}
