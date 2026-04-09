package com.crrc.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.crrc.client.WorkFlowClient;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.ProcessInstanceVo;
import com.crrc.entity.dto.StartProcessInstanceDTO;
import com.crrc.exception.entity.ErrorCode;
import com.crrc.pojo.dto.workflow.ApprovalParamDto;
import com.crrc.pojo.dto.workflow.FormDto;
import com.crrc.pojo.vo.workflow.ProcessRunningInfoVo;
import com.crrc.pojo.vo.workflow.SelectedUsersVo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.crrc.utils.ServiceExceptionUtil.exception;

@Slf4j
@Component
public class FlowableUtils {
    public static final String END_EVENT_NODE_ID = "end_event";
    public static final String START_EVENT_NODE_ID = "start_event";
    public static final String TASK_DATA_KEY = "task_data";
    public static final String NODE_CODE_KEY = "nodeCode";
    @Resource // 实例注入
    private WorkFlowClient workFlowClient;

    private final ObjectMapper objectMapper;

    private static final String PUBLIC_KEY_STR = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJK8wwL06DQUeQZOAHA8sXJzgDQKjllaKNJ760feZjuDJ/eI19oAlpDf4oW6qYhGwhPGHLa4Z70WT+bkGPSTxAzYbMEvGy4M7UE3uN9hKDVaYPf0cCDHUEN/XmzZsTjp78cny3DMQcRdenJKMldNtQvtB4etG2AKB3pKUaW/F4twIDAQAB";

