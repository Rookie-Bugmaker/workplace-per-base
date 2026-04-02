package com.crrc.client.fallback;

import com.crrc.client.WorkFlowClient;
import com.crrc.common.ResultData;
import com.crrc.common.ReturnCode;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.pojo.dto.ProcessStatusDTO;
import com.crrc.pojo.dto.workflow.ApprovalParamDto;
import com.crrc.pojo.dto.workflow.FormDto;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;
import com.crrc.pojo.vo.workflow.TaskVo;

import java.util.List;
import java.util.Map;


public class WorkFlowClientFallback implements WorkFlowClient {

    @Override
    public ResultData<ProcessInstanceVo> startProcessInstanceNew(StartProcessInstanceDTO param) {
        return null;
    }

    @Override
    public ResultData<Boolean> resetVariables(FormDto formData) {
        return null;
    }

    @Override
    public ResultData<List<TaskVo>> currentUserTask() {
        return ResultData.fallback();
    }

    @Override
    public ResultData<ProcessRunningInfoVo> getProcessSequenceGraph(FormDto formData) {
        return null;
    }

    @Override
    public ResultData<ProcessRunningInfoVo> runningSequenceGraph(String processInstanceId) {
        return null;
    }

    @Override
    public ResultData<Boolean> done(ApprovalParamDto approvalParamDto) {
        return null;
    }

    @Override
    public ResultData<Boolean> reject(ApprovalParamDto approvalParamDto, String token) {
        return null;
    }

    @Override
    public ResultData<Boolean> shutdown(String processInstanceId) {
        return null;
    }

    @Override
    public void deliver(ApprovalParamDto approvalParamDto) {
        // 暂时无实现
    }

    @Override
    public ResultData<Map<String, Object>> getFromDataByProcessInstanceId(String processInstanceId) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<ProcessStatusDTO>> getProcessStatusByProcessInstanceId(String processInstanceId) {
        return ResultData.fallback();
    }

}
