package com.crrc.pojo.vo.workflow;

import lombok.Data;


/**
 * @Author: fucheng
 * @Description: 选择用户
 * @Date: 2022/12/22
 * <p>
 * modified by huangsen 20221226
 */
@Data
public class SelectUserVo {
	/**
	 * 是否可以选择用户
	 */
	private Boolean enable;
	/**
	 * 用户是否必填
	 */
	private Boolean required;
	/**
	 * 如果min==max,限定需要选择几个人
	 * 最少需要选择几个用户，null则不控制（required：true下，min最小为1）
	 */
	private Integer min;
	/**
	 * 最多可以选择几个用户，null则不控制
	 */
	private Integer max;

	/**
	 * 选择范围 1:全部用户，2：指定用户，3：根据列表
	 * @see com.crrc.workflow.enums.ProcessStatus.SelectRange
	 */
	private Integer range;

	/**
	 * 可选范围的keys，range 1的情况下是null,2是根据模板返回指定的用户ids，逗号隔开(例如：1,2,3)，3是返回列表id，后续提
	 */
	private String rangeKeys;
}
