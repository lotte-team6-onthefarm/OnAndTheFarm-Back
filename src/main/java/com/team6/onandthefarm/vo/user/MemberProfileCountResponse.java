package com.team6.onandthefarm.vo.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberProfileCountResponse {
    private Integer photoCount;
    private Integer scrapCount;
    private Integer likeCount;
}
