package com.crrc.client;


import com.crrc.common.ResultData;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.pojo.dto.ProcessStatusDTO;
import com.crrc.pojo.dto.workflow.ApprovalParamDto;
import com.crrc.pojo.dto.workflow.FormDto;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;
import com.crrc.pojo.vo.workflow.TaskVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "workflow-server"
)
public interface WorkFlowClient {

    @PostMapping({"/processCtl/startProcessInstanceNew"})
    ResultData<ProcessInstanceVo> startProcessInstanceNew(@RequestBody StartProcessInstanceDTO param);


    /**
     *重置全局变量 更新表单数据 必传processInstanceId和formData
     * @param formData
     */
    @PostMapping("/processCtl/updateVariables")
    ResultData<Boolean> resetVariables(@RequestBody FormDto formData);

    /**
     * 获取当前用户的待办任务列表
     *
     * @return 当前用户待办任务列表
     */
    @GetMapping("/processCtl/getCurrentUserToDos")
    ResultData<List<TaskVo>> currentUserTask();

    /**
     * 获取节点时序图(预处理)
     *
     * @param formData 表单数据
     * @return List<ProcessSequenceDiagramVo> 节点时序图对象集合
     */
    @PostMapping("/processCtl/sequenceGraph")
    ResultData<ProcessRunningInfoVo> getProcessSequenceGraph(@RequestBody FormDto formData);


    /**
     * 运行时流程图
     *
     * @param processInstanceId 流程实例ID
     * @return List<ProcessHisTaskVo> 历史已完成任务列表
     */
    @GetMapping("/processCtl/runningSequenceGraph/{processInstanceId}")
    ResultData<ProcessRunningInfoVo> runningSequenceGraph(@PathVariable String processInstanceId);


    /**
     * 审批同意
     *
     * @param approvalParamDto 审批参数
     */
    @PostMapping("/processCtl/done")
    ResultData<Boolean> done(@RequestBody @Validated ApprovalParamDto approvalParamDto);

    /**
     * 审批拒绝
     *
     * @param approvalParamDto 审批参数
     */
    @PostMapping("/processCtl/refuse")
    ResultData<Boolean> refuse(@RequestBody @Validated ApprovalParamDto approvalParamDto);


    @PostMapping("/processCtl/reject")
    ResultData<Boolean> reject(@RequestBody @Validated ApprovalParamDto approvalParamDto, @RequestHeader("Authorization") String token);

    /**
     * 终止流程
     */
    @PostMapping("/processCtl/shutdown")
    ResultData<Boolean> shutdown(@RequestParam String processInstanceId);

    /**
     * 审批转发
     *
     * @param approvalParamDto 审批参数
     */
    @PostMapping("/processCtl/deliver")
    void deliver(@RequestBody @Validated ApprovalParamDto approvalParamDto);

    @GetMapping("/processCtl/getFromDataByProcessInstanceId")
    ResultData<Map<String, Object>> getFromDataByProcessInstanceId(@RequestParam String processInstanceId);

    @PostMapping(value = "/processCtl/processStatus", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResultData<List<ProcessStatusDTO>> getProcessStatusByProcessInstanceId(@RequestParam("processInstanceIds") String processInstanceIds);

}
