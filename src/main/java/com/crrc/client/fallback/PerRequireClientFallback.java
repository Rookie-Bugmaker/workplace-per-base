package com.crrc.client.fallback;

import com.crrc.client.PerRequireClient;
import com.crrc.entity.CodeInfo;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.TreeVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * CodeInfoClient的熔断降级实现
 *
 * @author yukun
 */
@Component
public class PerRequireClientFallback implements PerRequireClient {

    @Override
    public ResponseResult<TreeVO<CodeInfo>> getTreeById(@RequestParam("id") String id) {
        return ResponseResult.fail(HttpStatus.SERVICE_UNAVAILABLE.value(), "获取字典表树形数据失败，服务不可用");
    }
}