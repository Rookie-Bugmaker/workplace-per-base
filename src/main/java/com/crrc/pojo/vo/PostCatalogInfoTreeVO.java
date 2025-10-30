package com.crrc.pojo.vo;

import com.crrc.entity.PostCatalogInfo;
import lombok.Data;

import java.util.List;

/**
 * @author yukun
 */
@Data
public class PostCatalogInfoTreeVO extends PostCatalogInfo {
    List<PostCatalogInfoTreeVO> children;

}
