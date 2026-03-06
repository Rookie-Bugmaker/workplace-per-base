package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.annotation.TenantIgnore;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.mapper.PersonnelSecondmentRequestMapper;
import com.crrc.mapper.PersonnelSecondmentRequestRelationMapper;
import com.crrc.pojo.vo.PersonnelSecondmentRequestRelationTemplateVO;
import com.crrc.pojo.vo.PersonnelSecondmentRequestTemplateVO;
import com.crrc.utils.DeptUtils;
import com.crrc.utils.WordTemplateProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateGenerateServiceImpl {
    @Value("${crrc.file.template.borrow.id}")
    private String templateFileId;

    @Autowired
    PersonnelSecondmentRequestRelationMapper mapper;
    @Autowired
    PersonnelSecondmentRequestMapper requestMapper;
    @Autowired
    FileServiceImpl fileService;

    @TenantIgnore
    @Async("templateGenerateAsyncExecutor")
    public void generateRequestWordAndUpdate(String id) {
        PersonnelSecondmentRequestRelation relation = mapper.selectById(id);
        try {
            String fileId = generateRequestWord(id);
            relation.setFileId(fileId);
        } catch (IOException e) {
            log.error("generateRequestWordAndUpdate出现错误", e);
        }

        mapper.updateById(relation);
    }

    public String generateRequestWord(@RequestParam String id) throws IOException {
        PersonnelSecondmentRequestRelation relation = mapper.selectById(id);

        // 翻译部门
        String deptNameByCode = DeptUtils.getDeptNameByCode(relation.getRequestingDepartment());
        relation.setRequestingDepartment(deptNameByCode);

        PersonnelSecondmentRequestRelationTemplateVO relationVO = new PersonnelSecondmentRequestRelationTemplateVO();
        BeanUtils.copyProperties(relation, relationVO);
        // 需要单独把relation的翻译结果copy到relationVO中
        relationVO.setRequestingDepartment(relation.getRequestingDepartmentValue());

        LocalDate date = LocalDate.now();

        relationVO.setDay(String.valueOf(date.getDayOfMonth()));
        relationVO.setMonth(String.valueOf(date.getMonth().getValue()));
        relationVO.setYear(String.valueOf(date.getYear()));

        LambdaQueryWrapper<PersonnelSecondmentRequest> wrapper = new LambdaQueryWrapper<>();
        List<PersonnelSecondmentRequest> requestList = requestMapper.selectList(wrapper);
        final int[] index = {1};
        List<PersonnelSecondmentRequestTemplateVO> requestVOList = requestList.stream().map(request -> {
            PersonnelSecondmentRequestTemplateVO vo = new PersonnelSecondmentRequestTemplateVO();
            BeanUtils.copyProperties(request, vo);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            vo.setStartEndDate(String.format("%s至%s", request.getStartDate().format(dateTimeFormatter), request.getEndDate().format(dateTimeFormatter)));
            vo.setIndex(index[0]);
            index[0] = index[0] + 1;
            return vo;
        }).collect(Collectors.toList());

        FileDataStream fileStream = fileService.getFileStream(templateFileId);
        String fileId;
        try {
            MultipartFile docx = WordTemplateProcessor.processTemplate(fileStream.getStream(), relationVO, requestVOList);
            fileId = fileService.upload(docx);
        } catch (Exception e) {
            log.error("模板生成出现错误", e);
            throw new IOException(e);
        }

        return fileId;
    }
}
