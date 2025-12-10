package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.entity.CodeInfo;
import com.crrc.mapper.CodeInfoMapper;
import com.crrc.service.CodeInfoService;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author mayiyao
 * @since 2025-10-15 10:51:03
 */
@Service
@Slf4j
public class CodeInfoServiceImpl extends WorkServiceImpl<CodeInfoMapper, CodeInfo> implements CodeInfoService {

    private final CodeInfoMapper codeInfoMapper;

    public CodeInfoServiceImpl(CodeInfoMapper codeInfoMapper) {
        this.codeInfoMapper = codeInfoMapper;
    }

    /**
     * 将指定的表中的指定字段进行字典值反写
     *
     * @param dictCode  字典key值
     * @param tableName 表名
     * @param type      字段名
     * @return 字典value值
     */
    @Override
    public String translateCode(String dictCode, String tableName, String type) {
        MPJLambdaWrapper<CodeInfo> wrapper = new MPJLambdaWrapper<CodeInfo>()
                .select(CodeInfo::getCode, CodeInfo::getCodeValue, CodeInfo::getType, CodeInfo::getTableName)
                .eq(CodeInfo::getTableName, tableName)
                .eq(CodeInfo::getType, type)
                .eq(CodeInfo::getCode, dictCode);
        try{
            CodeInfo codeInfo = codeInfoMapper.selectList(wrapper).get(0);
            if(StringUtils.isNotBlank(codeInfo.getCodeValue())){
                return codeInfo.getCodeValue();
            }else {
                log.warn("未查询到字典值，tableName:{}, dictCode:{}， type:{}", dictCode,tableName, type);
                return dictCode;
            }
        } catch (Exception e){
            log.error("字典查询失败, {}", e.getMessage());
            return dictCode;
        }
    }

    /**
     * 获得指定的表中的指定字段的字典值
     *
     * @param tableName 表名
     * @param type      字段名
     */
    @Override
    public Map<String, String> getTranslateMap(String tableName, String type) {
        LambdaQueryWrapper<CodeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeInfo::getTableName, tableName);
        wrapper.eq(CodeInfo::getType, type);
        List<CodeInfo> codeInfos = this.baseMapper.selectList(wrapper);

        return codeInfos.stream().collect(Collectors.toMap(
            CodeInfo::getCode,
            CodeInfo::getCodeValue,
            (a, b) -> a
        ));
    }
}