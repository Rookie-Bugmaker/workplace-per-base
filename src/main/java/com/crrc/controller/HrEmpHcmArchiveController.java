package com.crrc.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrEmpHcmArchive;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrEmpHcmArchiveServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工历史档案数据 前端控制器
 *
 * <p>该控制器提供只读的查询接口，不支持增删改操作。
 * 主要接口包括：
 * <ul>
 *     <li>POST /hrEmpHcmArchive/page - 分页查询</li>
 *     <li>POST /hrEmpHcmArchive/query - 列表查询（不分页）</li>
 *     <li>GET /hrEmpHcmArchive/get - 根据档案号精确查询</li>
 * </ul>
 * </p>
 *
 * <p>所有接口都进行异常处理和日志记录，返回友好的错误信息。</p>
 *
 * @author yukun
 * @since 2026-06-23
 */
@Slf4j
@RestController
@RequestMapping("/hrEmpHcmArchive")
@RefreshScope
@Tag(name = "员工历史档案数据", description = "员工历史档案数据查询接口")
public class HrEmpHcmArchiveController {

    /**
     * 注入服务实现类
     * 用于处理员工历史档案数据的查询业务逻辑
     */
    @Autowired
    private HrEmpHcmArchiveServiceImpl hrEmpHcmArchiveService;

    /**
     * 条件查询（分页）
     *
     * <p>根据传入的实体对象，动态拼装非空字段的查询条件，返回分页结果。
     * 如果查询实体为空，则查询所有数据并进行分页。</p>
     *
     * <p>支持的查询条件：
     * <ul>
     *     <li>da1 - 档案号（模糊查询）</li>
     *     <li>da3 - 姓名（模糊查询）</li>
     *     <li>da2 - 所在部门（模糊查询）</li>
     *     <li>da5 - 性别（精确查询）</li>
     *     <li>da6 - 民族（精确查询）</li>
     *     <li>da7 - 籍贯（模糊查询）</li>
     *     <li>da4 - 出生日期（精确查询）</li>
     *     <li>da24 - 参加工作时间（精确查询）</li>
     *     <li>da52 - 政治面貌（精确查询）</li>
     *     <li>da9 - 第一学历（精确查询）</li>
     * </ul>
     * </p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件
     * @param pageNo 页码，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 分页结果，包含总记录数和当前页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询员工历史档案数据")
    @AuditLog(value = "分页查询员工历史档案数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "queryEntity", description = "查询条件实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", example = "1")
    @Parameter(name = "pageSize", description = "每页大小", example = "10")
    public ResponseResult<IPage<HrEmpHcmArchive>> pageHrEmpHcmArchive(
            @Validated @RequestBody(required = false) HrEmpHcmArchive queryEntity,
            @RequestParam(required = false, defaultValue = "1") int pageNo,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // 如果查询实体为空，创建默认实体（查询所有数据）
        queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

        try {
            // 创建分页对象，设置当前页码和每页大小
            Page<HrEmpHcmArchive> page = new Page<>(pageNo, pageSize);

            // 调用服务层方法执行分页查询
            IPage<HrEmpHcmArchive> result = hrEmpHcmArchiveService.pageByCondition(page, queryEntity);

            // 返回查询结果
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            // 记录异常日志
            log.error("分页查询员工历史档案数据异常", e);
            // 返回错误信息
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    /**
     * 条件查询（不分页）
     *
     * <p>根据传入的实体对象，动态拼装非空字段的查询条件，返回所有匹配数据。
     * 如果查询实体为空，则查询所有数据。</p>
     *
     * <p>支持的查询条件与 pageHrEmpHcmArchive 方法相同。</p>
     *
     * @param queryEntity 查询条件实体对象，非空字段作为查询条件
     * @return 匹配的员工历史档案列表
     */
    @PostMapping("/query")
    @Operation(summary = "查询员工历史档案数据（不分页）")
    @AuditLog(value = "查询员工历史档案数据", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "queryEntity", description = "查询条件实体对象", required = false)
    public ResponseResult<List<HrEmpHcmArchive>> queryHrEmpHcmArchive(
            @Validated @RequestBody(required = false) HrEmpHcmArchive queryEntity) {

        // 如果查询实体为空，创建默认实体（查询所有数据）
        queryEntity = (queryEntity == null) ? new HrEmpHcmArchive() : queryEntity;

        try {
            // 调用服务层方法执行查询
            List<HrEmpHcmArchive> result = hrEmpHcmArchiveService.queryByCondition(queryEntity);

            // 返回查询结果
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            // 记录异常日志
            log.error("查询员工历史档案数据异常", e);
            // 返回错误信息
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }

    /**
     * 根据档案号精确查询
     *
     * <p>通过档案号（DA1，对应 da1 字段）精确查询单条员工历史档案数据。
     * 如果档案号不存在，返回 404 状态码。</p>
     *
     * @param da1 员工档案号，必填参数
     * @return 员工历史档案信息
     */
    @GetMapping("/get")
    @Operation(summary = "根据档案号查询员工历史档案")
    @AuditLog(value = "根据档案号查询员工历史档案", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "da1", description = "员工档案号（DA1）", required = true, example = "28035")
    public ResponseResult<HrEmpHcmArchive> getHrEmpHcmArchive(@RequestParam("da1") String da1) {
        try {
            // 调用服务层方法执行查询
            HrEmpHcmArchive result = hrEmpHcmArchiveService.getByDa1(da1);

            // 判断查询结果
            if (result == null) {
                // 如果未找到数据，返回 404 状态码
                return new ResponseResult<>(HttpStatus.NOT_FOUND.value(), "未找到该档案号的员工信息");
            }

            // 返回查询结果
            return new ResponseResult<>(HttpStatus.OK.value(), "查询成功", result);
        } catch (Exception e) {
            // 记录异常日志
            log.error("根据档案号查询员工历史档案异常", e);
            // 返回错误信息
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "查询异常: " + e.getMessage());
        }
    }
}
