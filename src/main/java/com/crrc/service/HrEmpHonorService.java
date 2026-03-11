package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.HrEmpHonor;
import com.crrc.entity.HrEmpPaper;

import java.util.List;

/**
 * <p>
 * 荣誉信息表 服务类
 * </p>
 *
 * @author mayiyao
 * @since 2026-03-10 11:38:02
 */
public interface HrEmpHonorService extends IService<HrEmpHonor> {

 List<HrEmpHonor> selectMsg(String userCode);
}