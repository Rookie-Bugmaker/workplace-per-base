package com.crrc.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * @author yukun
 */
@Data
public class ProcessStatusDTO {
    String nodeName;
    String processInstanceId;
    String processStatus;
    String nodeAssigneeType;
    List<SysUserDTO> userList;
}
