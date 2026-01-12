package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.IgnoreTenant;
import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.client.SysServerClient;
import com.crrc.entity.*;
import com.crrc.entity.dto.ResponseResultDTO;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import com.crrc.entity.dto.mainData.OrganizationTreeDTO;
import com.crrc.mapper.*;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.dto.HrEmpBasicSearchDTO;
import com.crrc.service.HrEmpBasicService;
import com.crrc.utils.*;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 员工基本信息表 服务实现类
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Service
@Slf4j
public class HrEmpBasicServiceImpl extends WorkServiceImpl<HrEmpBasicMapper, HrEmpBasic> implements HrEmpBasicService {

    private static final String SKILL_LEVEL_TABLE_NAME = "hr_emp_skill_level";
    private static final String SKILL_LEVEL_TYPE_NAME = "SKILL_LEVEL";
    private static final String APPOINTMENT_TABLE_NAME = "hr_emp_appointment";
    private static final String POSITION_TYPE_NAME = "APPOINTMENT_POSITION";
    private final HrEmpBasicMapper hrEmpBasicMapper;

    private final HrEmpEducationMapper hrEmpEducationMapper;

    private final HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService;

    private final HrEmpMajorInfoServiceImpl hrEmpMajorInfoService;

    private final HrEmpProfessionalQualificationMapper hrEmpProfessionalQualificationMapper;

    private final HrEmpSkillLevelMapper hrEmpSkillLevelMapper;

    private final CodeInfoServiceImpl codeInfoService;

    private final WkCoreAPi wkCoreApi;

    private static final String TABLE_NAME = "hr_emp_basic";

    private static final String POLITICAL_TYPE_NAME = "BASIC_POLITICAL";

    private static final String EDUCATION_TABLE_NAME = "hr_emp_education";

    private static final String EDUCATION_TYPE_NAME = "EDU_EDU";

    private static final String DEGREE_TYPE_NAME = "EDU_DEGREE";

    private static final String INFO_MANAGER_TYPE_NAME = "info_manager";

    private final WkDataAPi wkDataAPi;

    private final SysServerClient sysServerClient;

    private final DictTypeUtils dictTypeUtils;
    private final HrEmpAppointmentMapper hrEmpAppointmentMapper;
    private final DictUtils dictUtils;

    @Autowired
    public HrEmpBasicServiceImpl(HrEmpBasicMapper hrEmpBasicMapper, HrEmpEducationMapper hrEmpEducationMapper, HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService, HrEmpMajorInfoServiceImpl hrEmpMajorInfoService, HrEmpProfessionalQualificationMapper hrEmpProfessionalQualificationMapper, HrEmpSkillLevelMapper hrEmpSkillLevelMapper, WkCoreAPi wkCoreApi, CodeInfoServiceImpl codeInfoService, WkDataAPi wkDataApi, @Qualifier("com.crrc.client.SysServerClient") SysServerClient sysServerClient, DictTypeUtils dictTypeUtils, HrEmpAppointmentMapper hrEmpAppointmentMapper, DictUtils dictUtils) {
        this.hrEmpBasicMapper = hrEmpBasicMapper;
        this.hrEmpEducationMapper = hrEmpEducationMapper;
        this.hrEmpSchoolInfoService = hrEmpSchoolInfoService;
        this.hrEmpMajorInfoService = hrEmpMajorInfoService;
        this.hrEmpProfessionalQualificationMapper = hrEmpProfessionalQualificationMapper;
        this.hrEmpSkillLevelMapper = hrEmpSkillLevelMapper;
        this.codeInfoService = codeInfoService;
        this.wkCoreApi = wkCoreApi;
        this.wkDataAPi = wkDataApi;
        this.sysServerClient = sysServerClient;
        this.dictTypeUtils = dictTypeUtils;
        this.hrEmpAppointmentMapper = hrEmpAppointmentMapper;
        this.dictUtils = dictUtils;
    }

    // 模糊分页查询，用于给考勤打卡系统按姓名或档案号模糊查询
    public Page<HrEmpBasic> queryFuzzy(Page<HrEmpBasic> page, HrEmpBasic queryReqVO) {
        MPJLambdaWrapper<HrEmpBasic> wrapper = new MPJLambdaWrapper<>();

        // 基础查询条件
        wrapper.selectAll(HrEmpBasic.class);

        // 模糊查询条件
        if (StringUtils.isNotBlank(queryReqVO.getUserCode())) {
            wrapper.like(HrEmpBasic::getUserCode, queryReqVO.getUserCode());
        }
        if (StringUtils.isNotBlank(queryReqVO.getName())) {
            wrapper.like(HrEmpBasic::getName, queryReqVO.getName());
        }

        // 执行分页查询
        return baseMapper.selectJoinPage(page, HrEmpBasic.class, wrapper);
    }

