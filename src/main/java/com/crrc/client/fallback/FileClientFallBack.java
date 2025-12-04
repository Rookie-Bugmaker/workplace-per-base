package com.crrc.client.fallback;

import com.crrc.client.FileClient;
import com.crrc.common.ResultData;
import com.crrc.pojo.dto.SysFileDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public class FileClientFallBack implements FileClient {


    @Override
    public ResultData<List<SysFileDTO>> findByFileHeaderIds(@RequestBody List<Long> fileHeaderIds){
        return ResultData.fallback();
    }
}
