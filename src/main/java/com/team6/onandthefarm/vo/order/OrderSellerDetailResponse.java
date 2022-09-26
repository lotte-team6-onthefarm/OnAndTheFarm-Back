package com.team6.onandthefarm.vo.order;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSellerDetailResponse {
    List<OrderFindOneResponse> orderProducts;
    private String orderName;
    private String orderPhone;
    private String orderAddress;
    private String orderRequest;
    private String orderDate;
}
