package searching_program.search_product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    //HTML form에서 put delete, patch 등 HTTP 메서드 지원
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    private final StringToLocalDatTimeConverter stringToLocalDatTimeConverter;


    public WebConfig(StringToLocalDatTimeConverter stringToLocalDatTimeConverter) {
        this.stringToLocalDatTimeConverter = stringToLocalDatTimeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDatTimeConverter);
    }

    //특정 경로에 대한 Get 요청을 처리 하기 위한 방법
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/members/delete/{id}")
//                .setViewName("deleteForm");
//    }
}
