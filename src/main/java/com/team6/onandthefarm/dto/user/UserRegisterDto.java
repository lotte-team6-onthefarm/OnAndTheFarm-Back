package com.team6.onandthefarm.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterDto {

    private Long userId;

    private Integer userZipcode;

    private String userAddress;

    private String userAddressDetail;

    private String userPhone;

    private String userBirthday;

    private Integer userSex;

    private String userName;
}