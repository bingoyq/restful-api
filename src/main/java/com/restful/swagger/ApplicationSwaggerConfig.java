package com.restful.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by yuananyun on 2015/11/24.
 */
@EnableSwagger2
public class ApplicationSwaggerConfig {

//    @Bean
//    public Docket ProductApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .genericModelSubstitutes(DeferredResult.class)
//                .useDefaultResponseMessages(false)
//                .forCodeGeneration(false)
//                .pathMapping("/")
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.example.web"))
//                .build()
//                .apiInfo(productApiInfo());
//
//    @Bean
//    public ApiInfo productApiInfo() {
//        Docket docket = new Docket(DocumentationType.SWAGGER_2);
//        ApiInfo apiInfo = new ApiInfo("唯特迅开放平台API",
//                "文档描述。。。",
//                "1.0.0",
//                "API TERMS URL",
//                "联系人邮箱",
//                "license",
//                "license url");
//        //docket.apiInfo(apiInfo);
//        return apiInfo;
//    }

        @Bean
        public Docket ProductApi() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .genericModelSubstitutes(DeferredResult.class)
                    .useDefaultResponseMessages(false)
                    .forCodeGeneration(false)
                    .pathMapping("/")
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("com.restful.controller"))
                    .build()
                    .apiInfo(productApiInfo());
        }
        @Bean
        public ApiInfo productApiInfo() {
            ApiInfo apiInfo = new ApiInfo("唯特迅开放平台API",
                    "文档描述。。。",
                    "1.0.0",
                    "API TERMS URL",
                    "联系人邮箱",
                    "license",
                    "license url");
            return apiInfo;
        }
}
