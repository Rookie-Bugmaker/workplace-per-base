package com.crrc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crrc.entity.PostCatalogInfo;
import com.crrc.mapper.PostCatalogInfoMapper;
import com.crrc.pojo.vo.PostCatalogInfoVO;
import com.crrc.service.PostCatalogInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yukun
 */
@Service
@Slf4j
public class PostCatalogInfoServiceImpl extends WorkServiceImpl<PostCatalogInfoMapper, PostCatalogInfo> implements PostCatalogInfoService {
    public List<FlatRow> queryVO(PostCatalogInfo pageReqVO) {
        List<PostCatalogInfo> records = this.query(pageReqVO);
        List<PostCatalogInfoServiceImpl.FlatRow> rows = PostCatalogInfoServiceImpl.flattenLeafOnly(records); // 只要叶子
        return rows;
    }

    // === 展平后的行 ===
    public static class FlatRow {
        public Long categoryId;     public String category;     public String categoryCode;
        public Long groupId;        public String groupName;    public String groupCode;
        public Long typeId;         public String typeName;     public String typeCode;
    }

    /** 展平：仅输出叶子（职种）对应的一行 **/
    public static List<FlatRow> flattenLeafOnly(List<PostCatalogInfo> all) {
        Map<Long, PostCatalogInfo> byId = all.stream()
                .collect(Collectors.toMap(PostCatalogInfo::getId, Function.identity()));

        return all.stream()
                .filter(n -> "3".equals(n.getNodeLevel())) // 只取职种
                .map(n -> {
                    PostCatalogInfo group = ancestorAtLevel(n, 2, byId);
                    PostCatalogInfo category = ancestorAtLevel(n, 1, byId);
                    if (group == null || category == null) return null; // 父链不完整就跳过
                    FlatRow row = new FlatRow();
                    row.categoryId = category.getId(); row.category = category.getName(); row.categoryCode = category.getCode();
                    row.groupId = group.getId();       row.groupName = group.getName(); row.groupCode = group.getCode();
                    row.typeId = n.getId();            row.typeName = n.getName();      row.typeCode = n.getCode();
                    return row;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** 展平：包含所有节点（非叶子行的下级字段为空） */
    public static List<FlatRow> flattenAllLevels(List<PostCatalogInfo> all) {
        Map<Long, PostCatalogInfo> byId = all.stream()
                .collect(Collectors.toMap(n -> n.getId(), Function.identity()));

        List<FlatRow> out = new ArrayList<>(all.size());
        for (PostCatalogInfo n : all) {
            PostCatalogInfo category = (Integer.parseInt(n.getNodeLevel()) >= 1) ? ancestorAtLevel(n, 1, byId) : null;
            PostCatalogInfo group    = (Integer.parseInt(n.getNodeLevel()) >= 2) ? ancestorAtLevel(n, 2, byId) : null;
            PostCatalogInfo type     = (Integer.parseInt(n.getNodeLevel()) >= 3) ? n : null;

            FlatRow row = new FlatRow();
            if (category != null) {
                row.categoryId = category.getId(); row.category = category.getName(); row.categoryCode = category.getCode();
            }
            if (group != null) {
                row.groupId = group.getId(); row.groupName = group.getName(); row.groupCode = group.getCode();
            }
            if (type != null) {
                row.typeId = type.getId(); row.typeName = type.getName(); row.typeCode = type.getCode();
            }
            out.add(row);
        }
        return out;
    }

    /** 向上找指定层级的祖先：level=1 职类；2 职群；3 职种(自己) */
    private static PostCatalogInfo ancestorAtLevel(PostCatalogInfo node, int targetLevel, Map<Long, PostCatalogInfo> byId) {
        PostCatalogInfo cur = node;
        while (cur != null && Integer.parseInt(cur.getNodeLevel()) > targetLevel) {
            cur = (cur.getParentId() == null) ? null : byId.get(cur.getParentId());
        }
        return (cur != null && Integer.parseInt(cur.getNodeLevel()) == targetLevel) ? cur : null;
    }
}
