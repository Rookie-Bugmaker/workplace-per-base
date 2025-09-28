package com.crrc.client;

import com.crrc.client.fallback.SysServerClientFallback;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.SysDeptPersonDTO;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.SysUserRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 谭永强
 * @date 2025-09-18
 */
@FeignClient(name = "sys-server", fallback = SysServerClientFallback.class)
public interface SysServerClient {


    /**
     * 根据部门ID分页查询人员列表
     *
     * @param deptId   部门ID
     * @param page     当前页码
     * @param pageSize 查询数据条数
     * @return 结果
     */
    @GetMapping("/dept/person/findByDeptId/{deptId}")
    ResultData<PaginationGrid<SysDeptPersonDTO>> findPageByDeptId(@PathVariable("deptId") Long deptId, @RequestParam Integer page, @RequestParam Integer pageSize);

    /**
     * 根据人员ID查询用户信息(批量)
     *
     * @param personIdList 人员ID列表
     * @return 结果
     */
    @PostMapping("/user/findByRelationIds")
    ResultData<List<SysUserDTO>> findByPersonIds(@RequestBody List<Long> personIdList);

    /**
     * 根据用户部门id和companyId和角色编码信息 查询用户
     * @param sysUserRequestDTO
     * @return
     */
    @PostMapping("/user/findUserByRoleCode")
    ResultData<List<SysUserDTO>> findUserByRoleCode(@RequestBody SysUserRequestDTO sysUserRequestDTO);





}
