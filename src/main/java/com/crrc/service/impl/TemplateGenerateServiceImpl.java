package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crrc.annotation.TenantIgnore;
import com.crrc.entity.BorrowedPersonnelApprovalForm;
import com.crrc.entity.BorrowedPersonnelApprovalFormRelation;
import com.crrc.entity.PersonnelSecondmentRequest;
import com.crrc.entity.PersonnelSecondmentRequestRelation;
import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.mapper.BorrowedPersonnelApprovalFormMapper;
import com.crrc.mapper.BorrowedPersonnelApprovalFormRelationMapper;
import com.crrc.mapper.PersonnelSecondmentRequestMapper;
import com.crrc.mapper.PersonnelSecondmentRequestRelationMapper;
import com.crrc.pojo.vo.BorrowedPersonnelApprovalFormRelationTemplateVO;
import com.crrc.pojo.vo.BorrowedPersonnelApprovalFormTemplateVO;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateGenerateServiceImpl {
    @Value("${crrc.file.template.borrow.id}")
    private String templateFileId;
    @Value("${crrc.file.template.borrowing.id}")
    private String borrowingTemplateFileId;

    @Autowired
    PersonnelSecondmentRequestRelationMapper mapper;
    @Autowired
    PersonnelSecondmentRequestMapper requestMapper;
    @Autowired
    BorrowedPersonnelApprovalFormRelationMapper relationMapper;
    @Autowired
    BorrowedPersonnelApprovalFormMapper formMapper;
    @Autowired
    FileServiceImpl fileService;

    @Async("templateGenerateAsyncExecutor")
    @TenantIgnore
    public void generateRequestWordAndUpdate(String id, int tableNum, int headerNum) {
        String oldTenant = MultiTenantHandler.tenantId.get();
        try {
            MultiTenantHandler.tenantId.set("ignore");
            PersonnelSecondmentRequestRelation relation = mapper.selectById(id);
            String fileId = generateRequestWord(id, tableNum, headerNum);
            relation.setFileId(fileId);
            log.info("generateRequestWordAndUpdate生成的fileId: {}", fileId);
            mapper.updateById(relation);
        } catch (IOException e) {
            log.error("generateRequestWordAndUpdate出现错误", e);
        } finally {
            MultiTenantHandler.tenantId.set(oldTenant);
        }
    }

    @TenantIgnore
    public String generateRequestWord(String id, int tableNum, int headerNum) throws IOException {
        String oldTenant = MultiTenantHandler.tenantId.get();
        try {
            MultiTenantHandler.tenantId.set("ignore");
            PersonnelSecondmentRequestRelation relation = mapper.selectById(id);

            // 翻译部门
            String deptNameByCode = DeptUtils.getDeptNameByCode(relation.getRequestingDepartment());
            relation.setRequestingDepartment(deptNameByCode);

            PersonnelSecondmentRequestRelationTemplateVO relationVO = new PersonnelSecondmentRequestRelationTemplateVO();
            BeanUtils.copyProperties(relation, relationVO);
            // 需要单独把relation的翻译结果copy到relationVO中
            relationVO.setRequestingDepartment(DeptUtils.getDeptNameByCode(relationVO.getRequestingDepartment()));

            LocalDate date = LocalDate.now();

            relationVO.setDay(String.valueOf(date.getDayOfMonth()));
            relationVO.setMonth(String.valueOf(date.getMonth().getValue()));
            relationVO.setYear(String.valueOf(date.getYear()));

            LambdaQueryWrapper<PersonnelSecondmentRequest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PersonnelSecondmentRequest::getRelationId, id);
            List<PersonnelSecondmentRequest> requestList = requestMapper.selectList(wrapper);
            final int[] index = {1};
            List<PersonnelSecondmentRequestTemplateVO> requestVOList = requestList.stream().map(request -> {
                PersonnelSecondmentRequestTemplateVO vo = new PersonnelSecondmentRequestTemplateVO();
                BeanUtils.copyProperties(request, vo);
                vo.setSuggestedSourceDepartment(DeptUtils.getDeptNameByCode(vo.getSuggestedSourceDepartment()));
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
                vo.setStartEndDate(String.format("%s至%s", request.getStartDate().format(dateTimeFormatter), request.getEndDate().format(dateTimeFormatter)));
                vo.setIndex(index[0]);
                index[0] = index[0] + 1;
                return vo;
            }).collect(Collectors.toList());

            FileDataStream fileStream = fileService.getFileStream(templateFileId);
            String fileId;
            try {
                byte[] docx = WordTemplateProcessor.processTemplate(fileStream.getStream(), relationVO);
                byte[] bytes = WordTemplateProcessor.replaceTable(docx, requestVOList, tableNum, headerNum);
                MultipartFile multipartFile = WordTemplateProcessor.buildMultipartFile(bytes);
                fileId = fileService.upload(multipartFile);
            } catch (Exception e) {
                log.error("模板生成出现错误", e);
                throw new IOException(e);
            }

            return fileId;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        } finally {
            MultiTenantHandler.tenantId.set(oldTenant);
        }
    }

    @Async("templateGenerateAsyncExecutor")
    public void generateApprovalFormWordAndUpdate(String id, int tableNum, int headerNum) {
        String oldTenant = MultiTenantHandler.tenantId.get();
        try {
            MultiTenantHandler.tenantId.set("ignore");
            BorrowedPersonnelApprovalFormRelation relation = relationMapper.selectById(id);

            String fileId = generateApprovalFormWord(id, tableNum, headerNum);
            relation.setFileId(fileId);

            relationMapper.updateById(relation);
        } catch (IOException e) {
            log.error("generateRequestWordAndUpdate出现错误", e);
        } finally {
            if (oldTenant != null) {
                MultiTenantHandler.tenantId.set(oldTenant);
            } else {
                MultiTenantHandler.tenantId.remove();
            }
        }

    }

    @TenantIgnore
    public String generateApprovalFormWord(String id, int tableNum, int headerNum) throws IOException {
        String oldTenant = MultiTenantHandler.tenantId.get();
        try {
            MultiTenantHandler.tenantId.set("ignore");
            BorrowedPersonnelApprovalFormRelation relation = relationMapper.selectById(id);
            LocalDate date = LocalDate.now();

            BorrowedPersonnelApprovalFormRelationTemplateVO templateVO = new BorrowedPersonnelApprovalFormRelationTemplateVO();

            BeanUtils.copyProperties(relation, templateVO);

            templateVO.setDay(String.valueOf(date.getDayOfMonth()));
            templateVO.setMonth(String.valueOf(date.getMonth().getValue()));
            templateVO.setYear(String.valueOf(date.getYear()));

            templateVO.setStartDay(String.valueOf(relation.getStartDate().getDayOfMonth()));
            templateVO.setStartMonth(String.valueOf(relation.getStartDate().getMonth().getValue()));
            templateVO.setStartYear(String.valueOf(relation.getStartDate().getYear()));

            templateVO.setEndDay(String.valueOf(relation.getStartDate().getDayOfMonth()));
            templateVO.setEndMonth(String.valueOf(relation.getStartDate().getMonth().getValue()));
            templateVO.setEndYear(String.valueOf(relation.getStartDate().getYear()));


            LambdaQueryWrapper<BorrowedPersonnelApprovalForm> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BorrowedPersonnelApprovalForm::getRelationId, id);
            List<BorrowedPersonnelApprovalForm> requestList = formMapper.selectList(wrapper);

            final int[] index = {1};

            List<BorrowedPersonnelApprovalFormTemplateVO> requestVOList = requestList.stream().map(request -> {
                BorrowedPersonnelApprovalFormTemplateVO vo = new BorrowedPersonnelApprovalFormTemplateVO();
                BeanUtils.copyProperties(request, vo);
                vo.setIndex(index[0]);
                index[0] = index[0] + 1;
                return vo;
            }).collect(Collectors.toList());

            FileDataStream fileStream = fileService.getFileStream(borrowingTemplateFileId);
            String fileId;
            try {
                byte[] docx = WordTemplateProcessor.processTemplate(fileStream.getStream(), templateVO);
                byte[] bytes = WordTemplateProcessor.replaceTable(docx, requestVOList, tableNum, headerNum);
                MultipartFile multipartFile = WordTemplateProcessor.buildMultipartFile(bytes);
                fileId = fileService.upload(multipartFile);
            } catch (Exception e) {
                log.error("模板生成出现错误", e);
                throw new IllegalStateException("模板生成出现错误");
            }

            return fileId;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        } finally {
            MultiTenantHandler.tenantId.set(oldTenant);
        }
    }
}
