package com.hell.backend.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // .env 파일 로드
        System.out.println("DotenvEnvironmentPostProcessor 실행됨");
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        // .env 파일의 내용을 Map으로 변환
        Map<String, Object> props = new HashMap<>();
        dotenv.entries().forEach(entry -> props.put(entry.getKey(), entry.getValue()));

        // 새로운 프로퍼티 소스 생성
        MapPropertySource propertySource = new MapPropertySource("dotenvPropertySource", props);

        // 프로퍼티 소스를 환경에 추가
        environment.getPropertySources().addLast(propertySource);
    }
}
