package com.crrc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.pojo.vo.PostCatalogInfoImportResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yukun
 */
public interface PostCatalogInfoService extends IService<PostCatalogInfo> {
    /**
     * 新增职务族节点，进行编码唯一、类别层级、父子关系校验。
     */
    boolean saveWithValidation(PostCatalogInfo postCatalogInfo);

    /**
     * 更新职务族节点，进行编码唯一、类别层级、父子关系校验。
     */
    boolean updateWithValidation(PostCatalogInfo postCatalogInfo);

    /**
     * 删除或停用职务族节点。
     * 已被职位或下级引用时仅停用；无下级且无引用时逻辑删除。
     */
    boolean removeOrDisableById(Long id);

    /**
     * 停用职务族节点。停用父节点时级联停用下级节点。
     */
    boolean disableById(Long id);

    /**
     * 启用职务族节点。启用子节点前要求父节点已启用。
     */
    boolean enableById(Long id);

    /**
     * 按文档模板完整导入职类/职群/职种树。
     */
    PostCatalogInfoImportResultVO importFull(MultipartFile file) throws Exception;
}
