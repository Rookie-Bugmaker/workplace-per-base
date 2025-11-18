package com.crrc.client;

import com.crrc.client.fallback.SysServerClientFallback;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.CompanyDTO;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.pojo.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
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

    /**
     * 根据租户id和角色编码获取用户信息
     * @param sysUserRequestDTO
     * @return
     */
    @PostMapping("/user/findUserByRoleCodeAndCompanyId")
    ResultData<List<SysUserDTO>> findUserByRoleCodeAndCompanyId(@RequestBody SysUserRequestDTO sysUserRequestDTO);

    /**
     * 根据人员工号查询人员基本信息
     * @param jobNo
     * @return
     */
    @PostMapping("/org/person/findByJobNo")
    ResultData<SysDeptPersonDTO> findByUserCode(@RequestParam("jobNo") String jobNo);

    /**
     * 根据personId获取人员部门列表
     * @param deptListQueryDTO 员工id
     * @return List<OrganizationDTO>包裹在SysUserDTO中
     */
    @PostMapping(value = "/org/person/findById?language=zh-CN", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResultData<SysEmployeeDTO> findDeptListByPersonId(@RequestBody DeptListQueryDTO deptListQueryDTO);

    /**
     * 根据部门id获取部门信息
     * @param id 部门id
     * @return 信息
     */
    @GetMapping("/dept/get")
    ResultData<DeptDTO> findDeptByDeptId(@RequestParam("id") Long id);

    /**
     * 根据工号查询详细人员信息
     * @param jobNo 工号，即userCode
     * @return 详细人员信息
     */
    @PostMapping("/org/person/findByJobNo")
    ResultData<SysDeptPersonDTO> findByJobNo(@RequestParam("jobNo") String jobNo);

    @GetMapping("/company/page")
    ResultData<PaginationGrid<CompanyDTO>> pageCompany(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize);
}
