package com.crrc.client;


import com.crrc.common.ResultData;
import com.crrc.pojo.dto.ProcessStatusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "workflow-server"
)
public interface WorkFlowClient {


    @GetMapping("/processCtl/getFromDataByProcessInstanceId")
    ResultData<Map<String, Object>> getFromDataByProcessInstanceId(@RequestParam String processInstanceId);

    @PostMapping(value = "/processCtl/processStatus", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResultData<List<ProcessStatusDTO>> getProcessStatusByProcessInstanceId(@RequestParam("processInstanceIds") String processInstanceIds);

}
