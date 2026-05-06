package com.crrc.service.impl.translation;

import lombok.Getter;

import java.util.Map;

/**
 * 导出字段翻译规则
 */
@Getter
public class ExportFieldTranslationRule {

    /** 翻译类型 */
    public enum Type {
        /** map字典翻译 */
        MAP,
        /** CodeInfo 字典翻译 */
        CODE_INFO,
        /** 部门/组织翻译 */
        DEPT,
        /** 布尔标志翻译 (0/1 -> 否/是) */
        BOOLEAN_FLAG,
        /** 离职标记翻译 (0/1 -> 在职/离职) */
        EMPLOYED_FLAG,
        /** 性别标记翻译 (0/1 -> 女/男) */
        GENDER_FLAG,
        /** 籍贯编码翻译 */
        NATIVE_PLACE
    }

    private final Type type;

    /** CodeInfo: tableName */
    private final String tableName;

    /** CodeInfo: type */
    private final String typeCode;

    /**
     * 源字段名，用于 "xxxValue" 虚拟字段
     * 例如 politicalValue 需要从 political 字段取原始码值
     */
    private final String sourceField;

    /**
     * 翻译map
     */
    private final Map<String, String> translateMapCollection;

    public static ExportFieldTranslationRule map(Map<String, String> map) {

        return new ExportFieldTranslationRule(Type.MAP, null, null, null, map);
    }

    public static ExportFieldTranslationRule map(Map<String, String> map, String sourceField) {
        return new ExportFieldTranslationRule(Type.MAP, null, null, sourceField, map);
    }

    public static ExportFieldTranslationRule codeInfo(String tableName, String typeCode) {
        return new ExportFieldTranslationRule(Type.CODE_INFO, tableName, typeCode, null);
    }

    public static ExportFieldTranslationRule codeInfo(String tableName, String typeCode, String sourceField) {
        return new ExportFieldTranslationRule(Type.CODE_INFO, tableName, typeCode, sourceField);
    }

    public static ExportFieldTranslationRule dept() {
        return new ExportFieldTranslationRule(Type.DEPT, null, null, null);
    }

    public static ExportFieldTranslationRule dept(String sourceField) {
        return new ExportFieldTranslationRule(Type.DEPT, null, null, sourceField);
    }

    public static ExportFieldTranslationRule booleanFlag() {
        return new ExportFieldTranslationRule(Type.BOOLEAN_FLAG, null, null, null);
    }

    public static ExportFieldTranslationRule booleanFlag(String sourceField) {
        return new ExportFieldTranslationRule(Type.BOOLEAN_FLAG, null, null, sourceField);
    }

    public static ExportFieldTranslationRule employedFlag() {
        return new ExportFieldTranslationRule(Type.EMPLOYED_FLAG, null, null, null);
    }

    public static ExportFieldTranslationRule genderFlag() {
        return new ExportFieldTranslationRule(Type.GENDER_FLAG, null, null, null);
    }

    public static ExportFieldTranslationRule genderFlag(String sourceField) {
        return new ExportFieldTranslationRule(Type.GENDER_FLAG, null, null, sourceField);
    }

    public static ExportFieldTranslationRule nativePlace() {
        return new ExportFieldTranslationRule(Type.NATIVE_PLACE, "hr_emp_basic", "BASIC_NATIVE_PLACE_NAME", null);
    }

    public static ExportFieldTranslationRule nativePlace(String sourceField) {
        return new ExportFieldTranslationRule(Type.NATIVE_PLACE, "hr_emp_basic", "BASIC_NATIVE_PLACE_NAME", sourceField);
    }

    public static ExportFieldTranslationRule employedFlag(String sourceField) {
        return new ExportFieldTranslationRule(Type.EMPLOYED_FLAG, null, null, sourceField);
    }

    private ExportFieldTranslationRule(Type type, String tableName, String typeCode, String sourceField) {
        this.type = type;
        this.tableName = tableName;
        this.typeCode = typeCode;
        this.sourceField = sourceField;
        this.translateMapCollection = null;
    }

    private ExportFieldTranslationRule(Type type, String tableName, String typeCode, String sourceField, Map<String, String> map) {
        this.type = type;
        this.tableName = tableName;
        this.typeCode = typeCode;
        this.sourceField = sourceField;
        this.translateMapCollection = map;
    }
}
