package com.crrc.mongodb;

import com.crrc.entity.HrEmpSkillLevel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "hr_emp_skills_change_requests")
public class HrEmpSkillsChangeRequest {
    @Id
    private String id;
    private String userCode;
    private List<HrEmpSkillLevel> originalData;
    private List<HrEmpSkillLevel> changedData;
    private String status = "PENDING";
    private LocalDateTime createTime = LocalDateTime.now();
    private String processInstanceId;
    private Long wfId;
}