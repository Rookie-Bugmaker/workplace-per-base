package com.crrc.client.fallback;

import com.crrc.client.SysServerClient;
import com.crrc.common.PaginationGrid;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.SysDeptPersonDTO;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.SysUserRequestDTO;
import org.springframework.stereotype.Component;

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
    public ResultData<List<SysUserDTO>> findUserByRoleCodeAndCompanyId(SysUserRequestDTO sysUserRequestDTO) {
        return ResultData.fallback();
    }


}
