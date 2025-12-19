package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.CodeInfo;
import com.crrc.entity.PositionInfo;
import com.crrc.pojo.dto.SysUserDTO;
import com.crrc.pojo.dto.WorkflowWriteBackParamDTO;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yukun
 */
public interface PositionInfoService extends IService<PositionInfo> {
    /**
     * 翻译records里的所有需要翻译的字段
     * @param records 需要翻译的字段列表
     */
    void translateField(List<PositionInfo> records);

    /**
     * 查询已经生效的职位
     * @param page 分页数据
     * @param pageReqVO 查询条件
     */
    void queryActivated(Page<PositionInfo> page, PositionInfo pageReqVO);

    /**
     * 获取职层列表
     * @return 职层列表
     */
    List<CodeInfo> queryPositionLevel();

    /**
     * 保存或更新职层
     * @param codeInfo 需要更新的职层信息
     * @return 是否更新成功
     */
    Boolean saveOrUpdatePositionLevel(CodeInfo codeInfo);
}