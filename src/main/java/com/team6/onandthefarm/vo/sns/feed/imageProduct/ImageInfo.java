package com.team6.onandthefarm.vo.sns.feed.imageProduct;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageInfo {

    private Long feedImageId;
    private String feedImageSrc;
}
