package com.crrc.service.impl;

import com.crrc.annotation.TenantIgnore;
import com.crrc.api.WkDataAPi;
import com.crrc.entity.HrEmpEducation;
import com.crrc.entity.HrEmpFamMember;
import com.crrc.entity.HrEmpMajorInfo;
import com.crrc.entity.HrEmpPenalties;
import com.crrc.entity.HrEmpProfessionalQualification;
import com.crrc.entity.HrEmpResume;
import com.crrc.entity.HrEmpRewards;
import com.crrc.entity.HrEmpSchoolInfo;
import com.crrc.entity.filedata.vo.FileDataStream;
import com.crrc.handler.MultiTenantHandler;
import com.crrc.pojo.dto.HrEmpBasicDTO;
import com.crrc.pojo.vo.HrEmpCadreAppointmentFormVO;
import com.crrc.service.CodeInfoService;
import com.crrc.utils.SecurityFrameworkUtils;
import com.crrc.utils.WordTemplateProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 干部任免审批表生成服务。
 * 入口：传入一个或多个档案号(userCode)，逐个生成 docx，上传文件服务，返回 fileId。
 * 复用现有模板套打基建（WordTemplateProcessor + 文件服务模板）与各子表的翻译方法(getMsg/selectMsg)。
 */
@Service
@Slf4j
public class CadreAppointmentFormGenerateServiceImpl {

    /** 模板文件在文件服务中的 id（需在配置中心/filters 配置，上传模板后回填） */
    @Value("${crrc.file.template.cadre.id}")
    private String cadreTemplateFileId;

    /** 呈报单位（固定） */
    private static final String REPORT_UNIT = "中车长春轨道客车股份有限公司党委干部部";
    /** 任免理由默认值 */
    private static final String DEFAULT_APPOINT_REASON = "工作需要。";
    /** 健康状况默认值（取不到真实值时） */
    private static final String DEFAULT_HEALTH = "健康";
    /** 现任职务取 jobTitle 的人员分类4（与前端判断一致） */
    private static final Set<String> JOB_TITLE_CATEGORY4 =
            new HashSet<>(Arrays.asList("厂", "厂二", "二", "中", "干业", "人业"));
    /** 家庭成员展示行数（与模板预留行数一致） */
    private static final int FAMILY_ROWS = 7;
    /** 奖励导出的年限（近 N 年） */
    private static final int REWARD_RECENT_YEARS = 5;

    // 字典表/类型（与 HrEmpBasicServiceImpl 中常量保持一致）
    private static final String EDU_TABLE = "hr_emp_education";
    private static final String EDU_TYPE = "EDU_EDU";
    private static final String DEGREE_TYPE = "EDU_DEGREE";
    private static final String DEGREE_MAJOR_TYPE = "EDU_DEGREE_MAJOR";
    private static final String NATION_TABLE = "hr_emp_basic";
    private static final String NATION_TYPE = "BASIC_NATION";
    private static final String HEALTH_TABLE = "hr_emp_health";
    private static final String HEALTH_TYPE = "BASIC_HEALTH_STATUS";
    private static final String PRO_QUA_TABLE = "hr_emp_pro_qualification";
    private static final String PRO_QUA_TYPE = "QUA_ID";
    private static final String FAM_TABLE = "hr_emp_fam_member";
    private static final String FAM_RELATION_TYPE = "FAM_RELATION";

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyy.MM");
    private static final DateTimeFormatter YMD_CN = DateTimeFormatter.ofPattern("yyyy年M月d日");

    @Autowired
    private HrEmpBasicServiceImpl hrEmpBasicService;
    @Autowired
    private HrEmpEducationServiceImpl hrEmpEducationService;
    @Autowired
    private HrEmpSchoolInfoServiceImpl hrEmpSchoolInfoService;
    @Autowired
    private HrEmpMajorInfoServiceImpl hrEmpMajorInfoService;
    @Autowired
    private HrEmpProfessionalQualificationServiceImpl hrEmpProfessionalQualificationService;
    @Autowired
    private HrEmpResumeServiceImpl hrEmpResumeService;
    @Autowired
    private HrEmpRewardsServiceImpl hrEmpRewardsService;
    @Autowired
    private HrEmpPenaltiesServiceImpl hrEmpPenaltiesService;
    @Autowired
    private HrEmpFamMemberServiceImpl hrEmpFamMemberService;
    @Autowired
    private CodeInfoService codeInfoService;
    @Autowired
    private FileServiceImpl fileService;
    @Autowired
    private WkDataAPi wkDataAPi;

