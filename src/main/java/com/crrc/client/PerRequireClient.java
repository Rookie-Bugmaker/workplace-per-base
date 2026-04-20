package com.crrc.client;

import com.crrc.client.fallback.PerRequireClientFallback;
import com.crrc.entity.CodeInfo;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.dto.JobGrade;
import com.crrc.pojo.vo.TreeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    ResponseResult<TreeVO<CodeInfo>> getTreeById(@RequestParam("id") String id);


    /**
     * 获取薪资标准列表
     * @param queryReqVO 查询入参
     * @return 薪资标准列表
     */
    @PostMapping("/jobGrade/query")
    ResponseResult<List<JobGrade>> queryJobGrade(@Validated @RequestBody(required = false) JobGrade queryReqVO);
}