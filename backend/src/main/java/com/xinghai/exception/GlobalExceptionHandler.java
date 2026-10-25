package com.xinghai.exception;

import com.xinghai.common.Result;
import com.xinghai.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器：兜住所有异常，统一返回 Result 结构，避免把堆栈抛给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常（@Valid 对 @RequestBody 触发） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        log.warn("参数校验失败: {}", msg);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 表单参数绑定校验异常 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        log.warn("参数绑定失败: {}", msg);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 单个参数校验异常（@RequestParam/@PathVariable 上的 @Min 等） */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst().map(v -> v.getMessage()).orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "缺少必填参数: " + e.getParameterName());
    }

    /** 参数类型不匹配（如路径需要数字却传了字母） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "参数格式不正确: " + e.getName());
    }

    /** 请求体不可读 / JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误，请检查 JSON 是否正确");
    }

    /** 请求方式不支持（如把 GET 接口用 POST 访问） */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方式不支持: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请求方式不支持: " + e.getMethod());
    }

    /** 接口不存在（404） */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNoHandler(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    /** 唯一键冲突（如重复创建、唯一索引冲突） */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicate(DuplicateKeyException e) {
        log.warn("唯一约束冲突: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "数据重复，请勿重复提交");
    }

    /** 数据完整性异常（如字段超长、外键约束等），统一转成 400 而非 500 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("数据完整性异常: {}", e.getMostSpecificCause().getMessage());
        String root = e.getMostSpecificCause() == null ? "" : e.getMostSpecificCause().getMessage();
        String msg = root != null && root.contains("Data too long")
                ? "提交内容过长，请精简后再试" : "数据不合法，请检查后重试";
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /** 上传文件超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.warn("上传文件超出大小限制: {}", e.getMessage());
        return Result.fail(ResultCode.FILE_SIZE_ERROR);
    }

    /** multipart 请求缺少文件片段（如表单字段名不是 file） */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<Void> handleMissingPart(MissingServletRequestPartException e) {
        log.warn("上传缺少文件片段: {}", e.getRequestPartName());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "请选择要上传的文件（字段名需为 file）");
    }

    /** multipart 请求整体异常（如请求不是合法的 multipart/form-data） */
    @ExceptionHandler(MultipartException.class)
    public Result<Void> handleMultipart(MultipartException e) {
        log.warn("上传请求格式错误: {}", e.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), "上传请求格式错误，请使用 multipart/form-data 提交");
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.ERROR);
    }
}
