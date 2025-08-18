package com.crrc.entity;

import lombok.Data;

@Data
public class ValidateKeyEntity extends BaseEntity {
    private String validateKey;

    @Override
    public String toString() {
        return "ValidateKeyEntity(dynamicFields=" + this.getDynamicFields() + ")";
    }
}
