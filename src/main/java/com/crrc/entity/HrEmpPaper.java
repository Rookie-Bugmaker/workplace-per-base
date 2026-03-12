package com.crrc.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.crrc.annotation.QueryMethod;
import com.crrc.enums.QueryType;
import com.crrc.pojo.dto.SysFileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 论文论著信息表
 * </p>
 *
 * @author shenshuo
 * @since 2026-03-10 11:33:05
 */
@ExcelIgnoreUnannotated
@Accessors(chain = false)
@TableName("hr_emp_paper")
@Schema(description = "员工论文论著信息表")
@Data
@NoArgsConstructor
public class HrEmpPaper extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 2031211713627750400L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @TableLogic(value = "0")
    @Schema(description = "删除", example = "false")
    private Integer deleted;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"工号"}, index = 1)
    @Schema(description = "工号")
    private String userCode;

    @ExcelProperty(value = {"发表日期"}, index = 2)
    @Schema(description = "发表日期")
    private LocalDate publishDate;

    @ExcelProperty(value = {"论文/论著"},index = 3)
    @Schema(description = "论文/论著")
    private String paperType;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"论文（专著/教材）名称"}, index = 4)
    @Schema(description = "论文（专著/教材）名称")
    private String paperName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否被SCI、EI、ISTP检索"}, index = 5)
    @Schema(description = "是否被SCI、EI、ISTP检索")
    private String isSciEiIstp;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"刊物/出版社名称"}, index = 6)
    @Schema(description = "刊物/出版社名称")
    private String journalName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否为核心期刊"}, index = 7)
    @Schema(description = "是否为核心期刊")
    private String isCoreJournal;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"刊物级别"}, index = 8)
    @Schema(description = "刊物级别")
    private String journalLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"出版专著说明"}, index = 9)
    @Schema(description = "出版专著说明")
    private String publishDesc;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖项名称"}, index = 10)
    @Schema(description = "奖项名称")
    private String awardName;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励级别"}, index = 11)
    @Schema(description = "奖励级别")
    private String awardLevel;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"奖励等次"}, index = 12)
    @Schema(description = "奖励等次")
    private String awardRank;
    
    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"授奖单位"}, index = 13)
    @Schema(description = "授奖单位")
    private String awardOrg;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"个人排名"}, index = 14)
    @Schema(description = "个人排名")
    private String personRank;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"排名总人数"}, index = 15)
    @Schema(description = "排名总人数")
    private String totalRank;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"国内刊物期号"}, index = 16)
    @Schema(description = "国内刊物期号")
    private String domesticIssue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"国际刊物期号"}, index = 17)
    @Schema(description = "国际刊物期号")
    private String internationalIssue;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"字数"}, index = 18)
    @Schema(description = "字数")
    private Integer wordCount;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"论文分类"}, index = 19)
    @Schema(description = "论文分类")
    private String paperCategory;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"检索号"}, index = 20)
    @Schema(description = "检索号")
    private String retrievalNo;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"附件挂载Id"}, index = 21)
    @Schema(description = "附件挂载Id")
    private String fileHeaderId;

    @QueryMethod(method = QueryType.EQ)
    @ExcelProperty(value = {"是否存在正在被修改的状态"}, index = 22)
    @Schema(description = "是否存在正在被修改的状态，0为否，1为是")
    private String isUpdating;


    @Schema(description = "通过fileHeaderId，查询回的文件列表")
    @TableField(exist = false)
    private List<SysFileDTO> files;

}
