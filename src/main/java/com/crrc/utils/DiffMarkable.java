package com.crrc.utils;

import java.util.List;

/**
 * 可标记变更的实体接口
 */
public interface DiffMarkable {
    String getId();

    List<String> getMarkList();

    void setMarkList(List<String> markList);
}