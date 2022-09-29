package com.team6.onandthefarm.vo.order;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUserResponseList {
    List<OrderSellerResponse> orderSellerResponses;

    private Integer orderTotalPrice;

    private String orderDate;
}