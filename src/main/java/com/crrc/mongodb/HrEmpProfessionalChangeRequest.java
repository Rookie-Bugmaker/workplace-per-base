package com.crrc.mongodb;

import com.crrc.entity.HrEmpProfessionalQualification;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "hr_emp_professional_change_requests")
public class HrEmpProfessionalChangeRequest {
    @Id
    private String id;
    private String userCode;
    private List<HrEmpProfessionalQualification> originalData;
    private List<HrEmpProfessionalQualification> changedData;
    private String status = "PENDING";
    private LocalDateTime createTime = LocalDateTime.now();
    private String processInstanceId;
    private Long wfId;
}