package com.crrc.pojo.dto;

import com.crrc.entity.BorrowedPersonnelApprovalForm;
import com.crrc.entity.BorrowedPersonnelApprovalFormRelation;
import lombok.Data;

import java.util.List;

@Data
public class BorrowedPersonnelApprovalFormRelationDetailDTO {
    BorrowedPersonnelApprovalFormRelation relation;
    List<BorrowedPersonnelApprovalForm> approvalFormList;
}
