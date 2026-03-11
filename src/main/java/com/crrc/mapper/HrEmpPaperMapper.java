package com.crrc.mapper;

import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpPaper;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 论文论著信息表 Mapper 接口
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:33:05
 */
@Mapper
public interface HrEmpPaperMapper extends MPJBaseMapper<HrEmpPaper> {

}