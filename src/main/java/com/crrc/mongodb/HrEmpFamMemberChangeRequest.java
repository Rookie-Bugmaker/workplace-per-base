package com.crrc.mongodb;

import com.crrc.entity.HrEmpFamMember;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "hr_emp_famMem_change_requests")
public class HrEmpFamMemberChangeRequest {
    @Id
    private String id; // MongoDB ObjectId
    private String userCode;
    private List<HrEmpFamMember> originalData; // 变更前数据快照
    private List<HrEmpFamMember> changedData;  // 变更后数据
    private String status = "PENDING";  // PENDING/APPROVED/REJECTED
    private LocalDateTime createTime = LocalDateTime.now();
    private String processInstanceId;   // 流程实例ID
    private Long wfId;                  // 流程模板ID

}
