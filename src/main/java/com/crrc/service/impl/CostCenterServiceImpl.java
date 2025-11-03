package com.crrc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.CostCenter;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.mapper.CostCenterMapper;
import com.crrc.pojo.dto.BundleEmpAndCostCenterVO;
import com.crrc.pojo.vo.HrBasicCostCenterDetailVO;
import com.crrc.service.CostCenterService;
import com.crrc.utils.DeptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CostCenterServiceImpl extends WorkServiceImpl<CostCenterMapper, CostCenter> implements CostCenterService {
    @Autowired
    WkCoreAPi wkCoreAPi;
    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;

    public void queryVO(Page<CostCenter> page, CostCenter pageReqVO) {
        Map<String, String> orgCode2Name = wkCoreAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));

        this.query(page, pageReqVO);

        page.getRecords().forEach(org -> {
            org.setOrgName(orgCode2Name.getOrDefault(org.getCode(), "未知公司"));
        });
    }

    public List<CostCenter> queryVO(CostCenter queryReqVO) {
        Map<String, String> orgCode2Name = wkCoreAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));

        List<CostCenter> query = this.query(queryReqVO);

        query.forEach(org -> {
            org.setOrgName(orgCode2Name.getOrDefault(org.getCode(), "未知公司"));
        });

        return query;
    }

    public CostCenter getVO(Long id) {
        Map<String, String> orgCode2Name = wkCoreAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));

        CostCenter costCenter = this.getById(id);
        costCenter.setOrgName(orgCode2Name.getOrDefault(costCenter.getCode(), "未知公司"));

        return costCenter;
    }

    public Boolean bundleEmpAndCostCenter(BundleEmpAndCostCenterVO vo) {
        HrEmpBasic hrEmpBasic = new HrEmpBasic();
        hrEmpBasic.setId(vo.getEmpBasicId());
        hrEmpBasic.setCostCenter(vo.getCostCenterId());
        hrEmpBasicService.updateById(hrEmpBasic);
        return true;
    }

    public List<HrBasicCostCenterDetailVO> queryDetails(Long id) {
        CostCenter costCenter = this.getById(id);
        HrEmpBasic hrEmpBasic = new HrEmpBasic();
        hrEmpBasic.setCostCenter(id.toString());
        List<HrEmpBasic> hrEmpBasicList = hrEmpBasicService.query(hrEmpBasic);
        List<HrEmpBasic> query = hrEmpBasicService.getListByUserCode(hrEmpBasicList);
        List<HrBasicCostCenterDetailVO> result = query.stream().map(basic -> {
            HrBasicCostCenterDetailVO hrBasicCostCenterDetailVO = new HrBasicCostCenterDetailVO();
            hrBasicCostCenterDetailVO.setCostCenterCode(costCenter.getCode());
            hrBasicCostCenterDetailVO.setCostCenterName(costCenter.getName());
            return hrBasicCostCenterDetailVO;
        }).collect(Collectors.toList());
        return result;
    }
}
