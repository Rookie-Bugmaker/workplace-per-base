package com.crrc.client;

import com.crrc.client.fallback.FileClientFallBack;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "file-server", fallback = FileClientFallBack.class)
public interface FileClient {

    /**
     * 根据文件头ID查询文件信息(批量)
     *
     * @param fileHeaderIds 文件头ID
     * @return 文件列表
     */
    @PostMapping("findByFileHeaderIds")
    ResultData<List<SysFileDTO>> findByFileHeaderIds(@RequestBody List<Long> fileHeaderIds);
}