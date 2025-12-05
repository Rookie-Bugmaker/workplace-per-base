package com.crrc.utils;

import com.crrc.api.WkCoreAPi;
import com.crrc.entity.LoginUser;
import com.crrc.entity.dto.UserWithRoleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 角色相关工具类
 * @author mayiyao
 */
@Slf4j
@Component
public class RoleUtils {

    private static WkCoreAPi wkCoreApi;

    @Autowired
    public void setWkCoreApi(WkCoreAPi wkCoreApi) {
        RoleUtils.wkCoreApi = wkCoreApi;
    }

    /**
     * 判断当前登录用户是否具有指定名称的角色名
     * @param roleName 中文角色名称
     * @return 是否具有
     */
    public static boolean check(String roleName) {
        try {
            LoginUser currentUser = SecurityFrameworkUtils.getLoginUser();
            if (currentUser == null) {
                log.warn("获取当前登录用户失败!");
                return false;
            }

            // 权限校验逻辑
            List<UserWithRoleDTO> usersWithRoles = wkCoreApi.getUsersWithRoles(
                    Collections.singletonList(currentUser.getUser().getId()));

            return usersWithRoles.stream()
                    .findFirst()
                    .map(UserWithRoleDTO::getRoles)
                    .orElse(Collections.emptyList())
                    .stream()
                    // 修复角色名引用
                    .anyMatch(role -> roleName.equals(role.getName()));

        } catch (Exception e) {
            log.error("权限校验异常 | 角色: {} | 错误: {}", roleName, e.getMessage(), e);
            return false;
        }
    }
}