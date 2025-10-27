package com.crrc.controller;

import com.crrc.entity.*;
import com.crrc.service.MyBatisPlusMigrationService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/migrate")
public class MigrationController {

    @Autowired
    private MyBatisPlusMigrationService myBatisPlusMigrationService;

    @PostMapping("/employees")
    public String migrateEmployees() {
        myBatisPlusMigrationService.migrateEntity(HrEmpBasic.class);
        return "员工数据迁移任务已启动";
    }

    @PostMapping("/all")
    @XxlJob("migrateAll")
    public String migrateAll() {
        myBatisPlusMigrationService.migrateEntity(HrEmpAppointment.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpBank.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpBasic.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpCareerPath.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpCertificate.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpCommunication.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpFamMember.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpHealth.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpHouseHold.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpOtherAppointment.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpPenalties.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpProfessionalQualification.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpProfessionalSkill.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpResume.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpRewards.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpSalaryStandard.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpSkillLevel.class);
        myBatisPlusMigrationService.migrateEntity(HrEmpSalaryInfo.class);
        myBatisPlusMigrationService.migrateEntity(PostCatalogInfo.class);



        //基础数据表全量同步 无逻辑
        myBatisPlusMigrationService.migrateInfoTable(HrOrgCostCenter.class);


        //教育经历特殊处理
        myBatisPlusMigrationService.migrateEduTable();
        return "员工数据迁移任务已启动";
    }

    @PostMapping("/bank")
    public void migrateBank(){
//        int validatedCount = validateKeyService.validate(
//                bankService.list(),
//                HrEmpBank::getValidateKey,
//                bankService::saveOrUpdateBatch
//        );

        myBatisPlusMigrationService.migrateEntity(HrEmpBank.class);

//        int processedCount = validateKeyService.processCollection(
//                bankService.list(),
//                bankService::updateBatchById
//        );

//        log.info("数据推送完成 - 已验证更新{}条，重新处理{}条", validatedCount, processedCount);
    }

    @PostMapping("/edu")
    public void migrateEdu() {
        myBatisPlusMigrationService.migrateEduTable();
    }

    @PostMapping("/basic")
    public void migrateBasic() {
        myBatisPlusMigrationService.migrateEntity(HrEmpBasic.class);
    }


}

