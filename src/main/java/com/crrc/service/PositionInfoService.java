package com.crrc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PositionInfo;
import com.crrc.pojo.dto.PositionInfoCopyRequest;
import com.crrc.pojo.vo.PositionInfoBasicExportVO;
import com.crrc.pojo.vo.PositionInfoDescriptionExportVO;
import com.crrc.pojo.vo.PositionInfoImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author yukun
 */
public interface PositionInfoService extends IService<PositionInfo> {
    /**
     * 翻译字段
     * @param records 需要翻译的职位信息
     */
    void resolveField(List<PositionInfo> records);

    /**
     * 按顺序查询
     */
    void queryByOrder(Page<PositionInfo> page, PositionInfo pageReqVO);

    /**
     * 通过职位编码查询职位列表
     * @param codes 职位编码List
     * @return 查询回的职位列表，如果没查询到职位会返回空值
     */
    List<PositionInfo> selectByCodes(List<String> codes);

    /**
     * 导入文件，只输入编码code和标准岗级jobGrade
     */
    void importJobGrade(MultipartFile file) throws Exception;

    /**
     * 完整职位信息导入，支持新增和按职位编码更新。
     */
    PositionInfoImportResultVO importFull(MultipartFile file) throws Exception;

    /**
     * 删除职位信息，删除前按任职信息实时判断是否存在在岗人员。
     */
    boolean removePositionInfoWithIncumbentCheck(Collection<? extends Serializable> idList);

    /**
     * 刷新指定职位的在岗人数和在岗人员快照；codes为空时刷新全部职位。
     */
    int refreshIncumbents(Collection<String> codes);

    /**
     * 复制职位到指定组织单元。
     */
    List<PositionInfo> copyToOrgUnit(PositionInfoCopyRequest request);


    /**
     * 按查询条件导出职位信息基本版。
     */
    List<PositionInfoBasicExportVO> exportBasic(PositionInfo queryReqVO);

    /**
     * 按查询条件导出职位信息及当前职位说明书内容。
     */
    List<PositionInfoDescriptionExportVO> exportWithDescription(PositionInfo queryReqVO);

    /**
     * 保存或更新职位，进行职位编码唯一性和默认字段处理。
     */
    boolean saveOrUpdateWithValidation(PositionInfo positionInfo);
}
