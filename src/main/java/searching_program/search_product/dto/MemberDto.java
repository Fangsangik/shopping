package searching_program.search_product.dto;

import lombok.*;
import searching_program.search_product.type.Grade;
import searching_program.search_product.type.MemberStatus;
import searching_program.search_product.type.PaymentMethod;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {

    private Long id; // 식별자 필드 추가

    private String username;
    private int age;
    private String password;
    private String address;
    private LocalDateTime birth;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private PaymentMethod paymentMethod;
    private MemberStatus memberStatus;
    private Grade grade;
}
