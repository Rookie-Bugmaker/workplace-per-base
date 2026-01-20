package com.crrc.client.fallback;

import com.crrc.client.WorkFlowClient;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.ProcessStatusDTO;

import java.util.List;
import java.util.Map;


public class WorkFlowClientFallback implements WorkFlowClient {


    @Override
    public ResultData<Map<String, Object>> getFromDataByProcessInstanceId(String processInstanceId) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<ProcessStatusDTO>> getProcessStatusByProcessInstanceId(String processInstanceId) {
        return ResultData.fallback();
    }

}
