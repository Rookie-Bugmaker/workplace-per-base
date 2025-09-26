package com.crrc.mongodb;

public interface ChangeRequest {
    String getId();
    String getUserCode();
    Object getChangedData();  // 返回Object，因为不同类型返回不同DTO
    String getStatus();
    void setStatus(String status);
}