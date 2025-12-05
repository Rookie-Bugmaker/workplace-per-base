package com.crrc.utils;

import cn.hutool.core.util.IdUtil;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.UpdateListDTO;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用列表更新工具类（支持新增/修改/删除三合一操作）
 * @param <T> 实体类型
 * @param <ID> 主键类型
 */
public class UpdateListUtil<T, ID> {

    private final Function<String, List<T>> queryFunction;
    private final Function<T, ID> idGetter;
    private final BiConsumer<T, String> userCodeSetter;
    private final BiConsumer<T, ID> idSetter;
    private final Consumer<List<T>> saveOrUpdateBatch;
    private final Consumer<Collection<ID>> removeByIds;

    public UpdateListUtil(
            Function<String, List<T>> queryFunction,
            Function<T, ID> idGetter,
            BiConsumer<T, String> userCodeSetter,
            BiConsumer<T, ID> idSetter,
            Consumer<List<T>> saveOrUpdateBatch,
            Consumer<Collection<ID>> removeByIds) {
        this.queryFunction = queryFunction;
        this.idGetter = idGetter;
        this.userCodeSetter = userCodeSetter;
        this.idSetter = idSetter;
        this.saveOrUpdateBatch = saveOrUpdateBatch;
        this.removeByIds = removeByIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Boolean> update(UpdateListDTO<T> updateReqVO) {
        try {
            // 查询现有数据
            List<T> existList = queryFunction.apply(updateReqVO.getUserCode());

            // 收集现有数据的ID
            Set<ID> existIds = existList.stream()
                    .map(idGetter)
                    .collect(Collectors.toSet());

            // 准备新增、更新和删除的数据
            List<T> toSaveOrUpdate = new ArrayList<>();

            for (T entity : updateReqVO.getList()) {
                ID id = idGetter.apply(entity);
                if (id == null) {
                    // 使用回调生成新ID
                    idSetter.accept(entity, generateId());
                }
                // 设置userCode
                userCodeSetter.accept(entity, updateReqVO.getUserCode());
                toSaveOrUpdate.add(entity);
                // 移除不需要删除的ID
                existIds.remove(id);
            }

            // 批量保存或更新
            if (!toSaveOrUpdate.isEmpty()) {
                saveOrUpdateBatch.accept(toSaveOrUpdate);
            }

            // 批量删除
            if (!existIds.isEmpty()) {
                removeByIds.accept(existIds);
            }

            return new ResponseResult<>(HttpStatus.OK.value(), "操作成功", true);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), "操作失败: " + e.getMessage(), false);
        }
    }

    // 生成ID的默认实现（可被子类覆盖）
    protected ID generateId() {
        // 默认使用UUID字符串
        return (ID) IdUtil.simpleUUID();
    }

    // 函数式接口定义
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}