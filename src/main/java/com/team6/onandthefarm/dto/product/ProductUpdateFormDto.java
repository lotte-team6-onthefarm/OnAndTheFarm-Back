package com.team6.onandthefarm.dto.product;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductUpdateFormDto {
	private Long productId;

	@NotBlank(message = "상품명은 필수 입력 값입니다.")
	private String productName;

	@NotBlank(message = "카테고리는 필수 입력 값입니다.")
	private String productCategoryId;

	@NotNull(message = "가격은 필수 입력 값입니다.")
	private Integer productPrice;

	@NotNull(message = "재고는 필수 입력 값입니다.")
	private Integer productTotalStock;

	// @NotBlank(message = "상품대표 이미지는 필수 입력입니다.")
	private String productMainImgSrc;

	@NotBlank(message = "상세내용은 필수 입력 값입니다.")
	private String productDetail;

	@NotBlank(message = "원산지는 필수 입력 값입니다.")
	private String productOriginPlace;

	@NotBlank(message = "택배사는 필수 입력 값입니다.")
	private String productDeliveryCompany;

	private String productStatus;

	private String productDetailShort;

	private String productRegisterDate;

	private String productUpdateDate;

	private Integer productWishCount;

	private List<ProductImgDto> productImgDtoList = new ArrayList<>();
}
