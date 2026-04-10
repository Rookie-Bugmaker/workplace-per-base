package com.crrc.constants;

import lombok.Getter;

@Getter
public enum RsCode {
    OK(0);

    private final int code;

    RsCode(int code) {
        this.code = code;
    }
}
