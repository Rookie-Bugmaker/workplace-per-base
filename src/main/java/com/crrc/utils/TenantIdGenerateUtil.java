package com.crrc.utils;

import com.crrc.api.WkAuthAPi;
import com.crrc.api.WkCoreAPi;
import com.crrc.entity.dto.DeptInfoDTO;
import com.crrc.entity.dto.SysOrgPersonDTO;
import com.crrc.entity.dto.SysUserDTO;
import com.crrc.entity.dto.response.ResultData;
import com.crrc.handler.MultiTenantHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TenantIdGenerateUtil {

    @Autowired
    private WkAuthAPi wkAuthAPi;
    @Autowired
    private WkCoreAPi wkCoreAPi;

    /**
     * 框架要求传参
     */
    private final String LANGUAGE = "zh-CN";

    public String generateTenantId() {
        try {
            // 查询当前登录人的所属部门
            SysUserDTO currentUser =  wkAuthAPi.currentUser().getResult();
            ResultData<SysOrgPersonDTO> sysOrgPersonDTOResultData = wkCoreAPi.findPersonByUserId(currentUser.getUserId(), LANGUAGE);
            if(HttpStatus.OK.value() == sysOrgPersonDTOResultData.getCode()){
                List<DeptInfoDTO> deptInfoDTOS = sysOrgPersonDTOResultData.getResult().getDeptList();
                if(!deptInfoDTOS.isEmpty()) {
                    return deptInfoDTOS.get(0).getOrgCode();
                }
            }
        } catch (Exception e) {
            log.error("tenant_id查询错误", e);
        }

        return "438";
    }
}
