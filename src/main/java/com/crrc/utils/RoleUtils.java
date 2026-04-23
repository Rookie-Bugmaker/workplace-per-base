package com.crrc.utils;

import com.crrc.api.WkCoreAPi;
import com.crrc.entity.LoginUser;
import com.crrc.entity.dto.UserWithRoleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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

    /**
     * 判断当前登录用户是否具有指定名称的角色编码列表中任意一个
     * @param roleCodeSet 角色编码Set
     * @return 是否具有
     */
    public static boolean checkAnyMatch(Collection<String> roleCodeSet) {
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
                    .anyMatch(role -> roleCodeSet.contains(role.getRoleKey()));

        } catch (Exception e) {
            log.error("权限校验异常 | 角色: {} | 错误: {}", roleCodeSet, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 判断当前登录用户是否全都不具有指定名称的角色编码列表中所有角色
     * @param roleCodeSet 角色编码Set
     * @return 是否不具有
     */
    public static boolean checkNoneMatch(Collection<String> roleCodeSet) {
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
                    .noneMatch(role -> roleCodeSet.contains(role.getRoleKey()));

        } catch (Exception e) {
            log.error("权限校验异常 | 角色: {} | 错误: {}", roleCodeSet, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 判断当前登录用户是否不具有指定名称的角色编码
     * @param roleCode 角色编码
     * @return 是否不具有
     */
    public static boolean checkNoneMatch(String roleCode) {
        return checkNoneMatch(Collections.singletonList(roleCode));
    }

    /**
     * 判断当前登录用户是否不具有指定名称的角色编码
     * @param roleCodes 角色编码
     * @return 是否不具有
     */
    public static boolean checkNoneMatch(String... roleCodes) {
        return checkNoneMatch(new HashSet<>(Arrays.asList(roleCodes)));
    }

    /**
     * 判断当前登录用户是否具有指定名称的角色编码
     * @param roleCode 角色编码
     * @return 是否具有
     */
    public static boolean checkAnyMatch(String roleCode) {
        return checkAnyMatch(Collections.singletonList(roleCode));
    }

    /**
     * 判断当前登录用户是否具有指定名称的角色编码
     * @param roleCodes 角色编码
     * @return 是否具有
     */
    public static boolean checkAnyMatch(String... roleCodes) {
        return checkAnyMatch(new HashSet<>(Arrays.asList(roleCodes)));
    }
}