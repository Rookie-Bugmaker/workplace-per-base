package com.crrc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crrc.entity.HrEmpSalaryInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 更新员工考勤人脸图片（从个人资料图片同步）
     */
    @Update("UPDATE hr_emp_basic i " +
            "SET attendance_image_id = profile_picture " +
            "WHERE user_code = #{userCode}")
    void updateattendanceImage(@Param("userCode") String userCode);

}