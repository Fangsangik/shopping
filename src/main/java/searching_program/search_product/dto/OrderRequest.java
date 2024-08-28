package searching_program.search_product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private MemberDto memberDto;
    private OrderDto orderDto;
}
