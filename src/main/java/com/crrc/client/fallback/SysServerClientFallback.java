package com.crrc.client.fallback;

import com.crrc.client.SysServerClient;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.entity.dto.CompanyDTO;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.pojo.dto.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author 谭永强
 * @date 2025-09-18
 */
@Component
public class SysServerClientFallback implements SysServerClient {
    @Override
    public ResultData<PaginationGrid<SysDeptPersonDTO>> findPageByDeptId(Long deptId, Integer page, Integer pageSize) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<SysUserDTO>> findByPersonIds(List<Long> personIdList) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<SysUserDTO>> findUserByRoleCode(SysUserRequestDTO sysUserRequestDTO) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<SysUserDTO>> findUserByRoleCode(Long roleId) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<SysUserDTO>> findUserByRoleCodeAndCompanyId(SysUserRequestDTO sysUserRequestDTO) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<SysDeptPersonDTO> findByUserCode(String jobNo) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<SysEmployeeDTO> findDeptListByPersonId(DeptListQueryDTO deptListQueryDTO) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<DeptDTO> findDeptByDeptId(Long id) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<SysDeptPersonDTO> findByJobNo(String jobNo) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<PaginationGrid<CompanyDTO>> pageCompany(Integer pageNo, Integer pageSize) {
        return ResultData.fallback();
    }

    @Override
    public ResultData<List<DeptDTO>> listDeptVO(@RequestBody DeptReqDTO dto) {
        return ResultData.fallback();
    }


}
