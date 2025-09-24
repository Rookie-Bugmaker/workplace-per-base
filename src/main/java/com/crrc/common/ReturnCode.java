package com.crrc.common;

/**
 * 定义请求状态码
 *
 * @author 谭永强
 * @date 2024-11-06
 */
public enum ReturnCode {

    /**
     * 操作成功
     **/
    RC200(200, "操作成功"),
    /**
     * 操作失败
     **/
    RC999(999, "操作失败"),
    /**
     * 服务限流
     **/
    RC201(201, "服务开启限流保护,请稍后再试!"),
    /**
     * 服务降级
     **/
    RC202(202, "服务开启降级保护,请稍后再试!"),
    /**
     * 热点参数限流
     **/
    RC203(203, "热点参数限流了,请稍后再试!"),
    /**
     * 触发系统保护规则
     **/
    RC204(204, "触发系统保护规则,请稍后再试!"),
    /**
     * 授权规则不通过
     **/
    RC205(205, "授权规则不通过,请稍后再试!"),
    /**
     * access_denied
     **/
    RC403(403, "无访问权限,请联系管理员授予权限"),
    /**
     * access_denied
     **/
    RC401(401, "匿名用户访问无权限资源时的异常"),
    /**
     * 服务异常
     **/
    RC500(500, "系统异常，请稍后重试"),
    /**
     * 客户端认证失败
     */
    CLIENT_AUTHENTICATION_FAILED(1001, "客户端认证失败"),
    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    /**
     * 不支持的认证模式
     */
    UNSUPPORTED_GRANT_TYPE(1003, "不支持的认证模式"),
    /**
     * 访问令牌不合法
     */
    INVALID_TOKEN(2001, "访问令牌不合法"),
    /**
     * 账户在异地登录
     */
    OFFSITE_LOGIN_TOKEN(2002, "账户在异地登录"),
    /**
     * 没有权限访问该资源
     */
    ACCESS_DENIED(2003, "没有权限访问该资源"),
    /**
     * Excel 导入导出错误
     */
    EXCEL_ERROR(3001, "Excel 导入导出错误");


    /**
     * 自定义状态码
     **/
    private final int code;
    /**
     * 自定义描述
     **/
    private final String message;

    ReturnCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
