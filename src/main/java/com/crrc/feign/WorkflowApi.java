package com.crrc.feign;

import com.crrc.common.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@FeignClient(
    name = "workflow-server"
)
public interface WorkflowApi {
    @GetMapping("/processCtl/getFromDataByProcessInstanceId")
    ResultData<Map<String, Object>> getFormDataByPid(@RequestParam("processInstanceId") String processInstanceId);
}
