package com.team6.onandthefarm.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SellerDto {
    private String email;
    private String password;
    private String zipcode;
    private String address;
    private String addressDetail;
    private String phone;
    private String name;
    private String shopName;
}
