package com.xinghai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XingHai 星海视频社交平台 · 启动类
 * <p>@MapperScan 扫描 mapper 包，自动为每个 Mapper 接口生成代理实现。</p>
 */
@SpringBootApplication
@MapperScan("com.xinghai.mapper")
public class XingHaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XingHaiApplication.class, args);
        System.out.println("========================================");
        System.out.println("  XingHai 星海平台后端启动成功!");
        System.out.println("  接口地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
