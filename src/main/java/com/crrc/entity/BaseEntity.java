package com.crrc.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Schema(
        description = "基础实体类"
)
@Accessors(chain = true)
@Data
public abstract class BaseEntity implements Serializable {
    @TableField(
            fill = FieldFill.INSERT
    )
    @Schema(
            description = "创建人",
            example = "1"
    )
    private Long createBy;
    @TableField(
            fill = FieldFill.INSERT
    )
    @Schema(
            description = "创建时间",
            example = "2024-01-01 00:00:00"
    )
    private LocalDateTime createTime;
    @TableField(
            fill = FieldFill.INSERT_UPDATE
    )
    @Schema(
            description = "更新人",
            example = "1"
    )
    private Long updateBy;
    @TableField(
            fill = FieldFill.INSERT_UPDATE
    )
    @Schema(
            description = "更新时间",
            example = "2024-01-01 00:00:00"
    )
    private LocalDateTime updateTime;
    @TableField(
            exist = false
    )
    @Schema(
            description = "创建人名称",
            example = "admin"
    )
    private String createPerson;
    @TableField(
            exist = false
    )
    @Schema(
            description = "更新人名称",
            example = "admin"
    )
    private String updatePerson;

    @TableField(
            exist = false
    )
    private Map<String, Object> dynamicFields = new ConcurrentHashMap<>();

    @JsonIgnore
    public Map<String, Object> getDynamicFields() {
        return dynamicFields;
    }

    @JsonAnyGetter
    public Map<String, Object> anyDynamicFields() {
        return dynamicFields == null ? Collections.emptyMap() : dynamicFields;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof BaseEntity)) {
            return false;
        } else {
            BaseEntity other = (BaseEntity)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$createBy = this.getCreateBy();
                Object other$createBy = other.getCreateBy();
                if (this$createBy == null) {
                    if (other$createBy != null) {
                        return false;
                    }
                } else if (!this$createBy.equals(other$createBy)) {
                    return false;
                }

                Object this$updateBy = this.getUpdateBy();
                Object other$updateBy = other.getUpdateBy();
                if (this$updateBy == null) {
                    if (other$updateBy != null) {
                        return false;
                    }
                } else if (!this$updateBy.equals(other$updateBy)) {
                    return false;
                }

                Object this$createTime = this.getCreateTime();
                Object other$createTime = other.getCreateTime();
                if (this$createTime == null) {
                    if (other$createTime != null) {
                        return false;
                    }
                } else if (!this$createTime.equals(other$createTime)) {
                    return false;
                }

                Object this$updateTime = this.getUpdateTime();
                Object other$updateTime = other.getUpdateTime();
                if (this$updateTime == null) {
                    if (other$updateTime != null) {
                        return false;
                    }
                } else if (!this$updateTime.equals(other$updateTime)) {
                    return false;
                }

                Object this$createPerson = this.getCreatePerson();
                Object other$createPerson = other.getCreatePerson();
                if (this$createPerson == null) {
                    if (other$createPerson != null) {
                        return false;
                    }
                } else if (!this$createPerson.equals(other$createPerson)) {
                    return false;
                }

                Object this$updatePerson = this.getUpdatePerson();
                Object other$updatePerson = other.getUpdatePerson();
                if (this$updatePerson == null) {
                    if (other$updatePerson != null) {
                        return false;
                    }
                } else if (!this$updatePerson.equals(other$updatePerson)) {
                    return false;
                }

                Object this$dynamicFields = this.getDynamicFields();
                Object other$dynamicFields = other.getDynamicFields();
                if (this$dynamicFields == null) {
                    if (other$dynamicFields != null) {
                        return false;
                    }
                } else if (!this$dynamicFields.equals(other$dynamicFields)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BaseEntity;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $createBy = this.getCreateBy();
        result = result * PRIME + ($createBy == null ? 43 : $createBy.hashCode());
        Object $updateBy = this.getUpdateBy();
        result = result * PRIME + ($updateBy == null ? 43 : $updateBy.hashCode());
        Object $createTime = this.getCreateTime();
        result = result * PRIME + ($createTime == null ? 43 : $createTime.hashCode());
        Object $updateTime = this.getUpdateTime();
        result = result * PRIME + ($updateTime == null ? 43 : $updateTime.hashCode());
        Object $createPerson = this.getCreatePerson();
        result = result * PRIME + ($createPerson == null ? 43 : $createPerson.hashCode());
        Object $updatePerson = this.getUpdatePerson();
        result = result * PRIME + ($updatePerson == null ? 43 : $updatePerson.hashCode());
        Object $dynamicFields = this.getDynamicFields();
        result = result * PRIME + ($dynamicFields == null ? 43 : $dynamicFields.hashCode());
        return result;
    }

    public String toString() {
        return "BaseEntity(createBy=" + this.getCreateBy() + ", createTime=" + this.getCreateTime() + ", updateBy=" + this.getUpdateBy() + ", updateTime=" + this.getUpdateTime() + ", createPerson=" + this.getCreatePerson() + ", updatePerson=" + this.getUpdatePerson() + ", dynamicFields=" + this.getDynamicFields() + ")";
    }
}