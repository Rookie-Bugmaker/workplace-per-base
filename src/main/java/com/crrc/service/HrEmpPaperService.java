package com.crrc.service;

import cn.hutool.db.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpPaper;

import java.util.List;

/**
 * <p>
 * 论文论著信息表 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-10 11:33:05
 */
public interface HrEmpPaperService extends IService<HrEmpPaper> {

 List<HrEmpPaper> selectMsg(String userCode);
// void resolveFiles(List<HrEmpPaper> list);
}