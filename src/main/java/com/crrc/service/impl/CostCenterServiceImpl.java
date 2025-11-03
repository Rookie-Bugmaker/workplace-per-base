package com.crrc.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.CostCenter;
import com.crrc.entity.HrEmpBasic;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.mapper.CostCenterMapper;
import com.crrc.pojo.dto.BundleEmpAndCostCenterVO;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.vo.HrBasicCostCenterDetailVO;
import com.crrc.service.CostCenterService;
import com.crrc.utils.DeptUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CostCenterServiceImpl extends WorkServiceImpl<CostCenterMapper, CostCenter> implements CostCenterService {
    @Autowired
    WkDataAPi wkDataAPi;
    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;

    public void queryVO(Page<CostCenter> page, CostCenter pageReqVO) {
        Map<String, String> orgCode2Name = wkDataAPi.treeOrganization().getData().stream().peek(
                item -> item.setChildren(null)
        ).collect(Collectors.toMap(
                OrganizationTreeDTO::getOrgCode,
                OrganizationTreeDTO::getOrgName
        ));

        this.query(page, pageReqVO);

        page.getRecords().forEach(org -> org.setCollectionName(orgCode2Name.getOrDefault(org.getCollection(), "未知公司")));
    }

    public List<CostCenter> queryVO(CostCenter queryReqVO) {
        Map<String, String> orgCode2Name = wkDataAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));

        List<CostCenter> query = this.query(queryReqVO);

        query.forEach(org -> {
            org.setCollectionName(orgCode2Name.getOrDefault(org.getCode(), "未知公司"));
        });

        return query;
    }

    public CostCenter getVO(Long id) {
        Map<String, String> orgCode2Name = wkDataAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));

        CostCenter costCenter = this.getById(id);
        costCenter.setCollectionName(orgCode2Name.getOrDefault(costCenter.getCode(), "未知公司"));

        return costCenter;
    }

    public Boolean bundleEmpAndCostCenter(List<BundleEmpAndCostCenterVO> vo) {
        vo.forEach(item -> {
            HrEmpBasic hrEmpBasic = new HrEmpBasic();
            hrEmpBasic.setId(item.getEmpBasicId());
            hrEmpBasic.setCostCenter(item.getCostCenterId());
            hrEmpBasicService.updateById(hrEmpBasic);
        });

        return true;
    }

    public List<HrBasicCostCenterDetailVO> queryDetails(Long id) {
        CostCenter costCenter = this.getById(id);
        HrEmpBasic hrEmpBasic = new HrEmpBasic();
        hrEmpBasic.setCostCenter(id.toString());
        // 根据cost center查询所有的人员信息
        List<String> userCodeList = hrEmpBasicService.query(hrEmpBasic).stream().map(HrEmpBasic::getUserCode).collect(Collectors.toList());
        // 根据人员信息查询(带翻译)
        List<HrEmpBasicDTO> query = hrEmpBasicService.selectMsgList(userCodeList);
        return query.stream().map(basic -> {
            HrBasicCostCenterDetailVO hrBasicCostCenterDetailVO = new HrBasicCostCenterDetailVO();
            hrBasicCostCenterDetailVO.setCostCenterCode(costCenter.getCode());
            hrBasicCostCenterDetailVO.setCostCenterName(costCenter.getName());
            BeanUtils.copyProperties(basic, hrBasicCostCenterDetailVO);
            return hrBasicCostCenterDetailVO;
        }).collect(Collectors.toList());
    }
}
