package com.crrc.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.annotation.AuditLog;
import com.crrc.entity.HrCoreTalent;
import com.crrc.enums.LogType;
import com.crrc.enums.OperateType;
import com.crrc.pojo.ResponseResult;
import com.crrc.pojo.vo.MapVO;
import com.crrc.service.impl.CodeInfoServiceImpl;
import com.crrc.service.impl.HrCoreTalentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hrCoreTalent")
@RefreshScope
public class HrCoreTalentController {
    @Autowired
    private HrCoreTalentServiceImpl hrCoreTalentService;
    @Autowired
    private CodeInfoServiceImpl codeInfoService;

    private static final String TABLE_NAME = "hr_core_talent";

    private static final String SEQ_TYPE_NAME = "SEQUENCE_TYPE";

    private static final String TALENT_TYPE_NAME = "TALENT_LEVEL";

    private static final String APPOINTMENT_STATUS_TYPE_NAME = "APPOINTMENT_STATUS";

    @GetMapping("/getSeqDict")
    public ResponseResult<List<MapVO<String, String>>> getSeqDict() {
        try {
            List<MapVO<String, String>> result = codeInfoService.getTranslateMap(TABLE_NAME, SEQ_TYPE_NAME).entrySet()
                    .stream().map(e -> new MapVO<>(e.getKey(), e.getValue())).collect(Collectors.toList());
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", result);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/getAppointmentDict")
    public ResponseResult<List<MapVO<String, String>>> getAppointmentDict() {
        try {
            List<MapVO<String, String>> result = codeInfoService.getTranslateMap(TABLE_NAME, APPOINTMENT_STATUS_TYPE_NAME)
                    .entrySet().stream().map(e -> new MapVO<>(e.getKey(), e.getValue())).collect(Collectors.toList());
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", result);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @GetMapping("/getTalentDict")
    public ResponseResult<List<MapVO<String, String>>> getTalentDict(@RequestParam String seqCode) {
        try {
            List<MapVO<String, String>> map = codeInfoService.getTranslateMap(TABLE_NAME, TALENT_TYPE_NAME).entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(seqCode))
                    .map(e -> new MapVO<>(e.getKey(), e.getValue())).collect(Collectors.toList());
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", map);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @PostMapping("/page")
    @Operation(summary = "获取列表 page")
    @AuditLog(value = "获取列表 page", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO",description = "包含查询条件的实体对象", required = false)
    @Parameter(name = "pageNo", description = "页码", required = false, example = "1")
    @Parameter(name = "pageSize", description = "条数", required = false, example = "10")
    public ResponseResult<IPage> pageHrCoreTalent(@Validated @RequestBody(required = false) HrCoreTalent pageReqVO,
                                                  @RequestParam(required = false, defaultValue = "1") int pageNo,
                                                  @RequestParam(required = false, defaultValue = "10")int pageSize) {
        try {
            pageReqVO = (pageReqVO == null) ? new HrCoreTalent() : pageReqVO;
            Page<HrCoreTalent> page = new Page<>(pageNo, pageSize);
            hrCoreTalentService.queryVO(page, pageReqVO);
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表 page成功", page);
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @PostMapping("/query")
    @Operation(summary = "获取列表")
    @AuditLog(value = "获取列表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "pageReqVO", description = "包含查询条件的实体对象", required = false)
    public ResponseResult<List<HrCoreTalent>> queryHrCoreTalent(@Validated @RequestBody(required = false) HrCoreTalent queryReqVO) {
        try {
            queryReqVO = (queryReqVO == null) ? new HrCoreTalent() : queryReqVO;
            return new ResponseResult<>(HttpStatus.OK.value(), "获取列表成功", hrCoreTalentService.queryList(queryReqVO));
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }

    }

    @GetMapping("/get")
    @Operation(summary = "获得实体")
    @AuditLog(value = "获得实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_QUERY)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<HrCoreTalent> getHrCoreTalent(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "获取实体成功", hrCoreTalentService.getEntityById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/create")
    @Operation(summary = "创建实体")
    @AuditLog(value = "创建实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_ADD)
    @Parameter(name = "createReqVO", description = "需要创建的实体对象", required = true)
    public ResponseResult<Boolean> createHrCoreTalent(@Valid @RequestBody HrCoreTalent createReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "创建实体成功", hrCoreTalentService.saveVO(createReqVO));
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/update")
    @Operation(summary = "更新实体")
    @AuditLog(value = "更新实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_MODIFY)
    @Parameter(name = "createReqVO", description = "需要更新的实体对象", required = true)
    public ResponseResult<Boolean> updateHrCoreTalent(@Valid @RequestBody HrCoreTalent updateReqVO) {
        try {
            return new ResponseResult<>(HttpStatus.OK.value(), "更新实体成功", hrCoreTalentService.saveOrUpdateVO(updateReqVO));
        } catch (Exception e) {
            return new ResponseResult<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/delete")
    @Operation(summary = "删除实体")
    @AuditLog(value = "删除实体", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_DELETE)
    @Parameter(name = "id", description = "主键编号", required = true, example = "1")
    public ResponseResult<Boolean> deleteHrCoreTalent(@RequestParam("id") Long id) {
        return new ResponseResult<>(HttpStatus.OK.value(), "删除实体成功", hrCoreTalentService.removeById(id));
    }

    @PostMapping("/export")
    @AuditLog(value = "导出报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_EXPORT)
    @Operation(summary = "导出报表")
    public void exportHrCoreTalent(@Validated @RequestBody(required = false) HrCoreTalent exportReqVO,
                                   @RequestParam(required = false, defaultValue = "xlsx") String type,
                                   HttpServletResponse response) throws IOException {
        String contentType = "csv".equals(type) ?  "text/csv;charset=UTF-8" :
                ("xls".equals(type) ? "application/vnd.ms-excel" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("报表-" + new SimpleDateFormat("yyyyMMddhhmmss")
                        .format(System.currentTimeMillis()), "UTF-8")
                .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + "." +type);

        exportReqVO = (exportReqVO == null) ? new HrCoreTalent() : exportReqVO;
        List<HrCoreTalent> hrCoreTalentList = hrCoreTalentService.query(exportReqVO);
        ExcelTypeEnum typeEnum = "csv".equals(type) ?  ExcelTypeEnum.CSV : ("xls".equals(type) ? ExcelTypeEnum.XLS : ExcelTypeEnum.XLSX);
        EasyExcel.write(response.getOutputStream(), HrCoreTalent.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .autoCloseStream(true)
                .excelType(typeEnum)
                .sheet("报表")
                .doWrite(hrCoreTalentList);
    }

    @PostMapping("/import")
    @AuditLog(value = "导入报表", logType = LogType.TYPE_OPERATE, operateType = OperateType.TYPE_IMPORT)
    @Operation(summary = "导入报表")
    public ResponseResult<Boolean> imports(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), HrCoreTalent.class,
                        new CustomHrCoreTalentImportListener(hrCoreTalentService, codeInfoService))
                .registerConverter(new CustomDateConverter())
                .headRowNumber(1)
                .sheet()
                .doRead();
        return new ResponseResult<>(HttpStatus.OK.value(), "导入报表成功");
    }

    public static class CustomHrCoreTalentImportListener implements ReadListener<HrCoreTalent> {

        private final HrCoreTalentServiceImpl hrCoreTalentService;
        private final CodeInfoServiceImpl codeInfoService;
        private final List<HrCoreTalent> dataList;

        public CustomHrCoreTalentImportListener(HrCoreTalentServiceImpl hrCoreTalentService, CodeInfoServiceImpl codeInfoService) {
            this.hrCoreTalentService = hrCoreTalentService;
            this.codeInfoService = codeInfoService;
            this.dataList = new ArrayList<>();
        }

        @Override
        public void onException(Exception exception, AnalysisContext context) throws Exception {
            throw exception;
        }

        @Override
        public void invoke(HrCoreTalent hrCoreTalent, AnalysisContext analysisContext) {
            dataList.add(hrCoreTalent);

            if(dataList.size() > 800) {
                flush();
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
            flush();
        }

        private void flush() {
            // 获得value -> 码值的Map
            Map<String, String> talentValue2Code = codeInfoService.getTranslateMap(TABLE_NAME, TALENT_TYPE_NAME).entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey
                    ));

            Map<String, String> sequenceValue2Code = codeInfoService.getTranslateMap(TABLE_NAME, SEQ_TYPE_NAME).entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey
                    ));

            Map<String, String> appointmentValue2Code = codeInfoService.getTranslateMap(TABLE_NAME, APPOINTMENT_STATUS_TYPE_NAME).entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey
                    ));


            dataList.forEach(item -> {
                // 将value映射到码值
                if(!sequenceValue2Code.containsKey(item.getSequenceType())) {
                    throw new RuntimeException("所属序列翻译错误，请检查是否有无误，value：" + item.getTalentLevel());
                }

                if(!talentValue2Code.containsKey(item.getTalentLevel())) {
                    throw new RuntimeException("层级翻译错误，请检查是否有无误，value：" + item.getTalentLevel());
                }

                if(!appointmentValue2Code.containsKey(item.getAppointmentStatus())) {
                    throw new RuntimeException("层级翻译错误，请检查是否有无误，value：" + item.getAppointmentStatus());
                }

                item.setSequenceType(sequenceValue2Code.get(item.getSequenceType()));
                item.setTalentLevel(talentValue2Code.get(item.getTalentLevel()));
                item.setAppointmentStatus(appointmentValue2Code.get(item.getAppointmentStatus()));
            });
            hrCoreTalentService.saveBatch(dataList);

            dataList.clear();
        }
    }

    public static class CustomDateConverter implements Converter<Date> {
        @Override
        public Class<Date> supportJavaTypeKey() {
            return Date.class;
        }

        @Override
        public CellDataTypeEnum supportExcelTypeKey() {
            return CellDataTypeEnum.STRING;
        }

        @Override
        public Date convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                      GlobalConfiguration globalConfiguration) {

            // 处理字符串型日期
            String dateStr = cellData.getStringValue();
            SimpleDateFormat dateFormatForChinese = new SimpleDateFormat("yyyy年MM月dd日");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                return dateFormatForChinese.parse(dateStr);
            } catch (Exception ignored) {
                // 尝试下一种格式
            }

            try {
                return dateFormat.parse(dateStr);
            } catch (Exception ignored) {
                // 尝试下一种格式
            }

            throw new IllegalArgumentException("不支持的日期格式: " + dateStr);
        }
    }
}