    @Autowired
    FlowableUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 发起流程实例(新)
     * 该接口用于整合[获取节点时序图]+[发起流程实例]，实现一个接口完成流程创建过程
     * 注意：该接只适用于提交时无需选择审批人的情况
     *
     * @param param 请求参数
     * @return ProcessInstanceVo 流程实例视图对象
     */
    public ProcessInstanceVo startProcessInstanceNew(StartProcessInstanceDTO param) {
        long startTime = System.currentTimeMillis();
        ResultData<ProcessInstanceVo> processInstanceVoResultData = workFlowClient.startProcessInstanceNew(param);
        requestCheck(processInstanceVoResultData);
        long endTime = System.currentTimeMillis();
        log.info("发起流程实例 ，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        return processInstanceVoResultData.getResult();
    }

    /**
     * 重置全局变量 更新表单数据 必传processInstanceId和formData
     */
    public void resetVariables(String processInstanceId, Map<String, Object> formData) {
        long startTime = System.currentTimeMillis();
        log.info("重置formData：processInstanceId: {}，formData: {}", processInstanceId, JSON.toJSONString(formData));
        FormDto formDto=new FormDto();
        formDto.setProcessInstanceId(processInstanceId);
        formDto.setFormData(formData);
        ResultData<Boolean> resultData=workFlowClient.resetVariables(formDto);
        if(Objects.isNull(resultData) || Boolean.FALSE.equals(resultData.getSuccess())){
            throw exception(new ErrorCode(400,"工作流表单重置失败！"));
        }
        long endTime = System.currentTimeMillis();
        log.info("更新表单数据 ，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
    }

    /**
     * 运行时流程图
     *
     * @param processInstanceId 流程实例ID
     * @return List<ProcessHisTaskVo> 历史已完成任务列表
     */
    public ProcessRunningInfoVo runningSequenceGraph(String processInstanceId) {
        long startTime = System.currentTimeMillis();
        ResultData<ProcessRunningInfoVo> processRunningInfoVoResultData = workFlowClient.runningSequenceGraph(processInstanceId);
        requestCheck(processRunningInfoVoResultData);
        long endTime = System.currentTimeMillis();
        log.info("获取运行时流程图，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        return processRunningInfoVoResultData.getResult();
    }

    /**
     * 根据流程实例ID获取表单信息(非提交节点才能使用)
     *
     * @param processInstanceId 流程实例ID
     * @return 表达数据
     */
    public  Map<String, Object> getFromDataByProcessInstanceId(String processInstanceId) {
        long startTime = System.currentTimeMillis();
        log.info("获取formData,processInstanceId: {}", processInstanceId);
        ResultData<Map<String, Object>> fromDataByProcessInstanceId = workFlowClient.getFromDataByProcessInstanceId(processInstanceId);
        log.info("获取formData,processInstanceId: {}", JsonUtils.toJsonString(fromDataByProcessInstanceId));
        requestCheck(fromDataByProcessInstanceId);
        long endTime = System.currentTimeMillis();
        log.info("获取表单信息，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        return fromDataByProcessInstanceId.getResult();
    }

    public void refuse(String processInstanceId, String comment) {
        refuse(assembleWorkFlowApproveParams(processInstanceId, comment));
    }

    public void refuse(ApprovalParamDto approvalParamDto) {
        long startTime = System.currentTimeMillis();
        ResultData<Boolean> resultData;
        try {
            resultData = workFlowClient.refuse(approvalParamDto);
        } catch (FeignException e) {
            // 处理Feign客户端异常
            String errorMsg = extractErrorMessage(e.contentUTF8());
            log.error("工作流服务调用异常，函数refuse：{}", errorMsg, e);
            throw exception(new ErrorCode(400, errorMsg));
        }
        log.info("refuse参数{}", resultData.toString());
        if(Boolean.FALSE.equals(resultData.getSuccess())){
            throw exception(new ErrorCode(400,"工作流执行失败！"));
        }
        long endTime = System.currentTimeMillis();
        log.info("审批执行，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
    }

    public void done(String processInstanceId, String comment) {
        done(assembleWorkFlowApproveParams(processInstanceId, comment));
    }

    public void done(ApprovalParamDto approvalParamDto) {
        long startTime = System.currentTimeMillis();
        ResultData<Boolean> resultData;
        try {
            resultData = workFlowClient.done(approvalParamDto);
        } catch (FeignException e) {
            // 处理Feign客户端异常
            String errorMsg = extractErrorMessage(e.contentUTF8());
            log.error("工作流服务调用异常，函数done：{}", errorMsg, e);
            throw exception(new ErrorCode(400, errorMsg));
        }
        log.info("done参数{}", resultData.toString());
        if(Boolean.FALSE.equals(resultData.getSuccess())){
            throw exception(new ErrorCode(400,"工作流执行失败！"));
        }
        long endTime = System.currentTimeMillis();
        log.info("审批执行，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
    }

    public void reject(String processInstanceId, String comment) {
        reject(assembleWorkFlowApproveParams(processInstanceId, comment));
    }

    /**
     * 审批驳回
     *
     * @param approvalParamDto 审批参数
     */
    public void reject(ApprovalParamDto approvalParamDto) {
        long startTime = System.currentTimeMillis();
        ResultData<Boolean> resultData;
        try {
            resultData = workFlowClient.reject(approvalParamDto, "");
        } catch (FeignException e) {
            // 处理Feign客户端异常
            String errorMsg = extractErrorMessage(e.contentUTF8());
            log.error("reject错误：工作流服务调用异常：{}", errorMsg);
            throw exception(new ErrorCode(400, errorMsg));
        }
        log.info("reject参数{}", resultData.toString());
        if(Boolean.FALSE.equals(resultData.getSuccess())){
            throw exception(new ErrorCode(400,"工作流驳回失败！"));
        }
        long endTime = System.currentTimeMillis();
        log.info("审批驳回，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
    }

    private String extractErrorMessage(String jsonResponse) {
        try {
            JsonNode node = objectMapper.readTree(jsonResponse);
            return node.path("message").asText("工作流异常");
        } catch (Exception e) {
            return "解析错误信息失败";
        }
    }

    public void shutdown(String processInstanceId) {
        long startTime = System.currentTimeMillis();
        ResultData<Boolean> resultData;
        try {
         resultData = workFlowClient.shutdown(processInstanceId);
        } catch (FeignException e) {
            // 处理Feign客户端异常
            String errorMsg = extractErrorMessage(e.contentUTF8());
            log.error("shutdown错误：工作流服务调用异常：{}", errorMsg);
            throw exception(new ErrorCode(400, errorMsg));
        }
        log.info("shutdown 工作流实例参数{}", processInstanceId);
        if(Objects.isNull(resultData) || Boolean.FALSE.equals(resultData.getSuccess())){
            throw exception(new ErrorCode(400,"工作流终止失败！"));
        }
        long endTime = System.currentTimeMillis();
        log.info("工作流终，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
    }

    /**
     * 不中途设置审批人、节点通过工作流配置执行调用
     */
    public ApprovalParamDto assembleWorkFlowApproveParams(String processInstanceId, String comment) {
        long startTime = System.currentTimeMillis();
        log.info("不中途设置审批人、节点通过工作流配置执行调用processInstanceId: {} 实例组装工作流数据", processInstanceId);
        if (StringUtils.isEmpty(processInstanceId)) {
            throw exception(new ErrorCode(400, "assembleWorkFlowApproveParams异常：工作流流程实例不能为空！"));
        }

        log.info("assembleWorkFlowApproveParams执行：校验通过开始组装工作流数据，processInstanceId: {}", processInstanceId);
        ProcessRunningInfoVo result = runningSequenceGraph(processInstanceId);
        if (CollUtil.isEmpty(result.getSequenceGraph())) {
            throw exception(new ErrorCode(400, "assembleWorkFlowApproveParams异常：工作流时序图为空，请联系管理员！"));

        }
        log.info("done异常处理获取时序图:result {} ", JSON.toJSONString(result));
        Map<String, Object> extra = new HashMap<>();
        List<Map<String, Object>> taskData = new ArrayList<>();

        // 添加开始节点
        taskData.add(createNodeWithNull(START_EVENT_NODE_ID));
        result.getSequenceGraph().forEach(sequenceGraph -> {
            //设置默认节点审批人
            Map<String, Object> nodeWithDefault = createNodeWithDefault(sequenceGraph.getNodeId(), sequenceGraph.getSelectedUsers());
            taskData.add(nodeWithDefault);
            log.info("assembleWorkFlowApproveParams执行：节点：{}，设置原有默认审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(nodeWithDefault.get(sequenceGraph.getNodeId())));
        });
        // 添加结束节点
        taskData.add(createNodeWithNull(END_EVENT_NODE_ID));
        extra.put(TASK_DATA_KEY, taskData);
        ApprovalParamDto approvalParamDto = new ApprovalParamDto();
        approvalParamDto.setProcessInstanceId(processInstanceId);
        approvalParamDto.setComment(comment);
        approvalParamDto.setExtra(extra);
        long endTime = System.currentTimeMillis();
        log.info("assembleWorkFlowApproveParams执行: 组装工作流审批默认参数完成，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        log.info("done异常处理请求参数approvalParamDto: {} ", JSON.toJSONString(approvalParamDto));
        return approvalParamDto;
    }


    /**
     * 此方法必须在工作流中配置编码
     * 中途设置审批人、节点需设置中途的审批人工作流配置执行调用
     */
    public ApprovalParamDto assembleApproveParamsWithResetByNodeCode(String processInstanceId, String comment, List<String> nodeCodes, Map<String, List<String>> userIdMap) {
        long startTime = System.currentTimeMillis();
        //校验工作流和动态设置审批人信息
        log.info("assembleApproveParamsWithResetByNodeCode processInstanceId: {} 实例组装工作流数据", processInstanceId);
        if (StringUtils.isEmpty(processInstanceId)) {
            throw exception(new ErrorCode(400, "assembleApproveParamsWithResetByNodeCode异常：工作流流程实例不能为空！"));
        }
        if (CollUtil.isEmpty(nodeCodes) || CollUtil.isEmpty(userIdMap) || nodeCodes.size() != userIdMap.size()) {
            throw exception(new ErrorCode(400, "assembleApproveParamsWithResetByNodeCode异常：工作流重新指定审批人员有误！"));
        }

        // 组装审批传参数据
        log.info("assembleApproveParamsWithResetByNodeCode校验通过开始组装工作流数据，processInstanceId: {}", processInstanceId);
        ProcessRunningInfoVo result = runningSequenceGraph(processInstanceId);
        if (CollUtil.isEmpty(result.getSequenceGraph())) {
            throw exception(new ErrorCode(400, "assembleApproveParamsWithResetByNodeCode异常：工作流时序图为空，请联系管理员！"));

        }

        Map<String, Object> extra = new HashMap<>();
        List<Map<String, Object>> taskData = new ArrayList<>();

        // 添加开始节点
        taskData.add(createNodeWithNull(START_EVENT_NODE_ID));

        result.getSequenceGraph().forEach(sequenceGraph -> {
            //设置接口返回的原有审批人列表
            if (!nodeCodes.contains(sequenceGraph.getNodeCode())) {
                Map<String, Object> nodeWithDefault = createNodeWithDefault(sequenceGraph.getNodeId(), sequenceGraph.getSelectedUsers());
                taskData.add(nodeWithDefault);
                log.info("assembleApproveParamsWithResetByNodeCode执行: 节点：{}，设置原有默认审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(nodeWithDefault.get(sequenceGraph.getNodeId())));
            } else {
                //动态设置审批人
                taskData.add(createNode(sequenceGraph.getNodeId(), userIdMap.get(sequenceGraph.getNodeCode())));
                log.info("assembleApproveParamsWithResetByNodeCode执行: 节点：{}，设置动态自定义审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(userIdMap.get(sequenceGraph.getNodeCode())));
            }
        });

        // 添加结束节点
        taskData.add(createNodeWithNull(END_EVENT_NODE_ID));
        extra.put(TASK_DATA_KEY, taskData);
        ApprovalParamDto approvalParamDto = new ApprovalParamDto();
        approvalParamDto.setProcessInstanceId(processInstanceId);
        approvalParamDto.setComment(comment);
        approvalParamDto.setExtra(extra);
        long endTime = System.currentTimeMillis();
        log.info("assembleApproveParamsWithResetByNodeCode执行: 通过节点编码组装工作流审批默认参数完成，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        return approvalParamDto;
    }


    /**
     * 此方法必须在工作流中配置标题  且一个工作流中标题唯一,设置多节点审批人
     * 中途设置审批人、节点需设置中途的审批人工作流配置执行调用
     */
    public ApprovalParamDto assembleApproveParamsWithResetByNodeName(String processInstanceId, String comment, List<String> nodeNames, Map<String, List<String>> userIdMap) {
        long startTime = System.currentTimeMillis();
        //校验工作流和动态设置审批人信息
        log.info("assembleApproveParamsWithResetByNodeName执行：processInstanceId: {} 实例组装工作流数据", processInstanceId);
        if (StringUtils.isEmpty(processInstanceId)) {
            throw exception(new ErrorCode(400, "assembleApproveParamsWithResetByNodeName异常：工作流流程实例不能为空！"));
        }
        if (CollUtil.isEmpty(nodeNames) || CollUtil.isEmpty(userIdMap) || nodeNames.size() != userIdMap.size()) {
            throw exception(new ErrorCode(400, "assembleApproveParamsWithResetByNodeName异常：工作流重新指定审批人员有误！"));
        }

        // 组装审批传参数据
        log.info("assembleApproveParamsWithResetByNodeName执行：校验通过开始组装工作流数据，processInstanceId: {}", processInstanceId);
        ProcessRunningInfoVo result = runningSequenceGraph(processInstanceId);
        if (CollUtil.isEmpty(result.getSequenceGraph())) {
            throw exception(new ErrorCode(400, "assembleApproveParamsWithResetByNodeName异常：工作流时序图为空，请联系管理员！"));
        }

        Map<String, Object> extra = new HashMap<>();
        List<Map<String, Object>> taskData = new ArrayList<>();
        //日志打印数据
        List<Map<String, Object>> taskDataLog = new ArrayList<>();
        // 添加开始节点
        taskData.add(createNodeWithNull(START_EVENT_NODE_ID));

        result.getSequenceGraph().forEach(sequenceGraph -> {
            //设置接口返回的原有审批人列表
            if (!nodeNames.contains(sequenceGraph.getNodeName())) {
                Map<String, Object> nodeWithDefault = createNodeWithDefault(sequenceGraph.getNodeId(), sequenceGraph.getSelectedUsers());
                taskData.add(nodeWithDefault);

                //日志打印数据
                Map<String, Object> nodeWithDefaultLog = createNodeWithDefault(sequenceGraph.getNodeId(), sequenceGraph.getSelectedUsers());
                nodeWithDefaultLog.put(NODE_CODE_KEY, sequenceGraph.getNodeCode());
                taskDataLog.add(nodeWithDefaultLog);
                log.info("assembleApproveParamsWithResetByNodeName执行：节点：{}，设置原有默认审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(nodeWithDefault.get(sequenceGraph.getNodeId())));
            } else {
                //动态设置审批人
                taskData.add(createNode(sequenceGraph.getNodeId(), userIdMap.get(sequenceGraph.getNodeName())));

                //日志打印数据
                taskDataLog.add(createNodeWithNodeCode(sequenceGraph.getNodeId(), userIdMap.get(sequenceGraph.getNodeName()), sequenceGraph.getNodeCode()));
                log.info("assembleApproveParamsWithResetByNodeName执行：节点：{}，设置动态自定义审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(userIdMap.get(sequenceGraph.getNodeName())));
            }
        });

        // 添加结束节点
        taskData.add(createNodeWithNull(END_EVENT_NODE_ID));
        extra.put(TASK_DATA_KEY, taskData);

        ApprovalParamDto approvalParamDto = new ApprovalParamDto();
        approvalParamDto.setProcessInstanceId(processInstanceId);
        approvalParamDto.setComment(comment);
        approvalParamDto.setExtra(extra);

        log.info("assembleApproveParamsWithResetByNodeName执行：approvalParamDto: {} 实例组装工作流数据", JSON.toJSONString(approvalParamDto));
        log.info("assembleApproveParamsWithResetByNodeName执行：taskDataLog: {} 实例组装工作流数据", JSON.toJSONString(taskDataLog));
        long endTime = System.currentTimeMillis();
        log.info("assembleApproveParamsWithResetByNodeName执行：通过节点名称组装工作流审批默认参数完成，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        return approvalParamDto;
    }


    /**
     * 此方法必须在工作流中配置标题  且一个工作流中标题唯一,设置单节点
     * 中途设置审批人、节点需设置中途的审批人工作流配置执行调用
     */
    public ApprovalParamDto assembleSingleNodeParamsWithResetByNodeName(String processInstanceId, String comment, String nodeName, List<String> userIds) {
        long startTime = System.currentTimeMillis();
        //校验工作流和动态设置审批人信息
        log.info("assembleSingleNodeParamsWithResetByNodeName执行：processInstanceId: {} 实例组装工作流数据", processInstanceId);
        if (StringUtils.isEmpty(processInstanceId)) {
            throw exception(new ErrorCode(400, "assembleSingleNodeParamsWithResetByNodeName异常：工作流流程实例不能为空！"));
        }
        if (StringUtils.isEmpty(nodeName) || CollUtil.isEmpty(userIds)) {
            throw exception(new ErrorCode(400, "assembleSingleNodeParamsWithResetByNodeName异常：工作流重新指定审批人员有误！"));
        }

        // 组装审批传参数据
        log.info("校验通过开始组装工作流数据，processInstanceId: {}", processInstanceId);
        ProcessRunningInfoVo result = runningSequenceGraph(processInstanceId);
        if (CollUtil.isEmpty(result.getSequenceGraph())) {
            throw exception(new ErrorCode(400, "assembleSingleNodeParamsWithResetByNodeName异常：工作流时序图为空，请联系管理员！"));
        }

        Map<String, Object> extra = new HashMap<>();
        List<Map<String, Object>> taskData = new ArrayList<>();
        //日志打印数据
        List<Map<String, Object>> taskDataLog = new ArrayList<>();
        // 添加开始节点
        taskData.add(createNodeWithNull(START_EVENT_NODE_ID));

        result.getSequenceGraph().forEach(sequenceGraph -> {
            //设置接口返回的原有审批人列表
            if (!nodeName.equals(sequenceGraph.getNodeName())) {
                Map<String, Object> nodeWithDefault = createNodeWithDefault(sequenceGraph.getNodeId(), sequenceGraph.getSelectedUsers());
                taskData.add(nodeWithDefault);

                //日志打印数据
                Map<String, Object> nodeWithDefaultLog = createNodeWithDefault(sequenceGraph.getNodeId(), sequenceGraph.getSelectedUsers());
                nodeWithDefaultLog.put(NODE_CODE_KEY,sequenceGraph.getNodeCode());
                taskDataLog.add(nodeWithDefaultLog);
                log.info("assembleSingleNodeParamsWithResetByNodeName执行：节点：{}，设置原有默认审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(nodeWithDefault.get(sequenceGraph.getNodeId())));
            } else {
                //动态设置审批人
                taskData.add(createNode(sequenceGraph.getNodeId(), userIds));

                //日志打印数据
                taskDataLog.add(createNodeWithNodeCode(sequenceGraph.getNodeId(), userIds, sequenceGraph.getNodeCode()));
                log.info("assembleSingleNodeParamsWithResetByNodeName执行：节点：{}，设置动态自定义审批人列表：{}", sequenceGraph.getNodeName(), JsonUtils.toJsonString(userIds));
            }
        });

        // 添加结束节点
        taskData.add(createNodeWithNull(END_EVENT_NODE_ID));
        extra.put(TASK_DATA_KEY, taskData);

        ApprovalParamDto approvalParamDto = new ApprovalParamDto();
        approvalParamDto.setProcessInstanceId(processInstanceId);
        approvalParamDto.setComment(comment);
        approvalParamDto.setExtra(extra);

        log.info("assembleSingleNodeParamsWithResetByNodeName执行：approvalParamDto: {} 实例组装工作流数据", JSON.toJSONString(approvalParamDto));
        log.info("assembleSingleNodeParamsWithResetByNodeName执行：taskDataLog: {} 实例组装工作流数据", JSON.toJSONString(taskDataLog));
        long endTime = System.currentTimeMillis();
        log.info("assembleSingleNodeParamsWithResetByNodeName执行：组装工作流审批单节点参数完成，耗时: {}秒", String.format("%.2f", (endTime - startTime) / 1000.0));
        return approvalParamDto;
    }

    /**
     *设置默认节点审批人
     */
    private Map<String, Object> createNodeWithDefault(String nodeId, List<SelectedUsersVo> selectedUsers) {
        Map<String, Object> node = new HashMap<>();
        if (CollUtil.isEmpty(selectedUsers)) {
            node.put(nodeId, new ArrayList<>());
        } else {
            node.put(nodeId, selectedUsers.stream().map(user -> Long.toString(user.getUserId())).collect(Collectors.toList()));
        }
        return node;
    }


    /**
     * 设置nodeId对应空的userId
     */
    public Map<String, Object> createNodeWithNull(String nodeId) {
        Map<String, Object> node = new HashMap<>();
        node.put(nodeId, new ArrayList<>());
        return node;
    }


    /**
     * 设置nodeId对应自定义userId 动态自定义！
     */
    public Map<String, Object> createNode(String nodeId, List<String> userIds) {
        Map<String, Object> node = new HashMap<>();
        node.put(nodeId, userIds);
        return node;
    }

    /**
     * 校验工作流返回的请求
     */
    public void requestCheck(ResultData<?> resultData) {
        if (Objects.isNull(resultData) || !resultData.getSuccess() || resultData.getCode() != 200 || resultData.getResult() == null) {
            throw exception(new ErrorCode(400, "工作流实例查询失败，请联系管理员！"));
        }
    }

    /**
     * 用于日志打印使用！！！！！！
     */
    public Map<String, Object> createNodeWithNodeCode(String nodeId, List<String> userIds, String nodeCode) {
        Map<String, Object> node = new HashMap<>();
        node.put(nodeId, userIds);
        node.put(NODE_CODE_KEY, nodeCode);
        return node;
    }
}
