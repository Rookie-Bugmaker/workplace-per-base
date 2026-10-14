package com.crrc.mapper;

import com.crrc.entity.HrEmpBasic;
import com.crrc.pojo.dto.EmployeeLevelIdDTO;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 员工基本信息表 Mapper 接口
 * </p>
 *
 * @author why
 * @since 2025-07-26 16:18:11
 */
@Mapper
public interface HrEmpBasicMapper extends MPJBaseMapper<HrEmpBasic> {
    @Select("select\n" +
            "    heb.user_code as userCode,\n" +
            "    heb.dept as deptCode,\n" +
            "    hecp.level_id as levelId\n" +
            "from\n" +
            "    hr_emp_basic heb\n" +
            "left join (\n" +
            "    select\n" +
            "        user_code,\n" +
            "        level_id,\n" +
            "        row_number() over (\n" +
            "            partition by user_code\n" +
            "    order by\n" +
            "        create_time desc nulls last,\n" +
            "        qualification_date desc nulls last\n" +
            "        ) as rn\n" +
            "    from\n" +
            "        hr_emp_career_path\n" +
            "    where is_expired <> '1'\n" +
            "    and deleted = '0'\n" +
            ") hecp\n" +
            "    on\n" +
            "    heb.user_code = hecp.user_code\n" +
            "    and hecp.rn = 1;")
    List<EmployeeLevelIdDTO> queryEmployeeInfoWithLevelId();
}