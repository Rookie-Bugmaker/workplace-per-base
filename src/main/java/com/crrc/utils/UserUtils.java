package com.crrc.utils;

import com.crrc.entity.dto.UserDTO;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.pojo.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
public class UserUtils {

    /**
     * 适用于将UserDTO转化未审批流需要的SysUserDTO
     * @param userDTO 框架其它查询拿到的userDTO
     * @param roleCode 角色编码
     * @return 审批流需要的SysUserDTO
     */
    public static SysUserDTO convertUserDTO2SysUserDTO(UserDTO userDTO, String roleCode) {
        SysUserDTO sysUserDTO = new SysUserDTO();
        sysUserDTO.setUserId(userDTO.getId());
        sysUserDTO.setUserName(userDTO.getUserName());
        sysUserDTO.setFullName(userDTO.getNickName()); // todo 假设昵称对应姓名
        sysUserDTO.setUserRelationId(userDTO.getId()); // todo 人员ID与用户ID一致
        sysUserDTO.setDeptId(userDTO.getDepts() != null && !userDTO.getDepts().isEmpty() ? userDTO.getDepts().get(0).getId() : null);
        sysUserDTO.setCompanyId(userDTO.getCompanys() != null && !userDTO.getCompanys().isEmpty() ? userDTO.getCompanys().get(0).getId() : null);
        sysUserDTO.setRoleCode(roleCode);
        sysUserDTO.setDeptList(userDTO.getDepts() != null ? userDTO.getDepts().stream()
                .map(dept -> {
                    OrganizationDTO orgDTO = new OrganizationDTO();
                    orgDTO.setId(dept.getId());
                    orgDTO.setOrgCode(dept.getOrgCode());
                    orgDTO.setOrgName(dept.getDepartmentName());
                    return orgDTO;
                }).collect(Collectors.toList()) : null);
        return sysUserDTO;
    }
}
