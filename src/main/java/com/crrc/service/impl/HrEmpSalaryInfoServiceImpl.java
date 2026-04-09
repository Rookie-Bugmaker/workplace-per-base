package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.*;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.mapper.HrEmpBasicMapper;
import com.crrc.mapper.HrEmpSalaryInfoMapper;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.vo.CostCenterListVO;
import com.crrc.service.HrEmpSalaryInfoService;
import com.crrc.utils.*;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 员工薪资信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-08-26 15:23:52
 */
@Service
public class HrEmpSalaryInfoServiceImpl extends WorkServiceImpl<HrEmpSalaryInfoMapper, HrEmpSalaryInfo> implements HrEmpSalaryInfoService {

    private static final String TABLE_NAME = "hr_emp_basic";

    private static final String INFO_MANAGER_TYPE_NAME = "info_manager";

    @Autowired
    private HrEmpSalaryInfoMapper hrEmpSalaryInfoMapper;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;
    @Autowired
    private WkDataAPi wkDataAPi;
    @Autowired
    private CostCenterServiceImpl costCenterService;
    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicServiceImpl;
    @Autowired
    private HrEmpBasicMapper hrEmpBasicMapper;

    public List<HrEmpSalaryInfo> selectMsg(String userCode){
        MPJLambdaWrapper<HrEmpSalaryInfo> wrapper = new MPJLambdaWrapper<HrEmpSalaryInfo>()
                .selectAll(HrEmpSalaryInfo.class)
                .eq(HrEmpSalaryInfo::getUserCode, userCode);
        return hrEmpSalaryInfoMapper.selectList(wrapper);
    }



