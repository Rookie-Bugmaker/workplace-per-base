package com.crrc.pojo.vo.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Author: fucheng
 * @Description: 审批记录
 * @Date: 2022/12/22
 * <p>
 * modified by huangsen  20221226
 */
@Data
public class ApprovalRecordVo {
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 用户名称，后期根据人员设置获取
	 */
	private String userName;
	/**
	 * 用户头像，后期根据人员设置获取
	 */
	private String userAvatar;
	/**
	 * 是否加签用户（标识不是原节点处理人）????
	 */
	private Boolean isPlusSignUser;
	/**
	 * 是否被转交用户（标识不是原节点处理人）????
	 */
	private Boolean isDeliveredUser;
	/**
	 * 审批状态
	 * 0:待审批、1:已通过、2:已拒绝、3:已驳回、4:已转交、5:未处理、6:已加签、7:已抄送、8:已终止、9:已退回
	 */
	private Integer approvalStatus;
	/**
	 * 审批意见
	 */
	private String comment;

	/**
	 * 动态表单参数
	 */
	private String dynamicApprovalFormValues;

	/**
	 * 附件id
	 */
	private Long attachmentId;
	/**
	 * 活动创建时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS",timezone="GMT+8")
	private Date createTime;
	/**
	 * 处理时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS",timezone="GMT+8")
	private Date handleTime;
}
