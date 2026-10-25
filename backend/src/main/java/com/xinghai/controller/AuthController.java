package com.xinghai.controller;

import com.xinghai.common.Result;
import com.xinghai.dto.LoginDTO;
import com.xinghai.dto.RegisterDTO;
import com.xinghai.security.UserContext;
import com.xinghai.service.AuthService;
import com.xinghai.service.RateLimitService;
import com.xinghai.vo.LoginVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证接口：注册 / 登录 / 当前用户 / 注销
 * <p>登录与注册按客户端 IP 做限流，防止暴力破解与批量注册。</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @Value("${xinghai.rate-limit.login-limit:10}")
    private int loginLimit;

    @Value("${xinghai.rate-limit.login-window:60}")
    private int loginWindow;

    @Value("${xinghai.rate-limit.register-limit:5}")
    private int registerLimit;

    @Value("${xinghai.rate-limit.register-window:300}")
    private int registerWindow;

    public AuthController(AuthService authService, RateLimitService rateLimitService) {
        this.authService = authService;
        this.rateLimitService = rateLimitService;
    }

    /** 注册（自动登录返回令牌） */
    @PostMapping("/register")
    public Result<LoginVO> register(@Validated @RequestBody RegisterDTO dto, HttpServletRequest request) {
        rateLimitService.check("register", clientIp(request), registerLimit, registerWindow);
        return Result.ok("注册成功", authService.register(dto));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO dto, HttpServletRequest request) {
        rateLimitService.check("login", clientIp(request), loginLimit, loginWindow);
        return Result.ok("登录成功", authService.login(dto));
    }

    /** 当前登录用户ID（前端校验登录态用） */
    @GetMapping("/me")
    public Result<Long> me() {
        return Result.ok(UserContext.getUserId());
    }

    /** 注销（前端丢弃令牌即可，服务端无会话可清） */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }

    /** 取客户端真实 IP：兼容反向代理场景，仅取 X-Forwarded-For 的第一段 */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
