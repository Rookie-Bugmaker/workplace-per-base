package com.crrc.common;

/**
 * 全局返回值
 *
 * @param <T> 泛型
 * @author 谭永强
 */
public class ResultData<T> {

    /**
     * 响应编码，默认200
     */
    private Integer code = 200;

    /**
     * 响应消息
     */
    private String message = "系统繁忙，请联系管理员!";

    /**
     * 响应状态，成功/失败：ture/false
     */
    private Boolean success = false;

    /**
     * 响应结果
     */
    private T result;

    /**
     * 无参构造
     */
    public ResultData() {
    }

    /**
     * 有参构造
     *
     * @param message 消息
     */
    public ResultData(String message) {
        this.message = message;
    }

    /**
     * 有参构造
     *
     * @param message 消息
     * @param result  返回值
     */
    public ResultData(String message, T result) {
        this.message = message;
        this.result = result;
    }

    /**
     * 有参构造
     *
     * @param success 成功/失败
     * @param message 消息
     * @param result  返回值
     */
    public ResultData(Boolean success, String message, T result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    /**
     * 有参构造
     *
     * @param code    状态码
     * @param success 成功/失败
     * @param message 消息
     */
    public ResultData(Integer code, Boolean success, String message) {
        this.code = code;
        this.success = success;
        this.message = message;
    }

    /**
     * 成功返回结果
     *
     * @param data 返回的数据
     * @param <T>  泛型
     * @return 结果
     */
    public static <T> ResultData<T> success(T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(ReturnCode.RC200.getCode());
        resultData.setSuccess(true);
        resultData.setMessage(ReturnCode.RC200.getMessage());
        resultData.setResult(data);
        return resultData;
    }

    /**
     * 错误返回
     *
     * @param code    状态码
     * @param message 消息
     * @param <T>     泛型
     * @return 结果
     */
    public static <T> ResultData<T> fail(int code, String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setMessage(message);
        return resultData;
    }

    public static <T> ResultData<T> fail(int code, String message, T t) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setMessage(message);
        resultData.setResult(t);
        return resultData;
    }

    /**
     * 熔断返回
     *
     * @param <T> 泛型
     * @return 结果
     */
    public static <T> ResultData<T> fallback() {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(ReturnCode.RC202.getCode());
        resultData.setMessage(ReturnCode.RC202.getMessage());
        return resultData;
    }
    /**
     * 熔断返回
     *
     * @param <T> 泛型
     * @return 结果
     */
    public static <T> ResultData<T> fallback(String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setCode(ReturnCode.RC202.getCode());
        resultData.setMessage(message);
        return resultData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
