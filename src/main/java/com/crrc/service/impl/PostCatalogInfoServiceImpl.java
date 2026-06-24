package com.crrc.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.constants.PositionManagementConstants;
import com.crrc.entity.PositionInfo;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PositionInfoMapper;
import com.crrc.mapper.PostCatalogInfoMapper;
import com.crrc.pojo.vo.PostCatalogInfoImportResultVO;
import com.crrc.pojo.vo.PostCatalogInfoImportVO;
import com.crrc.pojo.vo.PostCatalogInfoTreeVO;
import com.crrc.pojo.vo.PostCatalogInfoVO;
import com.crrc.service.PostCatalogInfoService;
import com.crrc.utils.PostCatalogInfoImportListener;
import com.crrc.utils.TreeUtils;
import com.crrc.utils.WrapperUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yukun
 */
@Service
@Slf4j
public class PostCatalogInfoServiceImpl extends WorkServiceImpl<PostCatalogInfoMapper, PostCatalogInfo> implements PostCatalogInfoService {
    private static final Long ROOT_PARENT_ID = 0L;
    private static final int MAX_IMPORT_ERROR_COUNT = 50;

    @Autowired
    private PositionInfoMapper positionInfoMapper;

    /**
     * 只能查询叶子节点！
     * @param reqVO 默认会拼装nodeLevel=3
     * @return 结果
     */
    public List<PostCatalogInfoVO> queryVO(PostCatalogInfo reqVO) {
        reqVO = reqVO == null ? new PostCatalogInfo() : reqVO;
        reqVO.setNodeLevel(PositionManagementConstants.CATALOG_NODE_LEVEL_TYPE);
        Map<Long, PostCatalogInfo> cacheMap = new HashMap<>();
        List<PostCatalogInfo> query = this.query(reqVO);
        // 带缓存的拼装
        return query.stream().map(type -> {
            PostCatalogInfoVO postCatalogInfoVO = new PostCatalogInfoVO();
            postCatalogInfoVO.setTypeName(type.getName());
            postCatalogInfoVO.setTypeCode(type.getCode());
            if (type.getParentId() == null) {
                return postCatalogInfoVO;
            }
            PostCatalogInfo group = cacheMap.computeIfAbsent(type.getParentId(), this.baseMapper::selectById);
            if (group == null) {
                return postCatalogInfoVO;
            }
            postCatalogInfoVO.setGroupName(group.getName());
            postCatalogInfoVO.setGroupCode(group.getCode());
            if (group.getParentId() == null) {
                return postCatalogInfoVO;
            }
            PostCatalogInfo category = cacheMap.computeIfAbsent(group.getParentId(), this.baseMapper::selectById);
            if (category != null) {
                postCatalogInfoVO.setCategoryName(category.getName());
                postCatalogInfoVO.setCategoryCode(category.getCode());
            }
            return postCatalogInfoVO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据实体进行分页查询
     * @param reqVO
     * @return
     */
    public void page(Page<PostCatalogInfo> page, PostCatalogInfo reqVO) {
        normalizeQueryCondition(reqVO);
        QueryWrapper<PostCatalogInfo> queryWrapper = WrapperUtils.buildWrapper(reqVO);
        queryWrapper.orderByAsc("node_level", "code");
        this.page(page, queryWrapper);
    }

    @Override
    public List<PostCatalogInfo> query(PostCatalogInfo reqVO) {
        normalizeQueryCondition(reqVO);
        QueryWrapper<PostCatalogInfo> queryWrapper = WrapperUtils.buildWrapper(reqVO);
        queryWrapper.orderByAsc("node_level", "code");
        return this.baseMapper.selectList(queryWrapper);
    }

    public List<PostCatalogInfoTreeVO> queryTree() {
        QueryWrapper<PostCatalogInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("node_level", "code");
        List<PostCatalogInfo> postCatalogInfoList = this.baseMapper.selectList(queryWrapper);
        List<PostCatalogInfoTreeVO> fullList = postCatalogInfoList.stream().map(item -> {
            PostCatalogInfoTreeVO postCatalogInfoTreeVO = new PostCatalogInfoTreeVO();
            BeanUtils.copyProperties(item, postCatalogInfoTreeVO);
            return postCatalogInfoTreeVO;
        }).collect(Collectors.toList());

        return TreeUtils.buildCascadeList(
                fullList,
                PostCatalogInfoTreeVO::getId,
                PostCatalogInfoTreeVO::getParentId,
                PostCatalogInfoTreeVO::setChildren
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithValidation(PostCatalogInfo postCatalogInfo) {
        PostCatalogInfo entity = postCatalogInfo == null ? new PostCatalogInfo() : postCatalogInfo;
        validateAndFillCatalog(entity, null);
        return this.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithValidation(PostCatalogInfo postCatalogInfo) {
        if (postCatalogInfo == null || postCatalogInfo.getId() == null) {
            throw new IllegalArgumentException("职务族ID不能为空");
        }
        PostCatalogInfo existing = this.baseMapper.selectById(postCatalogInfo.getId());
        if (existing == null) {
            throw new IllegalArgumentException("职务族不存在或已删除，id=" + postCatalogInfo.getId());
        }
        fillMissingFieldsForUpdate(postCatalogInfo, existing);
        validateAndFillCatalog(postCatalogInfo, existing);
        return this.updateById(postCatalogInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeOrDisableById(Long id) {
        PostCatalogInfo entity = requireCatalog(id);
        if (hasEnabledOrDeletedChildren(id) || isReferencedByPosition(entity)) {
            return disableById(id);
        }
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableById(Long id) {
        requireCatalog(id);
        List<Long> ids = collectSelfAndDescendantIds(id);
        for (Long itemId : ids) {
            PostCatalogInfo update = new PostCatalogInfo();
            update.setId(itemId);
            update.setStatus(PositionManagementConstants.CATALOG_STATUS_DISABLED);
            this.updateById(update);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableById(Long id) {
        PostCatalogInfo entity = requireCatalog(id);
        String category = normalizeCategory(entity.getCategory(), entity.getNodeLevel());
        if (!PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(category)) {
            PostCatalogInfo parent = entity.getParentId() == null ? null : this.baseMapper.selectById(entity.getParentId());
            if (parent == null) {
                throw new IllegalArgumentException("启用失败：上级职务族不存在");
            }
            if (PositionManagementConstants.CATALOG_STATUS_DISABLED.equals(normalizeStatus(parent.getStatus()))) {
                throw new IllegalArgumentException("启用失败：请先启用上级职务族 " + parent.getCode() + "/" + parent.getName());
            }
        }
        PostCatalogInfo update = new PostCatalogInfo();
        update.setId(id);
        update.setStatus(PositionManagementConstants.CATALOG_STATUS_ENABLED);
        return this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostCatalogInfoImportResultVO importFull(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("导入文件不能为空");
        }
        PostCatalogInfoImportListener listener = new PostCatalogInfoImportListener();
        EasyExcel.read(file.getInputStream(), PostCatalogInfoImportVO.class, listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
        return importRows(listener.getRows());
    }

    private PostCatalogInfoImportResultVO importRows(List<PostCatalogInfoImportVO> rows) {
        PostCatalogInfoImportResultVO result = new PostCatalogInfoImportResultVO();
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("导入文件没有数据行");
        }
        result.setTotalRows(rows.size());

        List<PostCatalogImportPlan> plans = buildImportPlans(rows, result);
        if (result.hasError()) {
            throw new IllegalArgumentException("职务族导入校验失败：\n" + String.join("\n", result.getErrors()));
        }
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("导入文件没有有效数据行");
        }

        // 职类 -> 职群 -> 职种 顺序写入，保证同文件新增父节点后，子节点能解析到parentId。
        plans.sort(Comparator.comparingInt(PostCatalogImportPlan::getLevel));
        Map<String, PostCatalogInfo> codeToEntity = selectByCodes(plans.stream()
                .map(PostCatalogImportPlan::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        for (PostCatalogImportPlan plan : plans) {
            PostCatalogInfo entity = plan.getEntity();
            if (!PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(plan.getCategory())) {
                PostCatalogInfo parent = codeToEntity.get(plan.getParentCode());
                if (parent == null || parent.getId() == null) {
                    throw new IllegalArgumentException("职务族导入失败：未找到上级职务族编码 " + plan.getParentCode());
                }
                entity.setParentId(parent.getId());
                entity.setParentCode(parent.getCode());
                entity.setParentName(parent.getName());
            }
            validateAndFillCatalog(entity, plan.getExisting());
            if (plan.getExisting() == null) {
                this.save(entity);
                result.setInsertCount(result.getInsertCount() + 1);
            } else {
                entity.setId(plan.getExisting().getId());
                this.updateById(entity);
                result.setUpdateCount(result.getUpdateCount() + 1);
            }
            codeToEntity.put(entity.getCode(), entity);
        }
        return result;
    }

    private List<PostCatalogImportPlan> buildImportPlans(List<PostCatalogInfoImportVO> rows, PostCatalogInfoImportResultVO result) {
        List<PostCatalogImportPlan> plans = new ArrayList<>();
        Set<String> fileCodes = new HashSet<>();
        Set<String> allCodes = new LinkedHashSet<>();
        Set<String> allParentCodes = new LinkedHashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            PostCatalogInfoImportVO row = rows.get(i);
            if (isEmptyRow(row)) {
                continue;
            }
            int rowNo = i + 2;
            String code = trim(row.getNumber());
            if (StringUtils.isBlank(code)) {
                addImportError(result, rowNo, "number/职务族编码不能为空");
                continue;
            }
            if (!fileCodes.add(code)) {
                addImportError(result, rowNo, "导入文件中职务族编码重复：" + code);
            }
            allCodes.add(code);
            String parentCode = trim(row.getParentNumber());
            if (StringUtils.isNotBlank(parentCode)) {
                allParentCodes.add(parentCode);
            }
        }

        Set<String> queryCodes = new LinkedHashSet<>();
        queryCodes.addAll(allCodes);
        queryCodes.addAll(allParentCodes);
        Map<String, List<PostCatalogInfo>> existingGroupMap = selectGroupByCodes(queryCodes);
        Map<String, PostCatalogInfo> existingByCode = new LinkedHashMap<>();
        for (Map.Entry<String, List<PostCatalogInfo>> entry : existingGroupMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.getErrors().add("数据库中存在重复职务族编码，请先清理后再导入：" + entry.getKey());
                continue;
            }
            existingByCode.put(entry.getKey(), entry.getValue().get(0));
        }

        Map<String, PostCatalogImportPlan> filePlanMap = new LinkedHashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            PostCatalogInfoImportVO row = rows.get(i);
            if (isEmptyRow(row)) {
                continue;
            }
            int rowNo = i + 2;
            String code = trim(row.getNumber());
            if (StringUtils.isBlank(code)) {
                continue;
            }
            PostCatalogImportPlan plan = buildSingleImportPlan(row, rowNo, existingByCode, filePlanMap, result);
            if (plan != null) {
                plans.add(plan);
                filePlanMap.put(plan.getCode(), plan);
            }
            if (result.getErrors().size() >= MAX_IMPORT_ERROR_COUNT) {
                throw new IllegalArgumentException("职务族导入校验失败，错误过多：\n" + String.join("\n", result.getErrors()));
            }
        }
        validateParentReferences(plans, existingByCode, filePlanMap, result);
        return plans;
    }

    private PostCatalogImportPlan buildSingleImportPlan(PostCatalogInfoImportVO row,
                                                        int rowNo,
                                                        Map<String, PostCatalogInfo> existingByCode,
                                                        Map<String, PostCatalogImportPlan> filePlanMap,
                                                        PostCatalogInfoImportResultVO result) {
        String code = trim(row.getNumber());
        String name = trim(row.getName());
        String category;
        String status;
        try {
            category = normalizeCategory(row.getCategory(), null);
            status = normalizeStatus(row.getStatus());
        } catch (IllegalArgumentException e) {
            addImportError(result, rowNo, e.getMessage());
            return null;
        }

        if (StringUtils.isBlank(name)) {
            addImportError(result, rowNo, "name.zh_CN/职务族名称不能为空");
        }
        if (StringUtils.isBlank(row.getCategory())) {
            addImportError(result, rowNo, "ccar_category/类别不能为空");
        }
        if (StringUtils.isBlank(trim(row.getDescription()))) {
            addImportError(result, rowNo, "ccar_description/描述不能为空");
        }
        if (filePlanMap.containsKey(code)) {
            addImportError(result, rowNo, "导入文件中职务族编码重复：" + code);
        }

        PostCatalogInfo existing = existingByCode.get(code);
        String existingCategory = existing == null ? null : normalizeCategory(existing.getCategory(), existing.getNodeLevel());
        if (existing != null && StringUtils.isNotBlank(existingCategory) && !existingCategory.equals(category)) {
            addImportError(result, rowNo, "已存在职务族不允许变更类别，编码：" + code);
        }

        PostCatalogInfo entity = new PostCatalogInfo();
        entity.setId(existing == null ? null : existing.getId());
        entity.setCode(code);
        entity.setName(name);
        entity.setCategory(category);
        entity.setNodeLevel(categoryToNodeLevel(category));
        entity.setInputDept(trim(row.getInputDept()));
        entity.setModifyDept(trim(row.getModifyDept()));
        entity.setStatus(status);
        entity.setDescription(trim(row.getDescription()));
        String parentCode = trim(row.getParentNumber());
        String parentName = trim(row.getParentName());
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(category)) {
            entity.setParentId(ROOT_PARENT_ID);
            entity.setParentCode(null);
            entity.setParentName(null);
            if (StringUtils.isNotBlank(parentCode)) {
                addImportError(result, rowNo, "职类不能填写上级职务族编码");
            }
        } else {
            entity.setParentCode(parentCode);
            entity.setParentName(parentName);
            if (StringUtils.isBlank(parentCode)) {
                addImportError(result, rowNo, categoryToChinese(category) + "必须填写上级职务族编码");
            }
            if (code.equals(parentCode)) {
                addImportError(result, rowNo, "上级职务族编码不能等于自身编码：" + code);
            }
        }

        PostCatalogImportPlan plan = new PostCatalogImportPlan();
        plan.setRowNo(rowNo);
        plan.setCode(code);
        plan.setCategory(category);
        plan.setLevel(Integer.parseInt(entity.getNodeLevel()));
        plan.setParentCode(parentCode);
        plan.setParentName(parentName);
        plan.setExisting(existing);
        plan.setEntity(entity);
        return plan;
    }

    private void validateParentReferences(List<PostCatalogImportPlan> plans,
                                          Map<String, PostCatalogInfo> existingByCode,
                                          Map<String, PostCatalogImportPlan> filePlanMap,
                                          PostCatalogInfoImportResultVO result) {
        for (PostCatalogImportPlan plan : plans) {
            if (PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(plan.getCategory())) {
                continue;
            }
            String expectedParentCategory = expectedParentCategory(plan.getCategory());
            PostCatalogImportPlan parentPlan = filePlanMap.get(plan.getParentCode());
            PostCatalogInfo parentEntity = existingByCode.get(plan.getParentCode());
            String parentCategory;
            String parentName;
            String parentStatus;
            if (parentPlan != null) {
                parentCategory = parentPlan.getCategory();
                parentName = parentPlan.getEntity().getName();
                parentStatus = parentPlan.getEntity().getStatus();
            } else if (parentEntity != null) {
                parentCategory = normalizeCategory(parentEntity.getCategory(), parentEntity.getNodeLevel());
                parentName = parentEntity.getName();
                parentStatus = normalizeStatus(parentEntity.getStatus());
            } else {
                addImportError(result, plan.getRowNo(), "上级职务族编码不存在：" + plan.getParentCode());
                continue;
            }
            if (!expectedParentCategory.equals(parentCategory)) {
                addImportError(result, plan.getRowNo(), categoryToChinese(plan.getCategory())
                        + "的上级必须是" + categoryToChinese(expectedParentCategory)
                        + "，当前上级编码：" + plan.getParentCode());
            }
            if (StringUtils.isNotBlank(plan.getParentName()) && StringUtils.isNotBlank(parentName)
                    && !plan.getParentName().equals(parentName)) {
                addImportError(result, plan.getRowNo(), "上级职务族名称与编码不匹配，编码：" + plan.getParentCode()
                        + "，系统名称：" + parentName + "，导入名称：" + plan.getParentName());
            }
            if (PositionManagementConstants.CATALOG_STATUS_DISABLED.equals(parentStatus)
                    && PositionManagementConstants.CATALOG_STATUS_ENABLED.equals(plan.getEntity().getStatus())
                    && !containsParentInSameImport(parentPlan)) {
                addImportError(result, plan.getRowNo(), "上级职务族已停用，不能挂接启用/新增子节点：" + plan.getParentCode());
            }
        }
    }

    private boolean containsParentInSameImport(PostCatalogImportPlan parentPlan) {
        return parentPlan != null && PositionManagementConstants.CATALOG_STATUS_ENABLED.equals(parentPlan.getEntity().getStatus());
    }

    private void validateAndFillCatalog(PostCatalogInfo catalog, PostCatalogInfo existing) {
        if (catalog == null) {
            throw new IllegalArgumentException("职务族信息不能为空");
        }
        catalog.setCode(trim(catalog.getCode()));
        catalog.setName(trim(catalog.getName()));
        catalog.setCategory(normalizeCategory(catalog.getCategory(), catalog.getNodeLevel()));
        catalog.setNodeLevel(categoryToNodeLevel(catalog.getCategory()));
        catalog.setStatus(normalizeStatus(catalog.getStatus()));
        catalog.setInputDept(trim(catalog.getInputDept()));
        catalog.setModifyDept(trim(catalog.getModifyDept()));
        catalog.setDescription(trim(catalog.getDescription()));
        catalog.setParentCode(trim(catalog.getParentCode()));
        catalog.setParentName(trim(catalog.getParentName()));

        if (StringUtils.isBlank(catalog.getCode())) {
            throw new IllegalArgumentException("职务族编码不能为空，编码需手动填写，系统不自动生成");
        }
        if (StringUtils.isBlank(catalog.getName())) {
            throw new IllegalArgumentException("职务族名称不能为空");
        }
        if (StringUtils.isBlank(catalog.getDescription())) {
            throw new IllegalArgumentException("职务族描述不能为空");
        }
        checkCodeUnique(catalog.getCode(), catalog.getId());

        if (existing != null) {
            String existingCategory = normalizeCategory(existing.getCategory(), existing.getNodeLevel());
            if (StringUtils.isNotBlank(existingCategory) && !existingCategory.equals(catalog.getCategory())) {
                throw new IllegalArgumentException("已存在职务族不允许变更类别，编码：" + existing.getCode());
            }
        }

        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(catalog.getCategory())) {
            catalog.setParentId(ROOT_PARENT_ID);
            catalog.setParentCode(null);
            catalog.setParentName(null);
            return;
        }

        PostCatalogInfo parent = resolveParent(catalog);
        String expectedParentCategory = expectedParentCategory(catalog.getCategory());
        String parentCategory = normalizeCategory(parent.getCategory(), parent.getNodeLevel());
        if (!expectedParentCategory.equals(parentCategory)) {
            throw new IllegalArgumentException(categoryToChinese(catalog.getCategory())
                    + "的上级必须是" + categoryToChinese(expectedParentCategory));
        }
        if (catalog.getId() != null && catalog.getId().equals(parent.getId())) {
            throw new IllegalArgumentException("上级职务族不能选择自身");
        }
        if (PositionManagementConstants.CATALOG_STATUS_DISABLED.equals(normalizeStatus(parent.getStatus()))) {
            throw new IllegalArgumentException("上级职务族已停用，不能新增或挂接子节点：" + parent.getCode() + "/" + parent.getName());
        }
        catalog.setParentId(parent.getId());
        catalog.setParentCode(parent.getCode());
        catalog.setParentName(parent.getName());
    }

    private void fillMissingFieldsForUpdate(PostCatalogInfo target, PostCatalogInfo existing) {
        if (target.getCode() == null) {
            target.setCode(existing.getCode());
        }
        if (target.getName() == null) {
            target.setName(existing.getName());
        }
        if (target.getParentId() == null && target.getParentCode() == null) {
            target.setParentId(existing.getParentId());
            target.setParentCode(existing.getParentCode());
            target.setParentName(existing.getParentName());
        }
        if (target.getNodeLevel() == null) {
            target.setNodeLevel(existing.getNodeLevel());
        }
        if (target.getCategory() == null) {
            target.setCategory(existing.getCategory());
        }
        if (target.getInputDept() == null) {
            target.setInputDept(existing.getInputDept());
        }
        if (target.getModifyDept() == null) {
            target.setModifyDept(existing.getModifyDept());
        }
        if (target.getStatus() == null) {
            target.setStatus(existing.getStatus());
        }
        if (target.getDescription() == null) {
            target.setDescription(existing.getDescription());
        }
    }

    private PostCatalogInfo resolveParent(PostCatalogInfo catalog) {
        PostCatalogInfo parent = null;
        if (catalog.getParentId() != null && !ROOT_PARENT_ID.equals(catalog.getParentId())) {
            parent = this.baseMapper.selectById(catalog.getParentId());
        }
        if (parent == null && StringUtils.isNotBlank(catalog.getParentCode())) {
            parent = selectOneByCode(catalog.getParentCode());
        }
        if (parent == null) {
            throw new IllegalArgumentException(categoryToChinese(catalog.getCategory()) + "必须选择有效的上级职务族");
        }
        return parent;
    }

    private void checkCodeUnique(String code, Long currentId) {
        QueryWrapper<PostCatalogInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        if (currentId != null) {
            queryWrapper.ne("id", currentId);
        }
        Number count = this.baseMapper.selectCount(queryWrapper);
        if (count != null && count.longValue() > 0) {
            throw new IllegalArgumentException("职务族编码不能重复：" + code);
        }
    }

    private PostCatalogInfo selectOneByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        QueryWrapper<PostCatalogInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", trim(code));
        List<PostCatalogInfo> list = this.baseMapper.selectList(queryWrapper);
        if (list == null || list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new IllegalArgumentException("数据库中存在重复职务族编码，请先清理：" + code);
        }
        return list.get(0);
    }

    private Map<String, PostCatalogInfo> selectByCodes(Collection<String> codes) {
        Map<String, List<PostCatalogInfo>> groupMap = selectGroupByCodes(codes);
        Map<String, PostCatalogInfo> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<PostCatalogInfo>> entry : groupMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return result;
    }

    private Map<String, List<PostCatalogInfo>> selectGroupByCodes(Collection<String> codes) {
        Set<String> filteredCodes = codes == null ? Collections.emptySet() : codes.stream()
                .filter(StringUtils::isNotBlank)
                .map(this::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (filteredCodes.isEmpty()) {
            return new LinkedHashMap<>();
        }
        QueryWrapper<PostCatalogInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("code", filteredCodes);
        List<PostCatalogInfo> list = this.baseMapper.selectList(queryWrapper);
        Map<String, List<PostCatalogInfo>> map = new LinkedHashMap<>();
        for (PostCatalogInfo item : list) {
            map.computeIfAbsent(item.getCode(), k -> new ArrayList<>()).add(item);
        }
        return map;
    }

    private boolean hasEnabledOrDeletedChildren(Long id) {
        QueryWrapper<PostCatalogInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        Number count = this.baseMapper.selectCount(queryWrapper);
        return count != null && count.longValue() > 0;
    }

    private boolean isReferencedByPosition(PostCatalogInfo catalog) {
        if (catalog == null || StringUtils.isBlank(catalog.getCode())) {
            return false;
        }
        String category = normalizeCategory(catalog.getCategory(), catalog.getNodeLevel());
        String code = catalog.getCode();
        String name = catalog.getName();
        QueryWrapper<PositionInfo> queryWrapper = new QueryWrapper<>();
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(category)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("job_category", code);
                if (StringUtils.isNotBlank(name)) {
                    wrapper.or().eq("job_category", name);
                }
            });
        } else if (PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP.equals(category)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("job_group", code);
                if (StringUtils.isNotBlank(name)) {
                    wrapper.or().eq("job_group", name);
                }
            });
        } else if (PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE.equals(category)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("job_type", code);
                if (StringUtils.isNotBlank(name)) {
                    wrapper.or().eq("job_type", name);
                }
            });
        } else {
            return false;
        }
        Number count = positionInfoMapper.selectCount(queryWrapper);
        return count != null && count.longValue() > 0;
    }

