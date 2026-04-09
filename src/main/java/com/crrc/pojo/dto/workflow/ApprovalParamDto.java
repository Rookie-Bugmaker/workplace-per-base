package com.crrc.pojo.dto.workflow;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 审批参数DTO
 * @author huangsen
 * @since 2022/12/16 12:09
 */
@Data
public class ApprovalParamDto {
	/**
	 * 用于转交给谁的用户ID
	 */
	private String userId;

	/**
	 * 用户加签
	 */
	private List<String> userIds;

	/**
	 * 是否前置加签  Y N
	 */
	private String beforeAddSign;

	/**
	 * 流程实例ID
	 */
	@NotBlank(message = "流程实例ID不能为空")
	private String processInstanceId;

	/**
	 * 审批意见
	 */
	private String comment;
	/**
	 * 上传附件id
	 */
	private Long attachmentId;

	/**
	 * 动态表单参数
	 */
	private String dynamicApprovalFormValues;

	/**
	 * 额外信息
	 */
	private Map<String, Object> extra;
}
