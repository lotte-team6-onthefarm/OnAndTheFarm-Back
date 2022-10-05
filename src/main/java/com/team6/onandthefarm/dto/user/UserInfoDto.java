package com.team6.onandthefarm.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDto {

    private Long userId;

    private String userZipcode;

    private String userAddress;

    private String userAddressDetail;

    private String userPhone;

    private String userBirthday;

    private Integer userSex;

    private String userName;
}
