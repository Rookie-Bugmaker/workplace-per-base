package com.crrc.pojo.vo.workflow;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: fucheng
 * @Description: 节点数据
 * @Date: 2022/12/22
 * <p>
 * modified by huangsen 20221226
 */
@Data
public class ProcessNodeVo {

	private static final long serialVersionUID = -3374915427027263322L;
	/**
	 * 节点ID，同模板配置
	 */
	private String nodeId;

	/**
	 * 节点类型，同模板配置
	 */
	private Integer nodeType;

	/**
	 * 节点名称，同模板配置
	 */
	private String nodeName;
	/**
	 * 节点编码，同模板配置
	 */
	private String nodeCode;

	/**
	 * 节点描述，同模板配置
	 */
	private String nodeDesc;

	/**
	 * 节点状态 0：未开始，1：进行中，2：已完成，3：已拒绝
	 *
	 * @see com.crrc.workflow.enums.ProcessStatus.NodeStatus
	 */
	private Integer nodeStatus;

	/**
	 * 模板id
	 */
	private Long templateId;

	/**
	 * 是否是当前进行中的节点(流程未开始时，提交人节点isCurrentNode为true结束时，
	 * 结束节点isCurrentNode为true;终止时，终止的节点isCurrentNode为true)
	 */
	private Boolean isCurrentNode;

	/**
	 * 节点开始时间(年月日时分秒)
	 */
	private Date startTime;

	/**
	 * 节点结束时间(年月日时分秒)
	 */
	private Date endTime;

	/**
	 * 处理时长（单位：小时），endTime-startTime
	 */
	private String processingTime;

	/**
	 * 是否超时处理
	 */
	private Boolean overtimeFlag;

	/**
	 * 超时时长（单位：小时）
	 */
	private String overTime;

	/**
	 * 审批类型，1：单人审批，2：多人会签，3：多人或签
	 *
	 * @see com.crrc.workflow.enums.ProcessStatus.ApprovalType
	 */
	private Integer approvalType;

	/**
	 * 审批记录
	 */
	private List<ApprovalRecordVo> approvalRecords;

	/**
	 * 操作权限 1：同意，2：拒绝，3驳回，4：转交，5：加签
	 */
	private List<Integer> operationAuthority;

	/**
	 * 审批意见是否必填（拒绝的话必定是true）
	 */
	private Boolean requiredComment;

	/**
	 * 已经在模板中确定好的用户信息（即不用在实例中用户去选择的）
	 */
	private List<SelectedUsersVo> selectedUsers;

	/**
	 * 选择用户
	 */
	private SelectUserVo selectUser;

	/**
	 * 动态表单配置
	 */
	private String dynamicApprovalForm;
	/**
	 * 允许上传附件
	 */
	private Boolean allowUploadAttachment;

	/**
	 * 要求上传附件
	 */
	private Boolean requireUploadAttachment;
	/**
	 * 上传文件类型限制
	 */
	private String attachmentAccept;
}
