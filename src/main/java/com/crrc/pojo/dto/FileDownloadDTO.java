package com.crrc.pojo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author why
 */
@Data
@NoArgsConstructor
public class FileDownloadDTO {
    private String fileId;
    private List<String> idList;
}
