package com.crrc.utils;

import com.crrc.api.WkCoreAPi;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.dto.DeptDTO;
import com.crrc.entity.dto.DictItemReqDTO;
import com.crrc.entity.dto.mainData.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author why
 */
@Component
public class DictTypeUtils {

    public static final String AGREE_TEMPLATE_TYPE = "template_type";

    private final WkCoreAPi wkCoreApi;
    private final WkDataAPi wkDataAPi;

    @Autowired
    public DictTypeUtils(
        WkCoreAPi wkCoreApi,
        WkDataAPi wkDataApi
    ) {
        this.wkCoreApi = wkCoreApi;
        this.wkDataAPi = wkDataApi;
    }


    public List<DictItemReqDTO> getDict(String dictCode){
        return wkCoreApi.getDict(dictCode).getData().getItems();
    }

    public String getDeptCodeById(Long deptId) {
        List<DeptDTO> deptDTOList = wkCoreApi.getDeptList(Collections.singletonList(deptId));
        if(deptDTOList != null && !deptDTOList.isEmpty()) {
            return deptDTOList.get(0).getOrgCode();
        }
        return null;
    }

    /**
     * 用于获得userCode转trueName的map
     * @param entityList 实体list
     * @param getUserCode 获得UserCode的方法
     * @return 转换的map
     */
    public <T> Map<String, String> getUserCode2NameMap(Collection<T> entityList, Function<? super T, String> getUserCode) {
        Map<String, String> result;
        try {
            List<String> codeList = entityList.stream().map(getUserCode).distinct().filter(Objects::nonNull).collect(Collectors.toList());
            // 收集为用户code转用户trueName的map
            result = codeList.stream().collect(
                Collectors.toMap(code -> code, code ->
                    wkDataAPi.getEmployee(code).getData().getEmpName()
                )
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("用户字典翻译错误！用户不存在");
        }
        return result;
    }

    /**
     * 用于获得deptCode转deptName的map
     * @param entityList 实体list
     * @param getDeptCode 获得DeptCode的方法
     * @return 转换的map
     */
    public <T> Map<String, String> getDeptCode2NameMap(Collection<T> entityList, Function<? super T, String> getDeptCode) {
        try {
            // 没有使用到，由于暂时没有非全量查询的接口
            // List<String> deptList = entityList.stream().map(getDeptCode).distinct().filter(Objects::nonNull).collect(Collectors.toList());
            List<OrganizationDTO> organizationList = wkDataAPi.allOrganization().getData();

            // 收集为deptCode到name的map， 返回全量的接口
            return organizationList.stream().collect(Collectors.toMap(
                OrganizationDTO::getOrgCode,
                OrganizationDTO::getOrgName
            ));
        } catch (Exception e) {
            throw new IllegalArgumentException("部门字典翻译错误！部门不存在");
        }
    }

    /**
     * 用于获得deptCode转deptName的map
     * @return 转换的map
     */
    public <T> Map<String, String> getDictCode2NameMap(String dictCode) {
        try {
            // 没有使用到，由于暂时没有非全量查询的接口
            // List<String> deptList = entityList.stream().map(getDeptCode).distinct().filter(Objects::nonNull).collect(Collectors.toList());
            List<DictItemReqDTO> dtoList = getDict(dictCode);

            // 收集为deptCode到name的map， 返回全量的接口
            return dtoList.stream().collect(Collectors.toMap(
                DictItemReqDTO::getItemCode,
                DictItemReqDTO::getItemName
            ));
        } catch (Exception e) {
            throw new IllegalArgumentException("部门字典翻译错误！部门不存在");
        }
    }
}
