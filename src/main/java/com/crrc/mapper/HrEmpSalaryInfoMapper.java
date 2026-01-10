package com.crrc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpSalaryInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 员工薪资信息表 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-08-26 15:23:52
 */
@Mapper
public interface HrEmpSalaryInfoMapper extends BaseMapper<HrEmpSalaryInfo> {
    @Update("UPDATE hr_emp_salary_info i " +
            "SET name = b.name " +
            "FROM hr_emp_basic b " +
            "WHERE i.user_code = b.user_code")
    void syncNameFromBasic();

}