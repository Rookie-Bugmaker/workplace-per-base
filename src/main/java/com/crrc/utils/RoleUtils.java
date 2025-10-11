package com.crrc.utils;

import com.crrc.api.WkCoreAPi;
import com.crrc.entity.LoginUser;
import com.crrc.entity.dto.UserWithRoleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component; // 关键注解
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RoleUtils {

    private static WkCoreAPi wkCoreAPi;


    @Autowired
    public void setWkCoreAPi(WkCoreAPi wkCoreAPi) {
        RoleUtils.wkCoreAPi = wkCoreAPi;
    }

    public static boolean check(String roleName) {
        try {
            LoginUser currentUser = SecurityFrameworkUtils.getLoginUser();
            if (currentUser == null) {
                log.warn("获取当前登录用户失败!");
                return false;
            }

            // 权限校验逻辑
            List<UserWithRoleDTO> usersWithRoles = wkCoreAPi.getUsersWithRoles(
                    Collections.singletonList(currentUser.getUser().getId()));

            return usersWithRoles.stream()
                    .findFirst()
                    .map(UserWithRoleDTO::getRoles)
                    .orElse(Collections.emptyList())
                    .stream()
                    .anyMatch(role -> roleName.equals(role.getName())); // 修复角色名引用

        } catch (Exception e) {
            log.error("权限校验异常 | 角色: {} | 错误: {}", roleName, e.getMessage(), e);
            return false;
        }
    }
}