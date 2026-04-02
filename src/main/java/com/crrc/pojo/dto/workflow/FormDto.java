package com.crrc.pojo.dto.workflow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Map;

/**
 * 表单数据传输对象
 * @author huangsen
 * @since 2022/12/26 14:35
 */
@Data
public class FormDto {

	/**
	 * 业务ID(必填)
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private String bizId;
	/**
	 * 业务编码(必填)
	 */
	private String bizCode;

	/**
	 * 模版ID(可选，与模板编码二选一即可)
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long templateId;

	/**
	 * 模板编码(可选，与模板ID二选一即可)
	 */
	private String  templateAlias;

	/**
	 * 表单数据(必填)
	 */
	private Map<String, Object> formData;
	/**
	 * 流程实例(可选)
	 */
	private String processInstanceId;
}