    /**
     * 条件查询
     * 无参数返回全部数据
     * @param page 空白page，在controller层设置好size和pageNo
     * @param dto 查询实体
     * @return 分页list
     */
    public IPage<CostCenterListVO> listData(IPage<CostCenterListVO> page, CostCenterListVO dto) {

        // ========= 1. 先处理权限，保持跟原来逻辑完全一致 =========
        // 确保只要HrEmpBasic表有数据就能显示
        // 注意：这个条件要在基础表分页里保留
        // 下面的权限逻辑只是改了“使用谁的 wrapper”，业务逻辑不变
        List<OrganizationTreeDTO> childrenOrgList = new ArrayList<>();

        if (
                // 校验是否有	部门人员信息管理  或 	子公司人员信息管理 的角色，有的话进行数据范围校验
                RoleUtils.checkAnyMatch("hrsc_sub_emp_info_mgr", "hrsc_dept_emp_info_mgr")
        ) {

            String orgCode = getCurrentUserOrgCode();

            // 查找字典数据,延用人事员
            // 这里code是deptCode，codeValue是userCode，orgCode和deptCode完全一致，都指部门编码，如流程与数字化部 01320103
            List<CodeInfo> items = codeInfoService.getCodeInfoList(TABLE_NAME, INFO_MANAGER_TYPE_NAME);

            // 查找当前人员是否是特殊配置的信息管理人
            String currentUserCode = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
            Set<String> managerOrgCodes = items.stream().filter(e -> e.getCodeValue().equals(currentUserCode)).map(CodeInfo::getCode).collect(Collectors.toSet());

            ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataAPi.treeOrganization();
            List<OrganizationTreeDTO> data = listResponseResultDTO.getData();

            // 如果是字典中特殊配置的信息管理人，说明该人员管理的非本部门信息，同时，有可能管理多个部门的信息，那么后面就不需要判断了
            if(!managerOrgCodes.isEmpty()) {
                for (String manageOrgCode : managerOrgCodes) {
                    // 后面只用到了childrenOrgList中每一项的orgCode字段
                    for (OrganizationTreeDTO root : data) {
                        Optional<OrganizationTreeDTO> nodeBfs = TreeUtils.findNodeBfs(root, n -> manageOrgCode.equals(n.getOrgCode()), OrganizationTreeDTO::getChildren);
                        // 根据orgCode匹配其下级部门，并获得其展平后的List（包含所有部门Code）
                        nodeBfs.ifPresent(organizationTreeDTO -> childrenOrgList.addAll(TreeUtils.flattenDescendants(organizationTreeDTO, OrganizationTreeDTO::getChildren, true)));
                    }
                }
            } else {
                // 如果字典里没有配置，说明是正常情况，该人员管理自己部门的信息
                // 获得当前人员的orgCode，并获得其可查询的Tree结构
                // 获取所有的组织数据
                if (ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
                    throw new IllegalArgumentException("外部接口错误，获取树形组织信息失败");
                }

                // 如果是子公司级别管理员
                if(RoleUtils.checkAnyMatch("hrsc_sub_emp_info_mgr")) {
                    // 根据orgCode匹配公司及其下级部门能否有匹配项，如果包含，则获得其展平后的List（包含所有部门Code）
                    childrenOrgList.addAll(findParentAndFlattenList(data, orgCode));
                    if (childrenOrgList.isEmpty()) {
                        throw new IllegalArgumentException("获取公司失败，公司匹配错误，orgCode:" + orgCode);
                    }
                }

                // 如果是部门级别管理员
                if(RoleUtils.checkAnyMatch("hrsc_dept_emp_info_mgr")) {
                    Optional<OrganizationTreeDTO> nodeBfs = Optional.empty();
                    for (OrganizationTreeDTO root : data) {
                        nodeBfs = TreeUtils.findNodeBfs(root, n -> orgCode.equals(n.getOrgCode()), OrganizationTreeDTO::getChildren);
                        if (nodeBfs.isPresent()) {
                            break;
                        }
                    }

                    // 根据orgCode匹配其下级部门，并获得其展平后的List（包含所有部门Code）
                    childrenOrgList.addAll(TreeUtils.flattenDescendants(nodeBfs.get(), OrganizationTreeDTO::getChildren, true));
                }

                if (childrenOrgList.isEmpty()) {
                    throw new IllegalArgumentException("获取公司失败，公司匹配错误，orgCode:" + orgCode);
                }
            }
        }

        // 至此，拿到可查看部门列表
        Set<String> deptSet = childrenOrgList.stream()
                .map(OrganizationTreeDTO::getOrgCode)
                .collect(Collectors.toSet());

        // 构建联表查询
        LambdaQueryWrapper<HrEmpBasic> basicWrapper = new LambdaQueryWrapper<>();
        basicWrapper
                // 档案号模糊查询
                .like(StringUtils.isNotBlank(dto.getUserCode()), HrEmpBasic::getUserCode, dto.getUserCode())
                // 姓名模糊查询
                .like(StringUtils.isNotBlank(dto.getUserName()), HrEmpBasic::getName, dto.getUserName());

        // 部门条件单独判断，仅在 deptSet 不为空时拼接
        if (!deptSet.isEmpty()) {
            basicWrapper.and(wrapper -> wrapper.in(HrEmpBasic::getDept, deptSet));
        }

        IPage<HrEmpBasic> basicPage = new Page<>(page.getCurrent(), page.getSize());
        IPage<HrEmpBasic> basicResult = hrEmpBasicMapper.selectPage(basicPage, basicWrapper);

        // 根据 basicResult 中的 userCode 查询 hrEmpSalaryInfo 表
        Set<String> userCodeSet = basicResult.getRecords().stream()
                .map(HrEmpBasic::getUserCode)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<HrEmpSalaryInfo> salaryWrapper = new LambdaQueryWrapper<>();
        salaryWrapper.in(HrEmpSalaryInfo::getUserCode, userCodeSet);

        List<HrEmpSalaryInfo> salaryList = hrEmpSalaryInfoMapper.selectList(salaryWrapper);

        // 构建映射关系
        Map<String, HrEmpSalaryInfo> salaryMap = salaryList.stream()
                .collect(Collectors.toMap(HrEmpSalaryInfo::getUserCode, Function.identity(), (existing, replacement) -> existing));

        // 组装 VO
        List<CostCenterListVO> voList = basicResult.getRecords().stream()
                .map(basic -> {
                    CostCenterListVO vo = new CostCenterListVO();
                    vo.setUserCode(basic.getUserCode());
                    vo.setUserName(basic.getName());
                    vo.setDept(basic.getDept());
                    HrEmpSalaryInfo salaryInfo = salaryMap.get(basic.getUserCode());
                    if (salaryInfo != null) {
                        vo.setCostcenterCode(salaryInfo.getDepartmentCode());
                    }
                    return vo;
                })
                .collect(Collectors.toList());


        // 翻译部门和成本中心
        for(CostCenterListVO entity : voList) {
            String deptName = DeptUtils.getDeptNameByCode(entity.getDept());
            entity.setDept(deptName);

            CostCenter costCenter = costCenterService.translateByCode(entity.getCostcenterCode());
            if(costCenter != null && StringUtils.isNotBlank(costCenter.getName())) {
                entity.setCostcenterName(costCenter.getName());
            } else {
                entity.setCostcenterName("");
            }
        }

        // 设置分页信息
        Page<CostCenterListVO> resultPage = new Page<>(basicResult.getCurrent(), basicResult.getSize(), basicResult.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    private String getCurrentUserOrgCode() {
        // 根据员工获取部门编码
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            throw new IllegalArgumentException("获取员工登录信息失败");
        }
        if (loginUser.getUser() == null || StringUtils.isBlank(loginUser.getUser().getJobNo())) {
            throw new IllegalArgumentException("获取员工工号失败");
        }
        String jobNo = loginUser.getUser().getJobNo();
        // 如果没有配置，则从basic里查询出员工的部门
        List<HrEmpBasicDTO> dtoList = hrEmpBasicServiceImpl.selectMsg(jobNo);
        if (ObjectUtils.isEmpty(dtoList)) {
            throw new IllegalArgumentException("获取员工信息失败，员工不存在，userCode:" + jobNo);
        }
        return dtoList.get(0).getDept();
    }

    /**
     * 根据树型组织数据列表（公司列表），判断公司是否包含orgCode，并返回该公司展平后的列表
     * @param data 树型组织数据列表
     * @param orgCode 需要判断的orgCode
     * @return 包含orgCode的公司展平后的列表
     */
    private List<OrganizationTreeDTO> findParentAndFlattenList(List<OrganizationTreeDTO> data, String orgCode) {
        for(OrganizationTreeDTO root : data) {
            if (TreeUtils.containsBfs(root, n -> StringUtils.equals(orgCode, n.getOrgCode()), OrganizationTreeDTO::getChildren)) {
                return TreeUtils.flattenDescendants(root, OrganizationTreeDTO::getChildren, true);
            }
        }
        return Collections.emptyList();
    }

}