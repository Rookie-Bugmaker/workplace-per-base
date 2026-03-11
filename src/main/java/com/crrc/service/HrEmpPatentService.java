package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpPaper;
import com.crrc.entity.HrEmpPatent;

import java.util.List;

/**
 * <p>
 * 专利信息表 服务类
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:36:49
 */
public interface HrEmpPatentService extends IService<HrEmpPatent> {

 List<HrEmpPatent> selectMsg(String userCode);

}