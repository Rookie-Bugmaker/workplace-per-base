package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PositionInfo;

import java.util.List;

/**
 * @author yukun
 */
public interface PositionInfoService extends IService<PositionInfo> {
    /**
     * 翻译字段
     * @param records 需要翻译的职位信息
     */
    void resolveField(List<PositionInfo> records);

    /**
     * 按顺序查询
     */
    void queryByOrder(Page<PositionInfo> page, PositionInfo pageReqVO);
}