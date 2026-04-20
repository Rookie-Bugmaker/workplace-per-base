package com.crrc.pojo.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;

import java.util.List;

/**
 * 通用树形结构VO，用于接收远程服务返回的树形字典数据
 *
 * @param <T> 树节点数据类型
 * @author yukun
 */
@Data
public class TreeVO<T> {

    /**
     * 当前节点数据
     */
    @JsonUnwrapped
    private T node;

    /**
     * 子节点列表
     */
    private List<TreeVO<T>> children;
}