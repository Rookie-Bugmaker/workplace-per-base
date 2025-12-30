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
        // TODO: 已修改，有重复，已导出重复表格
        myBatisPlusMigrationService.migrateAppointmentEntity(HrEmpAppointment.class);
        // 暂无数据
        myBatisPlusMigrationService.migrateEntity(HrEmpBank.class);
        // TODO: 只有一个工号重复了：013252080202
        myBatisPlusMigrationService.migrateBasicEntity(HrEmpBasic.class);
        // 已修改，无问题
        myBatisPlusMigrationService.migrateCareerPathEntity(HrEmpCareerPath.class);
        // TODO: 013263021159 013252080202 重复身份证号
        myBatisPlusMigrationService.migrateEntity(HrEmpCertificate.class);
        // 无重复
        myBatisPlusMigrationService.migrateEntity(HrEmpCommunication.class);
        // 随机pick一条
        myBatisPlusMigrationService.migrateEntity(HrEmpFamMember.class);
        // 无重复
        myBatisPlusMigrationService.migrateHealthEntity(HrEmpHealth.class);
        // TODO: 只有一个人重复了：013252080202
        myBatisPlusMigrationService.migrateEntity(HrEmpHouseHold.class);
        // TODO: 已修改，有重复，已导出重复表格
        myBatisPlusMigrationService.migrateOtherAppointmentEntity(HrEmpOtherAppointment.class);
        // 无旧数据
        myBatisPlusMigrationService.migrateEntity(HrEmpPenalties.class);
        // 无问题
        myBatisPlusMigrationService.migrateProQuaEntity(HrEmpProfessionalQualification.class);
        // 暂无数据
        myBatisPlusMigrationService.migrateEntity(HrEmpProfessionalSkill.class);
        // TODO: 有重复！已导出表格
        myBatisPlusMigrationService.migrateResumeEntity(HrEmpResume.class);
        // TODO: tsda有用吗？这个怎么推
        myBatisPlusMigrationService.migrateEntity(HrEmpRewards.class);
        // 虽有重复，但是salary_grade与salary_level相同，推哪一条都可以
        myBatisPlusMigrationService.migrateSalaryStandardEntity(HrEmpSalaryStandard.class);
        // 虽有重复，但是skill_level, job_id, get_method等相同，推哪一条都可以
        myBatisPlusMigrationService.migrateSkillLevelEntity(HrEmpSkillLevel.class);
        // 均有create_time，无问题
        myBatisPlusMigrationService.migrateEntity(HrEmpSalaryInfo.class);


        //基础数据表全量同步 无逻辑
        myBatisPlusMigrationService.migrateInfoTable(HrOrgCostCenter.class);
        myBatisPlusMigrationService.migrateInfoTable(PostCatalogInfo.class);


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