    private List<Long> collectSelfAndDescendantIds(Long id) {
        List<PostCatalogInfo> all = this.baseMapper.selectList(new QueryWrapper<PostCatalogInfo>());
        Map<Long, List<PostCatalogInfo>> childrenMap = all.stream()
                .filter(item -> item.getParentId() != null)
                .collect(Collectors.groupingBy(PostCatalogInfo::getParentId));
        List<Long> ids = new ArrayList<>();
        collectDescendant(id, childrenMap, ids);
        return ids;
    }

    private void collectDescendant(Long id, Map<Long, List<PostCatalogInfo>> childrenMap, List<Long> ids) {
        ids.add(id);
        List<PostCatalogInfo> children = childrenMap.get(id);
        if (children == null || children.isEmpty()) {
            return;
        }
        for (PostCatalogInfo child : children) {
            collectDescendant(child.getId(), childrenMap, ids);
        }
    }

    private PostCatalogInfo requireCatalog(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("职务族ID不能为空");
        }
        PostCatalogInfo entity = this.baseMapper.selectById(id);
        if (entity == null) {
            throw new IllegalArgumentException("职务族不存在或已删除，id=" + id);
        }
        return entity;
    }

    private void normalizeQueryCondition(PostCatalogInfo reqVO) {
        if (reqVO == null) {
            return;
        }
        if (StringUtils.isNotBlank(reqVO.getCategory())) {
            reqVO.setCategory(normalizeCategory(reqVO.getCategory(), reqVO.getNodeLevel()));
            reqVO.setNodeLevel(null);
        } else if (StringUtils.isNotBlank(reqVO.getNodeLevel())) {
            reqVO.setNodeLevel(trim(reqVO.getNodeLevel()));
        }
        if (StringUtils.isNotBlank(reqVO.getStatus())) {
            reqVO.setStatus(normalizeStatus(reqVO.getStatus()));
        }
    }

    private String normalizeCategory(String category, String nodeLevel) {
        String value = trim(category);
        if (StringUtils.isBlank(value)) {
            value = trim(nodeLevel);
        }
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("职务族类别不能为空，取值为职类/职群/职种");
        }
        String upper = value.toUpperCase();
        if ("职类".equals(value) || "1".equals(value)
                || PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(upper)
                || "CATEGORY".equals(upper) || "JOBCATEGORY".equals(upper)) {
            return PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY;
        }
        if ("职群".equals(value) || "2".equals(value)
                || PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP.equals(upper)
                || "GROUP".equals(upper) || "JOBGROUP".equals(upper)) {
            return PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP;
        }
        if ("职种".equals(value) || "3".equals(value)
                || PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE.equals(upper)
                || "TYPE".equals(upper) || "JOBTYPE".equals(upper)) {
            return PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE;
        }
        throw new IllegalArgumentException("职务族类别不正确：" + value + "，取值为职类/职群/职种");
    }

    private String normalizeStatus(String status) {
        String value = trim(status);
        if (StringUtils.isBlank(value)) {
            return PositionManagementConstants.CATALOG_STATUS_ENABLED;
        }
        String upper = value.toUpperCase();
        if (PositionManagementConstants.CATALOG_STATUS_ENABLED.equals(upper)
                || "启用".equals(value) || "正常".equals(value) || "1".equals(value) || "Y".equals(upper) || "YES".equals(upper)) {
            return PositionManagementConstants.CATALOG_STATUS_ENABLED;
        }
        if (PositionManagementConstants.CATALOG_STATUS_DISABLED.equals(upper)
                || "停用".equals(value) || "禁用".equals(value) || "0".equals(value) || "N".equals(upper) || "NO".equals(upper)) {
            return PositionManagementConstants.CATALOG_STATUS_DISABLED;
        }
        throw new IllegalArgumentException("职务族数据状态不正确：" + value + "，取值为启用/停用");
    }

    private String categoryToNodeLevel(String category) {
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(category)) {
            return PositionManagementConstants.CATALOG_NODE_LEVEL_CATEGORY;
        }
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP.equals(category)) {
            return PositionManagementConstants.CATALOG_NODE_LEVEL_GROUP;
        }
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE.equals(category)) {
            return PositionManagementConstants.CATALOG_NODE_LEVEL_TYPE;
        }
        throw new IllegalArgumentException("职务族类别不正确：" + category);
    }

    private String expectedParentCategory(String category) {
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP.equals(category)) {
            return PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY;
        }
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE.equals(category)) {
            return PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP;
        }
        throw new IllegalArgumentException("职类不能设置上级职务族");
    }

    private String categoryToChinese(String category) {
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_CATEGORY.equals(category)) {
            return "职类";
        }
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_GROUP.equals(category)) {
            return "职群";
        }
        if (PositionManagementConstants.CATALOG_CATEGORY_JOB_TYPE.equals(category)) {
            return "职种";
        }
        return category;
    }

    private boolean isEmptyRow(PostCatalogInfoImportVO row) {
        if (row == null) {
            return true;
        }
        return StringUtils.isBlank(row.getNumber())
                && StringUtils.isBlank(row.getName())
                && StringUtils.isBlank(row.getCategory())
                && StringUtils.isBlank(row.getInputDept())
                && StringUtils.isBlank(row.getModifyDept())
                && StringUtils.isBlank(row.getStatus())
                && StringUtils.isBlank(row.getDescription())
                && StringUtils.isBlank(row.getParentNumber())
                && StringUtils.isBlank(row.getParentName());
    }

    private void addImportError(PostCatalogInfoImportResultVO result, int rowNo, String message) {
        result.getErrors().add("第" + rowNo + "行：" + message);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Data
    private static class PostCatalogImportPlan {
        private int rowNo;
        private int level;
        private String code;
        private String category;
        private String parentCode;
        private String parentName;
        private PostCatalogInfo existing;
        private PostCatalogInfo entity;
    }
}
