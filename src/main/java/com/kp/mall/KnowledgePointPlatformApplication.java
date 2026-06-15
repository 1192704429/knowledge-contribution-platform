package com.kp.mall;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wuming
 * @description: 基于SpringBoot的知识贡献积分激励与兑换平台服务端启动入口
 */
@SpringBootApplication
@Slf4j
@MapperScan("com.kp.mall.**.mapper")
@EnableScheduling
public class KnowledgePointPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgePointPlatformApplication.class, args);
        log.info("JAVA后端服务启动成功！！！！！！");
    }

}
