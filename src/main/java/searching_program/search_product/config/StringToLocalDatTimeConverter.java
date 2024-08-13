package searching_program.search_product.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToLocalDatTimeConverter implements Converter<String, LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    public LocalDateTime convert(String source) {
        try {
            // 문자열을 LocalDateTime으로 변환합니다. 형식은 yyyy-MM-ddTHH:mm 입니다.
            return LocalDateTime.parse(source, formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다. 'yyyy-MM-ddTHH:mm' 형식을 사용하세요.", e);
        }
    }
}