    public List<HrEmpBasic> getCategoryInfo(List<HrEmpBasic> hrEmpBasicList) {
        Set<String> userCodeSet = hrEmpBasicList.stream().map(HrEmpBasic::getUserCode).collect(Collectors.toSet());
        LambdaQueryWrapper<HrEmpBasic> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HrEmpBasic::getUserCode, userCodeSet);
        return hrEmpBasicMapper.selectList(wrapper);
    }

    /**
     * 获取全量用户信息
     * @return 全量用户信息list
     */
    public List<HrEmpBasic> getAllUser() {
        MPJLambdaWrapper<HrEmpBasic> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(HrEmpBasic.class);
        return hrEmpBasicMapper.selectList(queryWrapper);
    }

    /**
     * 通过档案号获取部门Code
     * @param userCode 档案号
     * @return 部门Code
     */
    @IgnoreTenant
    public String getDept(String userCode){
        MPJLambdaWrapper<HrEmpBasic> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.select(HrEmpBasic::getDept)
                .eq(HrEmpBasic::getUserCode, userCode);
        try{
            HrEmpBasic empBasic = hrEmpBasicMapper.selectOne(queryWrapper);
            if(empBasic == null){
                return null;
            } else {
                return empBasic.getDept();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据传入的档案号列表查询用户信息列表
     * @param userCodeList 包含userCode的员工基本信息列表
     * @return 用户信息list
     */
    public List<HrEmpBasic> getListByUserCode(List<HrEmpBasic> userCodeList) {
        // 1. 从参数中提取userCode列表
        List<String> codes = userCodeList.stream()
                .map(HrEmpBasic::getUserCode)
                .collect(Collectors.toList());

        // 2. 构建查询条件
        MPJLambdaWrapper<HrEmpBasic> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(HrEmpBasic.class)
                .in(HrEmpBasic::getUserCode, codes);
        return hrEmpBasicMapper.selectList(queryWrapper);
    }

    /**
     * 根据传入的档案号查询用户信息
     * @param userCode 用户档案号
     * @return 用户信息list
     */
    public HrEmpBasic getByUserCode(String userCode) {
        // 构建查询条件
        MPJLambdaWrapper<HrEmpBasic> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper.selectAll(HrEmpBasic.class)
                .eq(HrEmpBasic::getUserCode, userCode);
        try{
            return hrEmpBasicMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 条件查询
     * 无参数返回全部数据
     * @param page 空白page，在controller层设置好size和pageNo
     * @param dto 查询实体
     * @return 分页list
     */
    public IPage<HrEmpBasicDTO> listData(IPage<HrEmpBasicDTO> page, HrEmpBasicSearchDTO dto) {

        // ========= 1. 先处理权限，保持跟原来逻辑完全一致 =========
        // 确保只要HrEmpBasic表有数据就能显示
        // 注意：这个条件要在基础表分页里保留
        // 下面的权限逻辑只是改了“使用谁的 wrapper”，业务逻辑不变

        if (
                RoleUtils.checkAnyMatch("hrsc_sub_emp_info_mgr", "hrsc_dept_emp_info_mgr")
        ) {
//            // 不查部门，查询公司
//            dto.setDept(null);
//
//            // 先去字典中判断是否为子公司的公司级管理员
//            Map<String, String> jobNo2CompanyCode = dictTypeUtils.getDict(COMPANY_MANAGER_DICT_CODE)
//                    .stream()
//                    .collect(Collectors.toMap(
//                            DictItemReqDTO::getItemCode,
//                            DictItemReqDTO::getItemName
//                    ));

//            // 根据员工获取部门编码
//            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
//            if (loginUser == null) {
//                throw new IllegalArgumentException("获取员工登录信息失败");
//            }
//            if (loginUser.getUser() == null || StringUtils.isBlank(loginUser.getUser().getJobNo())) {
//                throw new IllegalArgumentException("获取员工工号失败");
//            }
//            String jobNo = loginUser.getUser().getJobNo();
//
//            String orgCode;
//            // 如果在字典中配置了，则不需要进行后续查询
//            if (jobNo2CompanyCode.containsKey(jobNo)) {
//                orgCode = jobNo2CompanyCode.get(jobNo);
//            } else {
//                // 如果没有配置，则从basic里查询出员工的部门
//                List<HrEmpBasicDTO> dtoList = this.selectMsg(jobNo);
//                if (ObjectUtils.isEmpty(dtoList)) {
//                    throw new IllegalArgumentException("获取员工信息失败，员工不存在，userCode:" + jobNo);
//                }
//                orgCode = dtoList.get(0).getOrgCode();
//            }
//
//            // 获取所有的组织数据
//            ResponseResultDTO<List<OrganizationTreeDTO>> listResponseResultDTO = wkDataAPi.treeOrganization();
//            if (listResponseResultDTO == null || ObjectUtils.isEmpty(listResponseResultDTO.getData())) {
//                throw new IllegalArgumentException("外部接口错误，获取树形组织信息失败");
//            }
//            List<OrganizationTreeDTO> data = listResponseResultDTO.getData();
//
//            // 根据orgCode匹配公司及其下级部门能否有匹配项，如果包含，则获得其展平后的List（包含所有部门Code）
//            List<OrganizationTreeDTO> childrenOrgList = findParentAndFlattenList(data, orgCode);
//            if (childrenOrgList == null || childrenOrgList.isEmpty()) {
//                throw new IllegalArgumentException("获取公司失败，公司匹配错误，orgCode:" + orgCode);
//            }
//
//            dto.setDeptSet(childrenOrgList.stream()
//                    .map(OrganizationTreeDTO::getOrgCode)
//                    .collect(Collectors.toSet()));

            String orgCode = getCurrentUserOrgCode();

            // 查找字典数据
            // 这里code是deptCode，codeValue是userCode，orgCode和deptCode完全一致，都指部门编码，如流程与数字化部 01320103
            List<CodeInfo> items = codeInfoService.getCodeInfoList(TABLE_NAME, INFO_MANAGER_TYPE_NAME);

            // 查找当前人员是否是特殊配置的信息管理人
            String currentUserCode = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
            Set<String> managerOrgCodes = items.stream().filter(e -> e.getCodeValue().equals(currentUserCode)).map(CodeInfo::getCode).collect(Collectors.toSet());

            List<OrganizationTreeDTO> childrenOrgList = new ArrayList<>();
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

            dto.setDeptSet(childrenOrgList.stream()
                    .map(OrganizationTreeDTO::getOrgCode)
                    .collect(Collectors.toSet()));
        }

        // ========= 2. 用 HrEmpBasic 先做一次分页（只走主表，不 join 大表） =========

        long current = page.getCurrent();
        long size = page.getSize();

        IPage<HrEmpBasic> basicPage = new Page<>(current, size);

        LambdaQueryWrapper<HrEmpBasic> basicWrapper = Wrappers.<HrEmpBasic>lambdaQuery()
                .isNotNull(HrEmpBasic::getUserCode);

        // 这里的 buildFuzzyConditions 需要只使用 HrEmpBasic 上的字段做条件
        buildFuzzyConditions(basicWrapper, dto);

        // 按照是否包含0132排序，这样可以让正常工号显示在前面
        basicWrapper.last("ORDER BY user_code LIKE '013200%' DESC, user_code like '01320006%' ASC, birth_date DESC NULLS LAST, user_code ASC");

        IPage<HrEmpBasic> empPage = hrEmpBasicMapper.selectPage(basicPage, basicWrapper);

        if (ObjectUtils.isEmpty(empPage.getRecords())) {
            // 无数据，直接返回一个空分页
            Page<HrEmpBasicDTO> empty = new Page<>(current, size, 0L);
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        // 当前页对应的 userCode 列表（去重）
        List<String> userCodes = empPage.getRecords().stream()
                .map(HrEmpBasic::getUserCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        // ========= 3. 再针对本页 userCode 做一次小范围 join 查询，拼装 DTO =========

        MPJLambdaWrapper<HrEmpBasic> joinWrapper = JoinWrappers.lambda(HrEmpBasic.class)
                .distinct()
                .selectAll(HrEmpBasic.class)
                .select(HrEmpCertificate::getCertiId, HrEmpCertificate::getCertiType)
                .leftJoin(HrEmpCertificate.class, HrEmpCertificate::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpHouseHold::getHouseHoldType, HrEmpHouseHold::getHouseHoldLocation, HrEmpHouseHold::getHouseHoldLocationDetail)
                .leftJoin(HrEmpHouseHold.class, HrEmpHouseHold::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpCommunication::getMobilePhone, HrEmpCommunication::getPersonalEmail,
                        HrEmpCommunication::getAddress, HrEmpCommunication::getWorkEmail)
                .leftJoin(HrEmpCommunication.class, HrEmpCommunication::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpHealth::getHealthStatus)
                .leftJoin(HrEmpHealth.class, HrEmpHealth::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpProfessionalSkill::getSkillLevel, HrEmpProfessionalSkill::getSkillName)
                .leftJoin(HrEmpProfessionalSkill.class, HrEmpProfessionalSkill::getUserCode, HrEmpBasic::getUserCode)
                .select(HrEmpEducation::getSchoolCode, HrEmpEducation::getEducation, HrEmpEducation::getDegree,
                        HrEmpEducation::getIsHighestEducation, HrEmpEducation::getMajorCode)
                .leftJoin(HrEmpEducation.class,
                        on -> on.eq(HrEmpEducation::getUserCode, HrEmpBasic::getUserCode)
                                .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                                .eq(HrEmpEducation::getIsHighestDegree, SystemConstants.TRUE_FLAG)
                )
                // .leftJoin(HrEmpAppointment.class, HrEmpAppointment::getUserCode, HrEmpBasic::getUserCode)
                // 关键：只查本页的员工，避免全表 join
                .in(HrEmpBasic::getUserCode, userCodes)
                .orderByAsc(HrEmpBasic::getUserCode);

        // 这里不再分页，因为 userCode 已经是分页后的结果，数据量很小
        List<HrEmpBasicDTO> dtoList = hrEmpBasicMapper.selectJoinList(HrEmpBasicDTO.class, joinWrapper);

        // 翻译部门
        dtoList.forEach(item -> item.setDepartmentName(DeptUtils.getDeptNameByCode(item.getDept())));

        // ========= 4. 组装 IPage<HrEmpBasicDTO>，保持分页信息一致 =========

        Page<HrEmpBasicDTO> resultPage = new Page<>(empPage.getCurrent(), empPage.getSize(), empPage.getTotal());
        resultPage.setRecords(dtoList);
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
        List<HrEmpBasicDTO> dtoList = this.selectMsg(jobNo);
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

    private void buildFuzzyConditions(LambdaQueryWrapper<HrEmpBasic> wrapper, HrEmpBasicSearchDTO dto) {
        // 链式构建多个模糊查询条件
        wrapper
                .like(StringUtils.isNotBlank(dto.getName()),
                        HrEmpBasic::getName,
                        dto.getName())
                .like(StringUtils.isNotBlank(dto.getUserCode()),
                        HrEmpBasic::getUserCode,
                        dto.getUserCode())
                .like(StringUtils.isNotBlank(dto.getDept()),
                        HrEmpBasic::getDept,
                        dto.getDept())
                .like(StringUtils.isNotBlank(dto.getIsEmployed()),
                        HrEmpBasic::getIsEmployed,
                        dto.getIsEmployed())
                .in(ObjectUtils.isNotEmpty(dto.getDeptSet()),
                        HrEmpBasic::getDept,
                        dto.getDeptSet())
                .eq(StringUtils.isNotBlank(dto.getCategory1()),
                        HrEmpBasic::getCategory1,
                        dto.getCategory1());

        Set<String> category8StatusSet = new HashSet<>(Arrays.asList(
                "002",
                "004",
                "008"
        ));
        // 判断dto中的dutyStatus字段，如果是002、004、008（借、派、助），则从人员分类8中筛选
        if(StringUtils.isNotBlank(dto.getDutyStatus()) && category8StatusSet.contains(dto.getDutyStatus())) {
            wrapper.eq(HrEmpBasic::getCategory8, dto.getDutyStatus());
        }
        // 否则从在岗状态中筛选
        if(StringUtils.isNotBlank(dto.getDutyStatus()) && !category8StatusSet.contains(dto.getDutyStatus())) {
            wrapper.eq(HrEmpBasic::getStatus, dto.getDutyStatus());
        }
    }


    /**
     * 通过档案号查询员工完整信息（含关联表数据）
     * @param userCode 员工档案号
     * @return 员工基本信息DTO集合（通常仅1条记录）
     */
    @Override
    public List<HrEmpBasicDTO> selectMsg(String userCode) {
        try{
            // 1. 主表及左连接表查询
            List<HrEmpBasicDTO> result = queryBaseInfoWithJoins(userCode);

            if (result.isEmpty()) {
                return Collections.emptyList();
            }

            // 3. 查询关联子表数据
            SubTableData subData = querySubTableData(userCode);

            // 处理是否可编辑
            // 超管均可编辑
            if(RoleUtils.checkAnyMatch("hrsc_super_admin")) {
                result.get(0).setIsEditable("1");
            } else if(RoleUtils.checkAnyMatch("hrsc_emp_non_inc_info_mgr")) {
                // 如果是非在职管理员，那么可编辑非在职数据
                String userName = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
                result.get(0).setIsEditable(result.get(0).getUserCode().equals(userName) || "0".equals(result.get(0).getIsEmployed()) ? "1" : "0");
            } else {
                String userName = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
                result.get(0).setIsEditable(result.get(0).getUserCode().equals(userName) ? "1" : "0");
            }



            // 4. 处理子表数据（当所有子表数据唯一时）
            if (subData.isAllUnique()) {
                mergeSubTableData(result.get(0), subData);
            }

            return result;
        } catch (Exception e) {
            log.warn("查询异常,{}", e.getMessage(), e);
            return queryBaseInfoWithJoins(userCode);
        }

    }

    public void resolveFields(HrEmpBasicDTO basic) {
        resolveFields(Collections.singletonList(basic));
    }

    public void resolveFields(List<HrEmpBasicDTO> result) {
        // 需要在mergeSubTableData之后进行部门名称转换
        resolveDepartmentName(result.get(0));

        // 处理最高学历
        resolveEducation(result.get(0));

        // 处理最高学位
        resolveDegree(result.get(0));

        // 处理政治面貌
        resolvePolitical(result.get(0));

        // 5. 处理年龄
        resolveAge(result.get(0));

        // 6. 处理人员分类1,4,7,8
        resolveCategory(result.get(0));

        // 6. 处理人员在岗状态
        resolveStatus(result.get(0));

        // 处理职位信息
        resolvePosition(result.get(0));

        // 处理技能等级信息
        resolveSkillLevel(result.get(0));

        // 处理附件信息
        resolveFiles(result.get(0));
    }

    private void resolveSkillLevel(HrEmpBasicDTO hrEmpBasicDTO) {
        hrEmpBasicDTO.setSkillLevel(
            codeInfoService.translateCode(hrEmpBasicDTO.getSkillLevel(), SKILL_LEVEL_TABLE_NAME, SKILL_LEVEL_TYPE_NAME)
        );
    }

    private void resolvePosition(HrEmpBasicDTO hrEmpBasicDTO) {
        Map<String, String> translateMap = codeInfoService.getTranslateMap(APPOINTMENT_TABLE_NAME, POSITION_TYPE_NAME);
        hrEmpBasicDTO.setPosition(translateMap.getOrDefault(hrEmpBasicDTO.getPosition(), hrEmpBasicDTO.getPosition()));
    }

    private void resolveEducation(HrEmpBasicDTO hrEmpBasicDTO) {
        Map<String, String> translateMap = codeInfoService.getTranslateMap(EDUCATION_TABLE_NAME, EDUCATION_TYPE_NAME);
        hrEmpBasicDTO.setEducationValue(translateMap.getOrDefault(hrEmpBasicDTO.getEducation(), hrEmpBasicDTO.getEducation()));
    }

    private void resolveDegree(HrEmpBasicDTO hrEmpBasicDTO) {
        Map<String, String> translateMap = codeInfoService.getTranslateMap(EDUCATION_TABLE_NAME, DEGREE_TYPE_NAME);
        hrEmpBasicDTO.setDegreeValue(translateMap.getOrDefault(hrEmpBasicDTO.getDegree(), hrEmpBasicDTO.getDegree()));
    }

    private void resolvePolitical(HrEmpBasicDTO hrEmpBasicDTO) {
        Map<String, String> translateMap = codeInfoService.getTranslateMap(TABLE_NAME, POLITICAL_TYPE_NAME);
        hrEmpBasicDTO.setPoliticalValue(translateMap.getOrDefault(hrEmpBasicDTO.getPolitical(), hrEmpBasicDTO.getPolitical()));
    }

    /**
     * 解析人员附件信息
     */
    public void resolveFiles(HrEmpBasicDTO dto) {
        try {
            FileUtils.resolveFilesByHeaderId(dto);
        } catch (Exception e) {
            log.warn("{}，的人员基本信息附件信息解析失败, {}" ,dto.getUserCode(), e.getMessage());
        }
    }


    /**
     * 主表关联查询（基础信息+左连接表）
     */
    private List<HrEmpBasicDTO> queryBaseInfoWithJoins(String userCode) {
        MPJLambdaWrapper<HrEmpBasic> wrapper = JoinWrappers.lambda(HrEmpBasic.class)
                .selectAll(HrEmpBasic.class)
                // 证书信息
                .select(HrEmpCertificate::getCertiId, HrEmpCertificate::getCertiType)
                .leftJoin(HrEmpCertificate.class, HrEmpCertificate::getUserCode, HrEmpBasic::getUserCode)
                // 户籍信息
                .select(HrEmpHouseHold::getHouseHoldType, HrEmpHouseHold::getHouseHoldLocation, HrEmpHouseHold::getHouseHoldLocationDetail)
                .leftJoin(HrEmpHouseHold.class, HrEmpHouseHold::getUserCode, HrEmpBasic::getUserCode)
                // 联系信息
                .select(HrEmpCommunication::getMobilePhone, HrEmpCommunication::getPersonalEmail, HrEmpCommunication::getAddress, HrEmpCommunication::getWorkEmail)
                .leftJoin(HrEmpCommunication.class, HrEmpCommunication::getUserCode, HrEmpBasic::getUserCode)
                // 健康信息
                .select(HrEmpHealth::getHealthStatus)
                .leftJoin(HrEmpHealth.class, HrEmpHealth::getUserCode, HrEmpBasic::getUserCode)
                .eq(HrEmpBasic::getUserCode, userCode);

        return hrEmpBasicMapper.selectJoinList(HrEmpBasicDTO.class, wrapper);
    }

    /**
     * 一次连接/一次 SQL，把多个 userCode 的联表信息查出来
     */
    public List<HrEmpBasicDTO> queryBaseInfoListWithJoins(List<String> userCodeList) {
        if (userCodeList == null || userCodeList.isEmpty()) {
            return Collections.emptyList();
        }

        MPJLambdaWrapper<HrEmpBasic> wrapper = JoinWrappers.lambda(HrEmpBasic.class)
                .selectAll(HrEmpBasic.class)
                // 证书信息（如一人多证，可能带来多行；见下方“去重/聚合策略”）
                .select(HrEmpCertificate::getCertiId, HrEmpCertificate::getCertiType)
                .leftJoin(HrEmpCertificate.class, HrEmpCertificate::getUserCode, HrEmpBasic::getUserCode)
                // 户籍信息（通常一对一）
                .select(HrEmpHouseHold::getHouseHoldType, HrEmpHouseHold::getHouseHoldLocation, HrEmpHouseHold::getHouseHoldLocationDetail)
                .leftJoin(HrEmpHouseHold.class, HrEmpHouseHold::getUserCode, HrEmpBasic::getUserCode)
                // 联系信息（通常一对一）
                .select(HrEmpCommunication::getMobilePhone, HrEmpCommunication::getPersonalEmail, HrEmpCommunication::getAddress, HrEmpCommunication::getWorkEmail)
                .leftJoin(HrEmpCommunication.class, HrEmpCommunication::getUserCode, HrEmpBasic::getUserCode)
                // 健康信息（通常一对一）
                .select(HrEmpHealth::getHealthStatus)
                .leftJoin(HrEmpHealth.class, HrEmpHealth::getUserCode, HrEmpBasic::getUserCode)
                .in(HrEmpBasic::getUserCode, userCodeList);

        return hrEmpBasicMapper.selectJoinList(HrEmpBasicDTO.class, wrapper);
    }


    /**
     * 解析人员在岗状态
     */
    private void resolveStatus(HrEmpBasicDTO dto) {
        // 查询的表名
        String tableName = "hr_emp_basic";
        try {
            if (StringUtils.isNotBlank(dto.getStatus())) {
                String status = codeInfoService.translateCode(dto.getStatus(), tableName, "per_status");
                dto.setStatus(status);
            }
        } catch (Exception e) {
            log.warn("人员在岗状态解析失败, {}", e.getMessage());
        }
    }


    /**
     * 解析人员分类1478
     */
    private void resolveCategory(HrEmpBasicDTO dto) {
        log.info("进入方法体成功");
        // 查询的表名
        String tableName = "person_category";

        try {
            if (StringUtils.isNotBlank(dto.getCategory1())) {
                String category1 = codeInfoService.translateCode(dto.getCategory1(), tableName, "category1");
                dto.setCategory1(category1);
            }

            if (StringUtils.isNotBlank(dto.getCategory4())) {
                String category4 = codeInfoService.translateCode(dto.getCategory4(), tableName, "category4");
                dto.setCategory4(category4);
            }

            if (StringUtils.isNotBlank(dto.getCategory7())) {
                String category7 = codeInfoService.translateCode(dto.getCategory7(), tableName, "category7");
                dto.setCategory7(category7);
            }

            if (StringUtils.isNotBlank(dto.getCategory8())) {
                String category8 = codeInfoService.translateCode(dto.getCategory8(), tableName, "category8");
                dto.setCategory8(category8);
            }

        } catch (Exception e) {
            log.warn("人员分类1478解析失败, {}", e.getMessage());
        }
    }

    /**
     * 解析部门名称（通过组织编码）
     */
    private void resolveDepartmentName(HrEmpBasicDTO dto) {
        if (StringUtils.isBlank(dto.getDept())) {
            return;
        }

        try {
            String deptName = DeptUtils.getDeptNameByCode(dto.getDept());
            dto.setOrgCode(dto.getDept());
            dto.setDept(deptName);
        } catch (Exception e) {
            log.warn("部门名称查询失败, orgCode: {}", dto.getDept(), e);
        }
    }

    /**
     * 计算并设置员工年龄（基于出生日期计算周岁）
     * @param dto 包含出生日期的员工DTO对象
     */
    private void resolveAge(HrEmpBasicDTO dto) {
        if (dto.getBirthDate() == null) {
            log.warn("档案号：{}，人员出生日期为空", dto.getUserCode());
            dto.setAge(null);
            return;
        }

        try {
            int age = Period.between(dto.getBirthDate(), LocalDate.now()).getYears();
            dto.setAge(Math.max(age, 0));
        } catch (Exception e) {
            log.error("年龄计算异常，档案号：{}", dto.getUserCode(), e);
            dto.setAge(null);
        }
    }

    /**
     * 批量查询子表数据（教育/技能/资格）
     */
    private Map<String, SubTableData> querySubTableDataList(List<String> userCodes) {
        if (userCodes == null || userCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        // 去重保持顺序
        List<String> codes = userCodes.stream().filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());

        // 建立userCode -> SubTableData Map
        Map<String, SubTableData> resultMap = new LinkedHashMap<>();
        codes.forEach(c -> resultMap.put(c, new SubTableData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())));

        // --- 1) 教育（最高学历）批量查询 ---

        List<HrEmpEducation> eduList = hrEmpEducationMapper.selectList(
                JoinWrappers.lambda(HrEmpEducation.class)
                        .select(HrEmpEducation::getUserCode,
                                HrEmpEducation::getSchoolCode,
                                HrEmpEducation::getEducation,
                                HrEmpEducation::getDegree,
                                HrEmpEducation::getMajorCode)
                        .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                        .in(HrEmpEducation::getUserCode, userCodes)
        );
            if (eduList != null) {
            eduList.forEach(e -> {
                SubTableData d = resultMap.get(e.getUserCode());
                if (d != null) {
                    d.educationList.add(e);
                }
            });
        }


        // --- 2) 技能等级（最高）批量查询 ---

        List<HrEmpSkillLevel> skillList = hrEmpSkillLevelMapper.selectList(
                JoinWrappers.lambda(HrEmpSkillLevel.class)
                        .select(HrEmpSkillLevel::getUserCode,
                                HrEmpSkillLevel::getSkillLevel)
                        .eq(HrEmpSkillLevel::getIsHighest, SystemConstants.TRUE_FLAG)
                        .in(HrEmpSkillLevel::getUserCode, userCodes)
        );
        if (skillList != null) {
            skillList.forEach(s -> {
                SubTableData d = resultMap.get(s.getUserCode());
                if (d != null) {
                    d.skillLevelList.add(s);
                }
            });
        }


        // --- 3) 职业资格（最高）批量查询 ---

        List<HrEmpProfessionalQualification> quaList = hrEmpProfessionalQualificationMapper.selectList(
                JoinWrappers.lambda(HrEmpProfessionalQualification.class)
                        .select(HrEmpProfessionalQualification::getUserCode,
                                HrEmpProfessionalQualification::getQuaId)
                        .eq(HrEmpProfessionalQualification::getIsHighest, SystemConstants.TRUE_FLAG)
                        .in(HrEmpProfessionalQualification::getUserCode, userCodes)
        );
        if (quaList != null) {
            quaList.forEach(q -> {
                SubTableData d = resultMap.get(q.getUserCode());
                if (d != null) {
                    d.qualificationList.add(q);
                }
            });
        }

        // --- 4) 任职信息（最近）批量查询 ---

        List<HrEmpAppointment> appointmentList = hrEmpAppointmentMapper.selectList(
                JoinWrappers.lambda(HrEmpAppointment.class)
                        .select(HrEmpAppointment::getUserCode,
                                HrEmpAppointment::getPosition)
                        .in(HrEmpAppointment::getUserCode, userCodes)
        );
        List<HrEmpAppointment> appointmentListSorted = appointmentList.stream().filter(
                e -> e.getStartDate() != null).sorted(Comparator.comparing(HrEmpAppointment::getStartDate).reversed()
        ).collect(Collectors.toList());
        if (!appointmentListSorted.isEmpty()) {
            SubTableData d = resultMap.get(appointmentListSorted.get(0).getUserCode());
            d.appointmentList.add(appointmentListSorted.get(0));
        }

        // 保持与入参顺序一致返回
        return resultMap;
    }

    /**
     * 查询子表数据（教育/技能/资格）
     */
    private SubTableData querySubTableData(String userCode) {
        SubTableData data = new SubTableData();

        // 教育信息（最高学历）
        data.educationList = hrEmpEducationMapper.selectList(
                JoinWrappers.lambda(HrEmpEducation.class)
                        .select(HrEmpEducation::getSchoolCode, HrEmpEducation::getEducation,
                                HrEmpEducation::getDegree, HrEmpEducation::getMajorCode)
                        .eq(HrEmpEducation::getIsHighestEducation, SystemConstants.TRUE_FLAG)
                        .eq(HrEmpEducation::getUserCode, userCode)
        );

        // 技能等级（最高等级）
        data.skillLevelList = hrEmpSkillLevelMapper.selectList(
                JoinWrappers.lambda(HrEmpSkillLevel.class)
                        .selectAll()
                        .eq(HrEmpSkillLevel::getIsHighest, SystemConstants.TRUE_FLAG)
                        .eq(HrEmpSkillLevel::getUserCode, userCode)
        );
        // 如果有多条，取取得时间最新的一条
        if(data.skillLevelList.size() > 1) {
            data.skillLevelList = data.skillLevelList.stream().filter(
                    e -> e.getGetDate() != null
            ).sorted(
                    Comparator.comparing(HrEmpSkillLevel::getGetDate).reversed()
            ).collect(Collectors.toList()).subList(0, 1);
        }

        // 职业资格（最高资格）
        data.qualificationList = hrEmpProfessionalQualificationMapper.selectList(
                JoinWrappers.lambda(HrEmpProfessionalQualification.class)
                        .selectAll()
                        .eq(HrEmpProfessionalQualification::getIsHighest, SystemConstants.TRUE_FLAG)
                        .eq(HrEmpProfessionalQualification::getUserCode, userCode)
        );

        // 如果有多条，取开始聘任时间最新的一条
        if(data.qualificationList.size() > 1) {
            data.qualificationList = data.qualificationList.stream().filter(
                    e -> e.getAppointedStartDate() != null
            ).sorted(
                    Comparator.comparing(HrEmpProfessionalQualification::getAppointedStartDate).reversed()
            ).collect(Collectors.toList()).subList(0, 1);
        }

        // 任职信息（最新一条）
        data.appointmentList = Collections.singletonList(hrEmpAppointmentMapper.selectList(
                JoinWrappers.lambda(HrEmpAppointment.class)
                        .selectAll()
                        .eq(HrEmpAppointment::getUserCode, userCode)
                        .orderByDesc(HrEmpAppointment::getCreateTime)
                        .last("NULLS LAST")
        ).stream().filter(e -> e.getStartDate() != null).sorted(
                Comparator.comparing(HrEmpAppointment::getStartDate).reversed()
        ).collect(Collectors.toList()).get(0));

        // 任职经历
        return data;
    }

    /**
     * 合并子表数据到主DTO
     */
    private void mergeSubTableData(HrEmpBasicDTO dto, SubTableData data) {
        // 处理教育信息
        if (!data.educationList.isEmpty()) {
            HrEmpEducation edu = data.educationList.get(0);
            // 补充院校/专业名称
            if(enrichEducationInfo(edu)) {
                dto.setSchoolName(edu.getSchoolName());
                dto.setEducation(edu.getEducation());
                dto.setDegree(edu.getDegree());
                dto.setMajor(edu.getMajor());
            }
        }

        // 处理技能等级
        if (!data.skillLevelList.isEmpty()) {
            dto.setSkillLevel(data.skillLevelList.get(0).getSkillLevel());
        }

        // 处理职业资格
        if (!data.qualificationList.isEmpty()) {
            dto.setQuaId(data.qualificationList.get(0).getQuaId());
        }

        // 处理职位
        if(!data.appointmentList.isEmpty()) {
            dto.setPosition(data.appointmentList.get(0).getPosition());
        }
    }

    /**
     * 补充教育明细信息（院校名称+专业名称）
     * @return 是否执行成功
     */
    private boolean enrichEducationInfo(HrEmpEducation edu) {
        try {
            // 院校名称
            HrEmpSchoolInfo school = hrEmpSchoolInfoService.selectByCode(edu.getSchoolCode());
            edu.setSchoolName(school != null ? school.getSchoolName() : edu.getSchoolCode());

            // 专业名称
            HrEmpMajorInfo major = hrEmpMajorInfoService.selectByCode(edu.getMajorCode());
            edu.setMajor(major != null ? major.getMajor() : edu.getMajorCode());
            return true;
        } catch (Exception e) {
            log.error("教育明细信息查询错误: {}", e.getMessage(), e);
            return false;
        }

    }

    public List<HrEmpBasicDTO> selectMsgList(List<String> userCodeList) {
        try {
            // 1. 主表及左连接表查询
            List<HrEmpBasicDTO> resultList = queryBaseInfoListWithJoins(userCodeList);
            if (ObjectUtils.isEmpty(resultList)) {
                return Collections.emptyList();
            }

            // 查询关联子表数据
            Map<String, SubTableData> stringSubTableDataMap = querySubTableDataList(userCodeList);

            if(RoleUtils.checkAnyMatch("hrsc_super_admin")) {
                resultList.forEach(result -> result.setIsEditable("1"));
            } else {
                String userName = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
                resultList.forEach(result -> result.setIsEditable(result.getUserCode().equals(userName) ? "1" : "0"));
            }

            resultList.forEach(result -> {
                // 3. 查询关联子表数据
                SubTableData subData = stringSubTableDataMap.get(result.getUserCode());

                // 4. 处理子表数据（当所有子表数据唯一时）
                if (subData.isAllUnique()) {
                    mergeSubTableData(result, subData);
                }
            });
            return resultList;
        } catch (Exception e) {
            log.warn("查询异常,{}", e.getMessage());
            return queryBaseInfoListWithJoins(userCodeList);
        }
    }

    public void resolveFieldsList(List<HrEmpBasicDTO> resultList) {
        Map<String, String> orgCode2Name = wkDataAPi.allOrganization().getData().stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
        ));

        Map<String, String> eduTranslateMap = codeInfoService.getTranslateMap(EDUCATION_TABLE_NAME, EDUCATION_TYPE_NAME);
        Map<String, String> politicalTranslateMap = codeInfoService.getTranslateMap(TABLE_NAME, POLITICAL_TYPE_NAME);
        resultList.forEach(result -> {

            // 处理最高学历
            result.setEducationValue(eduTranslateMap.getOrDefault(result.getEducation(), result.getEducation()));

            // 需要在mergeSubTableData之后再进行部门名称转换
            result.setDept(orgCode2Name.getOrDefault(result.getDept(), result.getDept()));

            // 处理政治面貌
            result.setPoliticalValue(politicalTranslateMap.getOrDefault(result.getPolitical(), result.getPolitical()));

            // 5. 处理年龄
            log.info("准备开始处理年龄信息");
            resolveAge(result);

            // 6. 处理人员分类1,4,7,8
            log.info("准备开始处理人员分类信息");
            resolveCategory(result);

            // 6. 处理人员在岗状态
            log.info("准备开始处理人员在岗状态信息");
            resolveStatus(result);

            // 处理职位信息
            resolvePosition(result);

            // 处理技能等级信息
            resolveSkillLevel(result);

            // 处理附件信息
            resolveFiles(result);
        });
    }

    /**
     * 子表数据容器（内部使用）
     */
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SubTableData {
        List<HrEmpEducation> educationList;
        List<HrEmpSkillLevel> skillLevelList;
        List<HrEmpProfessionalQualification> qualificationList;
        List<HrEmpAppointment> appointmentList;

        boolean isAllUnique() {
            return educationList.size() <= 1 &&
                    skillLevelList.size() <= 1 &&
                    qualificationList.size() <= 1 &&
                    appointmentList.size() <= 1;
        }
    }
}