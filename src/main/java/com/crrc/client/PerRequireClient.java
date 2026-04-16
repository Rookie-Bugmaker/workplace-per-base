package com.crrc.client;

import com.crrc.client.fallback.PerRequireClientFallback;
import com.crrc.common.ResultData;
import com.crrc.entity.CodeInfo;
import com.crrc.pojo.vo.TreeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * workplace-per-require服务的字典表Feign客户端
 *
 * @author yukun
 */
@FeignClient(name = "workplace-per-require", fallback = PerRequireClientFallback.class)
public interface PerRequireClient {

    /**
     * 根据主键ID获得字典表树形数据
     *
     * @param id 主键编号
     * @return 字典表树形数据
     */
    @GetMapping("/codeInfo/get")
    ResultData<TreeVO<CodeInfo>> getTreeById(@RequestParam("id") String id);
}