package com.crrc.controller;


import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.ExceptionCasePerson;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.ExceptionCasePersonPlusVO;
import com.crrc.service.impl.ExceptionCasePersonServiceImpl;
import com.crrc.utils.WebServiceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 额外人员信息表 前端控制器
 * </p>
 *
 * @author mayiyao
 * @since 2026-01-15 13:44:29
 */
@RestController
@RequestMapping("/exceptionCasePerson")
@RefreshScope
@Tag(name = "额外人员信息表", description = "额外人员信息表")
public class ExceptionCasePersonController {

    @Autowired
    private ExceptionCasePersonServiceImpl exceptionCasePersonService;
    @Autowired
    private WebServiceUtil webServiceUtil;


    /**
     * 处理职能标识
     * @param userType 职能标识编码
     * @return 转换后的职能标识汉字
     */
    private String resolveUserType(String userType) {
        // todo 如果后面换了逻辑，记得处理这里
        switch (userType) {
            case "EXT_SEC":
                userType = "外派股东";
                break;
            case "INT_LOAN":
                userType = "借用";
                break;
            default:
                userType = "未知类型";
        }
        return userType;
    }

    @PostMapping("/queryForKingdie")
    @Operation(summary = "金蝶webservice ,获取额外人员信息表列表")
    @AuditLog(value = "获取额外人员信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult queryForKingdie() {
        List<ExceptionCasePersonPlusVO> effectiveList = exceptionCasePersonService.selectEffectivePerson();

        // 组装XML报文
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<USERS>\n");

        for (ExceptionCasePersonPlusVO person : effectiveList) {
            xmlBuilder.append("    <USER>\n")
                    .append("        <USER_CODE>").append(person.getUserCode()).append("</USER_CODE>\n")
                    .append("        <NAME>").append(person.getName()).append("</NAME>\n")
                    .append("        <ORG>").append(person.getNewCompanyCode()).append("</ORG>\n")
                    .append("        <DEPT>").append(person.getNewDeptCode()).append("</DEPT>\n")
                    // EXT_SEC→外部派入，INT_LOAN→借用，其它为未知类型 todo 如果后面换了逻辑，记得处理这里
                    .append("        <DUTYPE>").append(resolveUserType(person.getUserType())).append("</DUTYPE>\n")  // todo 可能要换成中文的职能标识
                    .append("    </USER>\n");
        }

        xmlBuilder.append("</USERS>");

        // 发送WebService请求
        String messageCode = "013200HRC000003###"; // todo 需要修改为真实消息号
        String xmlMsg = webServiceUtil.sendByWebService(xmlBuilder.toString(), messageCode);

        // 处理返回结果
        String result = webServiceUtil.xmlTransformer(xmlMsg);
        JSONObject jsonObject = JSONUtil.parseObj(result);

        if("S".equals(jsonObject.get("M_TYPE"))) {
            return new ResponseResult<>(HttpStatus.OK.value(), DateTime.now() + "金蝶接收成功", jsonObject);
        } else {
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), DateTime.now() + "金蝶接收失败", jsonObject);
        }
    }

    @PostMapping("/queryForSap")
    @Operation(summary = "SAP webservice, 获取额外人员信息表列表")
    @AuditLog(value = "获取额外人员信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    public ResponseResult queryForSap() {
        List<ExceptionCasePersonPlusVO> effectiveList = exceptionCasePersonService.selectEffectivePerson();

        // 组装XML报文
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<Objects>\n");

        for (ExceptionCasePersonPlusVO person : effectiveList) {
            xmlBuilder.append("    <item>\n")
                    .append("        <USERID>").append(person.getUserCode()).append("</USERID>\n")
                    .append("        <NAME>").append(person.getName()).append("</NAME>\n")
                    .append("        <COMPANY>").append(person.getNewCompanyCode()).append("</COMPANY>\n")
                    .append("        <DEPARTMENT>").append(person.getNewDeptCode()).append("</DEPARTMENT>\n")
                    .append("        <DEPARTNAME>").append(person.getNewDept()).append("</DEPARTNAME>\n")
                    .append("        <GENDER>").append("1").append("</GENDER>\n") // todo 目前没有收集性别信息，默认全传1
                    // EXT_SEC→外部派入，INT_LOAN→借用，其它为未知类型 todo 如果后面换了逻辑，记得处理这里
                    .append("        <CLASS>").append(person.getUserType()).append("</CLASS>\n")
                    .append("    </item>\n");
        }

        xmlBuilder.append("</Objects>");

        // 发送WebService请求
        String messageCode = "013200HRC000004###"; // todo SAP专用消息号，需修改为真实消息号
        String xmlMsg = webServiceUtil.sendByWebService(xmlBuilder.toString(), messageCode);

        // 处理返回结果
        String result = webServiceUtil.xmlTransformer(xmlMsg);
        JSONObject jsonObject = JSONUtil.parseObj(result);

        if("S".equals(jsonObject.get("M_TYPE"))) {
            return new ResponseResult<>(HttpStatus.OK.value(), DateTime.now() + "SAP接收成功", jsonObject);
        } else {
            return new ResponseResult<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), DateTime.now() + "SAP接收失败", jsonObject);
        }
    }

    @PostMapping("/page")
    @Operation(summary = "获取额外人员信息表列表 page")
    @AuditLog(value = "获取额外人员信息表列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageExceptionCasePerson(@Validated @RequestBody(required = false) ExceptionCasePerson pageReqVO,
                                             @RequestParam(required = false, defaultValue = "1") int pageNo,
                                             @RequestParam(required = false, defaultValue = "10")int pageSize) {
        pageReqVO = (pageReqVO == null) ? new ExceptionCasePerson() : pageReqVO;
        Page<ExceptionCasePerson> page = new Page<>(pageNo, pageSize);
        exceptionCasePersonService.query(page, pageReqVO);
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息表列表 page成功", page);
    }

    @PostMapping("/query")
    @Operation(summary = "获取额外人员信息表列表")
    @AuditLog(value = "获取额外人员信息表列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<ExceptionCasePerson>> queryExceptionCasePerson(@Validated @RequestBody(required = false) ExceptionCasePerson queryReqVO) {
        queryReqVO = (queryReqVO == null) ? new ExceptionCasePerson() : queryReqVO;
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息表列表成功", exceptionCasePersonService.query(queryReqVO));
	}

    @GetMapping("/get")
    @Operation(summary = "获得额外人员信息表实体")
    @AuditLog(value = "获得额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<ExceptionCasePerson> getExceptionCasePerson(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取额外人员信息表实体成功", exceptionCasePersonService.getById(id));
    }

    @Transactional
    @PostMapping("/create")
    @Operation(summary = "创建额外人员信息表实体")
    @AuditLog(value = "创建额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createExceptionCasePerson(@Valid @RequestBody ExceptionCasePerson createReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "创建额外人员信息表实体成功", exceptionCasePersonService.save(createReqVO));
    }

    @Transactional
    @PostMapping("/update")
    @Operation(summary = "更新额外人员信息表实体")
    @AuditLog(value = "更新额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateExceptionCasePerson(@Valid @RequestBody ExceptionCasePerson updateReqVO) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新额外人员信息表实体成功", exceptionCasePersonService.saveOrUpdate(updateReqVO));
    }

    @PostMapping("/batchUpdate")
    @Operation(summary = "批量更新额外人员信息")
    @AuditLog(value = "批量更新额外人员信息", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateExceptionCasePerson(@Valid @RequestBody List<ExceptionCasePerson> updateReqVOList) {
        return new ResponseResult<>(HttpStatus.OK.value(), "更新额外人员信息表实体成功", exceptionCasePersonService.updateBatchById(updateReqVOList));
    }

    @Transactional
    @GetMapping("/delete")
    @Operation(summary = "删除额外人员信息表实体")
    @AuditLog(value = "删除额外人员信息表实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteExceptionCasePerson(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除额外人员信息表实体成功", exceptionCasePersonService.removeById(id));
    }
    
    @PostMapping("/export")
    @AuditLog(value = "导出额外人员信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出额外人员信息表报表")
    public void exportExceptionCasePerson(@Validated @RequestBody(required = false) ExceptionCasePerson exportReqVO,
                              @RequestParam(required = false, defaultValue = "xlsx") String type,
                           HttpServletResponse response) throws IOException {
        exceptionCasePersonService.exports(exportReqVO, response);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入额外人员信息表报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入额外人员信息表报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
    	// imports方法会导入保存数据到数据库，reads方法会从文件读取数据但不保存
        List<ExceptionCasePerson> importReqVO = exceptionCasePersonService.imports(file);
        return new ResponseResult<>(HttpStatus.OK.value(), "导入额外人员信息表报表成功", true);
    }

}
