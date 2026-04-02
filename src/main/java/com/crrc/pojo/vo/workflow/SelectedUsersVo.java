package com.crrc.pojo.vo.workflow;

import lombok.Data;


/**
 * @Author: fucheng
 * @Description: 已选中的用户  已经在模板中确定好的用户信息（即不用在实例中用户去选择的）
 * @Date: 2022/12/22
 * <p>
 * modified by huangsen 20221226
 */
@Data
public class SelectedUsersVo {
	/**
	 * 用户id
	 */
	private Long userId;
	/**
	 * 用户名称，后期根据人员设置获取
	 */
	private String userName;
	/**
	 * 用户头像，后期根据人员设置获取
	 */
	private String userAvatar;
}