    /** 单人 docx 组装结果：文件名 + 字节（上传与打包共用） */
    private static class DocxResult {
        final String fileName;
        final byte[] bytes;
        DocxResult(String fileName, byte[] bytes) {
            this.fileName = fileName;
            this.bytes = bytes;
        }
    }

    /**
     * 批量生成。返回每个档案号对应的结果：{userCode, fileId} 或 {userCode, error}。
     */
    @TenantIgnore
    public List<Map<String, String>> generateForUserCodes(List<String> userCodes) {
        String oldTenant = MultiTenantHandler.tenantId.get();
        List<Map<String, String>> results = new ArrayList<>();
        try {
            MultiTenantHandler.tenantId.set("ignore");
            String formFiller = resolveCurrentUserName();
            for (String userCode : userCodes) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("userCode", userCode);
                try {
                    item.put("fileId", generateOne(userCode, formFiller));
                } catch (Exception e) {
                    log.error("生成干部任免审批表失败, userCode={}", userCode, e);
                    item.put("error", e.getMessage());
                }
                results.add(item);
            }
        } finally {
            if (oldTenant != null) {
                MultiTenantHandler.tenantId.set(oldTenant);
            } else {
                MultiTenantHandler.tenantId.remove();
            }
        }
        return results;
    }

    private DocxResult assembleDocx(String userCode, String formFiller) throws IOException {
        log.info("[干部任免审批表] 开始生成, userCode={}", userCode);

        // 1. 基本信息（含翻译）
        List<HrEmpBasicDTO> basicList = hrEmpBasicService.selectMsg(userCode);
        if (basicList == null || basicList.isEmpty()) {
            throw new IllegalArgumentException("未找到人员基本信息: " + userCode);
        }
        HrEmpBasicDTO basic = basicList.get(0);
        hrEmpBasicService.resolveFields(basic);

        // 教育记录只查一次，供“学历学位”单元格与“简历”教育经历共用
        List<HrEmpEducation> eduList = hrEmpEducationService.selectMsg(userCode);
        if (eduList == null) {
            eduList = Collections.emptyList();
        }
        log.info("[干部任免审批表] userCode={} 查询到教育记录 {} 条", userCode, eduList.size());

        HrEmpCadreAppointmentFormVO vo = new HrEmpCadreAppointmentFormVO();
        vo.setName(nz(basic.getName()));
        vo.setGender(genderText(basic.getGender()));
        vo.setBirthYm(ym(basic.getBirthDate()));
        vo.setAge(basic.getAge() == null ? "" : String.valueOf(basic.getAge()));
        vo.setNation(translate(basic.getNation(), NATION_TABLE, NATION_TYPE));
        vo.setNativePlace(normalizePlace(basic.getNativePlaceName()));
        vo.setBirthPlace(normalizePlace(basic.getBirthPlace()));
        vo.setPartyJoinYm(ym(basic.getPartyJoinDate()));
        vo.setWorkYm(ym(basic.getWorkDate()));
        vo.setHealth(healthText(basic.getHealthStatus()));
        vo.setProfessionalTitle(resolveProfessionalTitle(userCode));
        vo.setCurrentPosition(resolveCurrentPosition(basic));
        vo.setAssessment(buildAssessment());
        vo.setAppointmentReason(DEFAULT_APPOINT_REASON);
        vo.setReportUnit(REPORT_UNIT);
        vo.setReportDate(LocalDate.now().format(YMD_CN));
        vo.setFormFiller(nz(formFiller));

        // 2. 学历学位（全日制/在职）
        fillEducation(eduList, vo);

        // 3. 家庭成员（固定 7 行占位符）
        Map<String, String> familyMap = buildFamilyMap(userCode);

        // 4. 多行字段：简历(教育经历 + 公司任职) / 奖惩
        String resume = buildResume(userCode, eduList);
        String rewardsPenalties = buildRewardsAndPenalties(userCode);

        // 5. 照片
        byte[] photo = loadPhoto(basic.getProfilePicture());

        // 6. 渲染
        byte[] bytes = render(vo, familyMap, resume, rewardsPenalties, photo);

        String fileName = "干部任免审批表_"
                + (StringUtils.isBlank(vo.getName()) ? userCode : vo.getName()) + ".docx";
        return new DocxResult(fileName, bytes);
    }

    /** 生成单人 docx 并上传文件服务，返回 fileId（行为与原方法一致） */
    private String generateOne(String userCode, String formFiller) throws IOException {
        DocxResult d = assembleDocx(userCode, formFiller);
        MultipartFile mf = WordTemplateProcessor.buildMultipartFile(d.bytes, d.fileName);
        try {
            return fileService.upload(mf);
        } catch (Exception e) {
            throw new IOException("上传干部任免审批表失败", e);
        }
    }

    /**
     * 批量生成并打包为 zip，直接返回 zip 字节（多选时直接下载，无需逐个用 fileId 下载）。
     * 单个失败不影响整体：失败项写入“生成失败_档案号.txt”一并打入压缩包。
     */
    @TenantIgnore
    public byte[] generateZipForUserCodes(List<String> userCodes) throws IOException {
        String oldTenant = MultiTenantHandler.tenantId.get();
        try {
            MultiTenantHandler.tenantId.set("ignore");
            String formFiller = resolveCurrentUserName();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Set<String> used = new HashSet<>();
            // 中文文件名用 UTF-8；若旧版 Windows 解压乱码，可改用 new ZipOutputStream(bos, java.nio.charset.Charset.forName("GBK"))
            try (ZipOutputStream zip = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
                for (String userCode : userCodes) {
                    try {
                        DocxResult d = assembleDocx(userCode, formFiller);
                        zip.putNextEntry(new ZipEntry(uniqueName(used, d.fileName)));
                        zip.write(d.bytes);
                        zip.closeEntry();
                    } catch (Exception e) {
                        log.error("生成干部任免审批表失败, userCode={}", userCode, e);
                        zip.putNextEntry(new ZipEntry(uniqueName(used, "生成失败_" + userCode + ".txt")));
                        zip.write(("生成失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
                        zip.closeEntry();
                    }
                }
            }
            return bos.toByteArray();
        } finally {
            if (oldTenant != null) {
                MultiTenantHandler.tenantId.set(oldTenant);
            } else {
                MultiTenantHandler.tenantId.remove();
            }
        }
    }

    /** zip 内文件名去重：重名追加 (2)/(3)... */
    private String uniqueName(Set<String> used, String name) {
        if (used.add(name)) {
            return name;
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        int i = 1;
        String candidate;
        do {
            candidate = base + "(" + (++i) + ")" + ext;
        } while (!used.add(candidate));
        return candidate;
    }

    private byte[] render(HrEmpCadreAppointmentFormVO vo, Map<String, String> familyMap,
                          String resume, String rewardsPenalties, byte[] photo) throws IOException {
        FileDataStream fileStream = fileService.getFileStream(cadreTemplateFileId);
        try (XWPFDocument doc = new XWPFDocument(fileStream.getStream());
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // 先处理多行与照片（占位符此时仍在）
            WordTemplateProcessor.fillMultilineAutoFit(doc, "${resume}", resume);
            WordTemplateProcessor.fillMultiline(doc, "${rewards}", rewardsPenalties);
            WordTemplateProcessor.fillPhoto(doc, "${photo}", photo);
            // 再处理标量 + 家庭成员
            Map<String, String> map = WordTemplateProcessor.extractRequireData(vo);
            map.putAll(familyMap);
            WordTemplateProcessor.replacePlaceholders(doc, map);
            doc.write(bos);
            return bos.toByteArray();
        }
    }

    // ---------------- 各字段构建 ----------------

    private String resolveProfessionalTitle(String userCode) {
        List<HrEmpProfessionalQualification> list = hrEmpProfessionalQualificationService.selectMsg(userCode);
        if (list == null || list.isEmpty()) {
            return "";
        }
        HrEmpProfessionalQualification best = list.stream()
                .filter(q -> "1".equals(q.getIsHighest()))
                .findFirst()
                .orElseGet(() -> list.stream()
                        .filter(q -> q.getQuaDate() != null)
                        .max(Comparator.comparing(HrEmpProfessionalQualification::getQuaDate))
                        .orElse(list.get(0)));
        return translate(best.getQuaId(), PRO_QUA_TABLE, PRO_QUA_TYPE);
    }

    private String resolveCurrentPosition(HrEmpBasicDTO basic) {
        String category4 = basic.getCategory4();
        if (category4 != null && JOB_TITLE_CATEGORY4.contains(category4.trim())) {
            return nz(basic.getJobTitle());
        }
        // 否则按 getMsg 逻辑：resolveFields 已将 position 翻译为名称
        return nz(basic.getPosition());
    }

    /** 学历学位单元格：全日制(isFullTime=1)/在职(=0) */
    private void fillEducation(List<HrEmpEducation> list, HrEmpCadreAppointmentFormVO vo) {
        log.info("[学历学位] 记录数={}", list.size());
        for (HrEmpEducation e : list) {
            log.info("[学历学位] record isFullTime={}, isHighestEducation={}, education={}({}), degree={}({}), degreeMajor={}, college={}, major={}, schoolCode={}, schoolName={}, grad={}",
                    e.getIsFullTime(), e.getIsHighestEducation(),
                    e.getEducation(), translate(e.getEducation(), EDU_TABLE, EDU_TYPE),
                    e.getDegree(), translate(e.getDegree(), EDU_TABLE, DEGREE_TYPE),
                    e.getDegreeMajor(), e.getCollege(), e.getMajor(),
                    e.getSchoolCode(), e.getSchoolName(), e.getGraduationDate());
        }
        HrEmpEducation full = pickEducation(list, true);
        HrEmpEducation part = pickEducation(list, false);
        log.info("[学历学位] 命中全日制记录={}, 在职记录={}（若都为“无”，请核对 isFullTime 是否确为 1/0）",
                full == null ? "无" : ("education=" + full.getEducation()),
                part == null ? "无" : ("education=" + part.getEducation()));
        if (full != null) {
            vo.setFulltimeEducation(translate(full.getEducation(), EDU_TABLE, EDU_TYPE));
            vo.setFulltimeDegree(join(translate(full.getDegreeMajor(), EDU_TABLE, DEGREE_MAJOR_TYPE), translate(full.getDegree(), EDU_TABLE, DEGREE_TYPE)));
            vo.setFulltimeSchool(join(resolveSchoolName(full), full.getCollege()));
            vo.setFulltimeMajor(majorText(resolveMajor(full)));
        }
        if (part != null) {
            vo.setParttimeEducation(translate(part.getEducation(), EDU_TABLE, EDU_TYPE));
            vo.setParttimeDegree(join(translate(part.getDegreeMajor(), EDU_TABLE, DEGREE_MAJOR_TYPE), translate(part.getDegree(), EDU_TABLE, DEGREE_TYPE)));
            vo.setParttimeSchool(join(resolveSchoolName(part), part.getCollege()));
            vo.setParttimeMajor(majorText(resolveMajor(part)));
        }
    }

    /** fullTime=true 取全日制(isFullTime=00010)，否则非全日制(=00020)；组内优先最高学历，其次最近毕业 */
    private HrEmpEducation pickEducation(List<HrEmpEducation> list, boolean fullTime) {
        String flag = fullTime ? "00010" : "00020";
        List<HrEmpEducation> group = list.stream()
                .filter(e -> flag.equals(e.getIsFullTime() == null ? "" : e.getIsFullTime().trim()))
                .collect(Collectors.toList());
        if (group.isEmpty()) {
            return null;
        }
        return group.stream()
                .filter(e -> "1".equals(e.getIsHighestEducation()))
                .findFirst()
                .orElseGet(() -> group.stream()
                        .filter(e -> e.getGraduationDate() != null)
                        .max(Comparator.comparing(HrEmpEducation::getGraduationDate))
                        .orElse(group.get(0)));
    }

    /** 简历 = 教育经历（本科[+研究生]）+ 公司任职经历 */
    private String buildResume(String userCode, List<HrEmpEducation> eduList) {
        List<String> lines = new ArrayList<>();
        lines.addAll(buildEducationResumeLines(eduList));
        lines.addAll(buildWorkResumeLines(userCode));
        String result = String.join("\n", lines);
        log.info("[简历] userCode={} 共 {} 行（含教育经历）:\n{}", userCode, lines.size(), result);
        return result;
    }

    /**
     * 简历开头的教育经历：
     * 最高学历为研究生(及以上) -> 插入“本科 + 研究生”两条；
     * 最高学历为本科 -> 仅插入“本科”一条；
     * 其它 -> 插入最高一条。
     * 按入学时间升序排列。
     */
    private List<String> buildEducationResumeLines(List<HrEmpEducation> list) {
        if (list == null || list.isEmpty()) {
            log.info("[简历-教育] 无教育记录，简历不插入教育经历");
            return Collections.emptyList();
        }
        for (HrEmpEducation e : list) {
            String n = translate(e.getEducation(), EDU_TABLE, EDU_TYPE);
            log.info("[简历-教育] 候选 education={}({}) rank={}, entry={}, grad={}, school={}, major={}",
                    e.getEducation(), n, eduRank(n), e.getSchoolEntryDate(), e.getGraduationDate(),
                    resolveSchoolName(e), e.getMajor());
        }
        HrEmpEducation highest = pickHighest(list);
        int hr = (highest == null) ? 0 : eduRank(translate(highest.getEducation(), EDU_TABLE, EDU_TYPE));

        List<HrEmpEducation> chosen = new ArrayList<>();
        if (hr >= 4) {
            // 研究生(硕士/博士)：本科 + 研究生两条
            HrEmpEducation undergrad = pickByRank(list, 3);
            if (undergrad != null) {
                chosen.add(undergrad);
            }
            chosen.add(highest);
        } else if (highest != null) {
            // 本科及以下：仅最高一条
            chosen.add(highest);
        }
        log.info("[简历-教育] 最高学历rank={}, 选中{}条", hr, chosen.size());

        List<HrEmpEducation> ordered = new ArrayList<>(new LinkedHashSet<>(chosen));
        ordered.sort(Comparator.comparing(HrEmpEducation::getSchoolEntryDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        List<String> lines = new ArrayList<>();
        for (HrEmpEducation e : ordered) {
            lines.add(formatEducationResumeLine(e));
        }
        log.info("[简历-教育] 最终插入 {} 条教育经历: {}", lines.size(), lines);
        return lines;
    }

    /** 取指定学历层次(按 eduRank)的记录，多个取最近毕业 */
    private HrEmpEducation pickByRank(List<HrEmpEducation> list, int rank) {
        return list.stream()
                .filter(e -> eduRank(translate(e.getEducation(), EDU_TABLE, EDU_TYPE)) == rank)
                .max(Comparator.comparing(HrEmpEducation::getGraduationDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private HrEmpEducation pickHighest(List<HrEmpEducation> list) {
        return list.stream()
                .filter(e -> "1".equals(e.getIsHighestEducation()))
                .findFirst()
                .orElseGet(() -> list.stream()
                        .max(Comparator.comparingInt(e -> eduRank(translate(e.getEducation(), EDU_TABLE, EDU_TYPE))))
                        .orElse(null));
    }

    private int eduRank(String name) {
        if (name == null) {
            return 0;
        }
        if (name.contains("博士")) {
            return 5;
        }
        if (name.contains("研究生") || name.contains("硕士")) {
            return 4;
        }
        if (name.contains("本科") || name.contains("大学")) {
            return 3;
        }
        if (name.contains("专科") || name.contains("大专")) {
            return 2;
        }
        return 1;
    }

    /** 教育经历单行：入学-毕业 学校学院专业[研究生]学习 */
    private String formatEducationResumeLine(HrEmpEducation e) {
        String period = ym(e.getSchoolEntryDate()) + "-" + ym(e.getGraduationDate());
        String eduName = translate(e.getEducation(), EDU_TABLE, EDU_TYPE);
        String suffix = (eduName.contains("研究生") ? "研究生" : "") + "学习";
        return (period + " " + join(resolveSchoolName(e), e.getCollege(), majorText(resolveMajor(e))) + suffix).trim();
    }

    /** 公司任职经历 */
    private List<String> buildWorkResumeLines(String userCode) {
        List<String> lines = new ArrayList<>();
        List<HrEmpResume> list = hrEmpResumeService.selectMsg(userCode);
        if (list == null || list.isEmpty()) {
            log.info("[简历-任职] userCode={} 无任职记录", userCode);
            return lines;
        }
        list.sort(Comparator.comparing(HrEmpResume::getStartDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        for (HrEmpResume r : list) {
            String start = ym(r.getStartDate());
            String end = (r.getEndDate() == null) ? "" : ym(r.getEndDate());
            lines.add((start + "-" + end + " " + join(r.getUnit(), r.getDepartment(), r.getPosition())).trim());
        }
        return lines;
    }

    /** 仿 HrEmpEducationController.getMsg：有 schoolCode 时按编码查学校名 */
    private String resolveSchoolName(HrEmpEducation e) {
        if (StringUtils.isNotBlank(e.getSchoolCode())) {
            HrEmpSchoolInfo info = hrEmpSchoolInfoService.selectByCode(e.getSchoolCode());
            if (info != null && StringUtils.isNotBlank(info.getSchoolName())) {
                return info.getSchoolName();
            }
        }
        return nz(e.getSchoolName());
    }

    private String majorText(String major) {
        if (StringUtils.isBlank(major)) {
            return "";
        }
        return major.endsWith("专业") ? major : major + "专业";
    }

    /** 仿 HrEmpEducationController.getMsg：大专及以上(教育码 001-004) 按 majorCode 反查专业名 */
    private String resolveMajor(HrEmpEducation e) {
        String edu = e.getEducation();
        boolean college = "001".equals(edu) || "002".equals(edu) || "003".equals(edu) || "004".equals(edu);
        if (!college || StringUtils.isBlank(e.getMajorCode())) {
            log.info("[专业] education={}, majorCode={} -> 不反查(非大专及以上或无专业编码)", edu, e.getMajorCode());
            return "";
        }
        HrEmpMajorInfo info = hrEmpMajorInfoService.selectByCode(e.getMajorCode());
        String major = (info == null || StringUtils.isBlank(info.getMajor())) ? e.getMajorCode() : info.getMajor();
        log.info("[专业] education={}, majorCode={} -> {}", edu, e.getMajorCode(), major);
        return major;
    }

    private Map<String, String> buildFamilyMap(String userCode) {
        Map<String, String> map = new LinkedHashMap<>();
        List<HrEmpFamMember> list = hrEmpFamMemberService.selectMsg(userCode);
        if (list == null) {
            list = new ArrayList<>();
        }
        log.info("[家庭成员] 记录数={}", list.size());
        for (HrEmpFamMember m : list) {
            hrEmpFamMemberService.resolvePoliticalStatus(m);
            m.setRelation(translate(m.getRelation(), FAM_TABLE, FAM_RELATION_TYPE));
            log.info("[家庭成员] relation={}, name={}, birth={}, political={}, unit={}, position={}",
                    m.getRelation(), m.getFamName(), m.getFamBirthDate(),
                    m.getFamPoliticalStatus(), m.getFamUnit(), m.getFamPosition());
        }
        list.sort(Comparator.comparingInt(m -> relationRank(m.getRelation())));
        for (int k = 1; k <= FAMILY_ROWS; k++) {
            HrEmpFamMember m = (k <= list.size()) ? list.get(k - 1) : null;
            map.put("${famRelation" + k + "}", m == null ? "" : nz(m.getRelation()));
            map.put("${famName" + k + "}", m == null ? "" : nz(m.getFamName()));
            map.put("${famAge" + k + "}", m == null ? "" : ym(m.getFamBirthDate()));
            map.put("${famPolitical" + k + "}", m == null ? "" : nz(m.getFamPoliticalStatus()));
            map.put("${famUnitPosition" + k + "}", m == null ? "" : join(m.getFamUnit(), m.getFamPosition()));
        }
        return map;
    }

    /** 排序：妻子/丈夫→儿女→父亲→母亲→其它 */
    private int relationRank(String relation) {
        if (relation == null) {
            return 99;
        }
        String r = relation.trim();
        if (r.contains("妻") || r.contains("夫") || r.contains("配偶")) {
            return 1;
        }
        if (r.contains("子") || r.contains("女")) {
            return 2;
        }
        if (r.contains("父")) {
            return 3;
        }
        if (r.contains("母")) {
            return 4;
        }
        return 50;
    }

    /** 奖励(近五年) + 惩处(全部)，合并到“奖惩情况” */
    private String buildRewardsAndPenalties(String userCode) {
        List<String> lines = new ArrayList<>();
        int fromYear = LocalDate.now().getYear() - REWARD_RECENT_YEARS;

        List<HrEmpRewards> rewards = hrEmpRewardsService.selectMsg(userCode);
        log.info("[奖惩-奖励] userCode={} 记录数={}, 仅保留 rewardDate 年份>={}",
                userCode, rewards == null ? 0 : rewards.size(), fromYear);
        if (rewards != null) {
            for (HrEmpRewards r : rewards) {
                log.info("[奖惩-奖励] rewardDate={}, rewardUnit={}, rewardName={}",
                        r.getRewardDate(), r.getRewardUnit(), r.getRewardName());
            }
            rewards.stream()
                    .filter(r -> r.getRewardDate() != null && r.getRewardDate().getYear() >= fromYear)
                    .sorted(Comparator.comparing(HrEmpRewards::getRewardDate))
                    .forEach(r -> {
                        String year = r.getRewardDate().getYear() + "年度";
                        lines.add(year + join(r.getRewardUnit(), r.getRewardName()) + "；");
                    });
        }

        List<HrEmpPenalties> penalties = hrEmpPenaltiesService.selectMsg(userCode);
        log.info("[奖惩-惩处] userCode={} 记录数={}", userCode, penalties == null ? 0 : penalties.size());
        if (penalties != null) {
            for (HrEmpPenalties p : penalties) {
                log.info("[奖惩-惩处] penaltyName={}, penaltyMeasures={}",
                        p.getPenaltyName(), p.getPenaltyMeasures());
                String s = join(p.getPenaltyName(), p.getPenaltyMeasures());
                if (StringUtils.isNotBlank(s)) {
                    lines.add(s + "；");
                }
            }
        }
        log.info("[奖惩] 最终 {} 条:\n{}", lines.size(), String.join("\n", lines));
        return String.join("\n", lines);
    }

    /** 年度考核：近三年年份，等级留空 */
    private String buildAssessment() {
        int y = LocalDate.now().getYear();
        return String.format("%d年度、%d年度、%d年度考核结果为　　　。", y - 3, y - 2, y - 1);
    }

    private byte[] loadPhoto(String fileId) {
        if (StringUtils.isBlank(fileId)) {
            return null;
        }
        try {
            FileDataStream s = fileService.getFileStream(fileId);
            if (s == null || s.getStream() == null) {
                return null;
            }
            try (InputStream in = s.getStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    bos.write(buf, 0, n);
                }
                return bos.toByteArray();
            }
        } catch (Exception e) {
            log.warn("读取人员照片失败, fileId={}", fileId, e);
            return null;
        }
    }

    /** 当前登录人姓名（填表人）：登录态取 jobNo，再查员工姓名 */
    private String resolveCurrentUserName() {
        try {
            String jobNo = SecurityFrameworkUtils.getLoginUser().getUser().getJobNo();
            if (StringUtils.isBlank(jobNo)) {
                return "";
            }
            return nz(wkDataAPi.getEmployee(jobNo).getData().getEmpName());
        } catch (Exception e) {
            log.warn("获取当前登录人姓名失败", e);
            return "";
        }
    }

    // ---------------- 工具 ----------------

    private String translate(String code, String table, String type) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        String v = codeInfoService.translateCode(code, table, type);
        return StringUtils.isBlank(v) ? nz(code) : v;
    }

    private String genderText(String g) {
        if ("1".equals(g)) {
            return "男";
        }
        if ("0".equals(g)) {
            return "女";
        }
        return nz(g);
    }

    private String healthText(String code) {
        String v = translate(code, HEALTH_TABLE, HEALTH_TYPE);
        return StringUtils.isBlank(v) ? DEFAULT_HEALTH : v;
    }

    private String ym(LocalDate d) {
        return d == null ? "" : d.format(YM);
    }

    /**
     * 籍贯/出生地规范化：到市一级——去掉所属区(区)/县段、去掉“省/市”后缀与“/”分隔并去重。
     * 例：吉林省/长春市/朝阳区 -> 吉林长春；吉林省/长春市/ -> 吉林长春；
     *     内蒙古赤峰市 -> 内蒙古赤峰；北京市/朝阳区/ -> 北京。
     */
    private String normalizePlace(String s) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        List<String> segs = new ArrayList<>();
        for (String part : s.split("/")) {
            String seg = part.trim();
            if (StringUtils.isBlank(seg)) {
                continue;
            }
            // 到市一级：去掉区/县（保留“地区”这类地级行政区）
            if ((seg.endsWith("区") && !seg.endsWith("地区")) || seg.endsWith("县")) {
                continue;
            }
            seg = seg.replace("省", "").replace("市", "").trim();
            if (StringUtils.isNotBlank(seg)
                    && (segs.isEmpty() || !segs.get(segs.size() - 1).equals(seg))) {
                segs.add(seg);
            }
        }
        return String.join("", segs);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String join(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (StringUtils.isNotBlank(p)) {
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }
}
