package com.crrc.pojo.vo.workflow;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 1 * @Author: fucheng
 * 2 * @Description: 任务视图对象
 * 3 * @Date: 2022/12/10
 */
@Data
public class TaskVo {
	/**
	 * 任务id
	 */
	private String id;
	/**
	 * 业务Id
	 */
	private String bizKey;
	/**
	 * 任务名称
	 */
	private String name;
	/**
	 * 属于者
	 */
	private String owner;
	/**
	 * 父任务id
	 */
	private String parentTaskId;

	private Map<String, Object> taskLocalVariables;
	/**
	 * 任务截止时间
	 */
	private Date dueDate;
	/**
	 * 委托人
	 */
	private String assignee;
	/**
	 * 认领时间
	 */
	private Date claimTime;
	private Map<String, Object> caseVariables;

	/**
	 * 业务表id
	 */
	private String bizId;

	/**
	 * 业务表 code
	 */
	private String bizCode;

	/**
	 * 创建时间
	 */
	private Date createTime;
	private String processInstanceId;
	private String processDefinitionId;
	private String taskDefinitionKey;
	private String taskDefinitionId;
	/**
	 * 流程变量
	 */
	private Map<String, Object> processVariables;

	/**
	 * 模板id
	 */
	private Long templateId;

	/**
	 * 模板名称
	 */
	private String templateName;

	/**
	 * 功能id
	 */
	private Long functionId;
	/**
	 * 发起人
	 */
	private  String promoter;
}
