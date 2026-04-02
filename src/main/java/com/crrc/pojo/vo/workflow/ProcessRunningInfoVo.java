package com.crrc.pojo.vo.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: fucheng
 * @Description: 运行时流程图视图对象 流程数据
 * @Date: 2022/12/22
 * <p>
 * modified by huangsen 20221226
 */
@Data
public class ProcessRunningInfoVo {

	private static final long serialVersionUID = -2436283582896753899L;
	/**
	 * 业务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private String bizId;

	/**
	 * 业务编码（业务表里面的单据号字段值，提交时会传参给后端）
	 */
	private String bizCode;

	/**
	 * 模板id
	 */
	private Long templateId;
	/**
	 * 版本
	 */
	private Integer templateVersion;

	/**
	 * 流程名称（模板名：templateName）
	 */
	private String processName;

	/**
	 * 整个流程的状态 0：未开始（待提交），1：进行中，2：已结束，3：已终止（中途拒绝,状态是终止）
	 */
	private Integer processStatus;

	/**
	 * 流程开始时间(年月日时分秒)
	 */
	private Date processStartTime;

	/**
	 * 流程结束时间(年月日时分秒)（终止也算结束）
	 */
	private Date processEndTime;

	/**
	 * 当前进行的流程是否有操作权限
	 */
	private Boolean isCurrentUser;

	/**
	 * 表单处理权限
	 */
	private EditPermissions editPermissions;

	/**
	 * 流程节点数据
	 */
	private List<ProcessNodeVo> sequenceGraph;

	/**
	 * 历史实例数据
	 */
	private List<ProcessRunningInfoVo> hisRunningInfoList;
}
