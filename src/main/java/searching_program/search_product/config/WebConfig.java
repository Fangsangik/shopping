package searching_program.search_product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToLocalDatTimeConverter stringToLocalDatTimeConverter;


    public WebConfig(StringToLocalDatTimeConverter stringToLocalDatTimeConverter) {
        this.stringToLocalDatTimeConverter = stringToLocalDatTimeConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDatTimeConverter);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/members/delete/{id}")
                .setViewName("forward:/error");
    }
}
