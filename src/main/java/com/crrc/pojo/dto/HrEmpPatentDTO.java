package com.crrc.pojo.dto;

import com.crrc.entity.HrEmpPaper;
import com.crrc.entity.HrEmpPatent;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class HrEmpPatentDTO {

    @Id
    String id;

    String userCode;

    List<HrEmpPatent> list;
}
