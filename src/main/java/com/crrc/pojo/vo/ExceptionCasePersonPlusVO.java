package com.crrc.pojo.vo;

import com.crrc.annotation.QueryMethod;
import com.crrc.entity.ExceptionCasePerson;
import com.crrc.enums.QueryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ExceptionCasePersonPlusVO extends ExceptionCasePerson {

    @QueryMethod(method = QueryType.EQ)
    @Schema(description = "EXT_SEC→外部排入，INT_LOAN→借用，借用类型，外部派入/借用")
    private String userType;
}
