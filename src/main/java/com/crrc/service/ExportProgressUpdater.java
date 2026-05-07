package com.crrc.service;

/**
 * 导出进度更新器接口
 */
public interface ExportProgressUpdater {

    /**
     * 初始化总数量
     *
     * @param totalCount 总记录数
     */
    void initTotal(int totalCount);

    /**
     * 增加已处理数量
     *
     * @param finishedDelta 增量
     */
    void increment(int finishedDelta);

    /**
     * 更新已处理数量（设置绝对值）
     *
     * @param finishedCount 已处理数量
     */
    void updateFinished(int finishedCount);

    /**
     * 直接更新进度值
     *
     * @param progress 进度百分比
     */
    void updateProgress(int progress);
}
