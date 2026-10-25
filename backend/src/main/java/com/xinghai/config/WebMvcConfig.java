package com.xinghai.config;

import com.xinghai.security.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Web MVC 配置：跨域 + 静态资源映射 + 拦截器注册
 * <p>跨域来源与上传目录均通过配置注入，生产环境可通过环境变量收敛，
 * 避免代码中硬编码域名/路径。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${xinghai.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${xinghai.upload.base-dir}")
    private String uploadBaseDir;

    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    /**
     * 跨域配置：来源白名单来自 xinghai.cors.allowed-origins（逗号分隔）
     * 配置为 * 时退化为允许所有来源（仅建议开发环境使用）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        var registration = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
        if (origins.length == 1 && "*".equals(origins[0])) {
            registration.allowedOriginPatterns("*").allowCredentials(true);
        } else {
            registration.allowedOrigins(origins).allowCredentials(true);
        }
    }

    /** 本地上传目录映射：URL /upload/** 指向配置的上传根目录 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadBaseDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/upload/**").addResourceLocations(location);
    }

    /** 注册拦截器：拦截所有 /api/** 接口（OPTIONS 预检请求放行） */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/error");
    }
}
