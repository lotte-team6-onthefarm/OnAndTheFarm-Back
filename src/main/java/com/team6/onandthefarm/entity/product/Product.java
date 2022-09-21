package com.team6.onandthefarm.entity.product;

import com.team6.onandthefarm.entity.category.Category;
import com.team6.onandthefarm.entity.seller.Seller;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Builder
@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    private Seller seller;

    private String productName;

    private Integer productPrice;

    private Integer productTotalStock;

    private String productMainImgSrc;

    private String productDetail;

    private String productDetailShort;

    private String productOriginPlace;

    private String productDeliveryCompany;

    private String productRegisterDate;

    private String productUpdateDate;

    private String productStatus;

    private Integer productWishCount;
}