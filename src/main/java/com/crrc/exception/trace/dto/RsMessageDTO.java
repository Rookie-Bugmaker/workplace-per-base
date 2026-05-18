package com.crrc.exception.trace.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = false)
@NoArgsConstructor
public class RsMessageDTO {
    private int errcode;
    private String errmsg;
}
