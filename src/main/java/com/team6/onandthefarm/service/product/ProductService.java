package com.team6.onandthefarm.service.product;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.team6.onandthefarm.dto.product.*;
import com.team6.onandthefarm.entity.product.ProductQna;
import com.team6.onandthefarm.entity.product.ProductQnaAnswer;
import com.team6.onandthefarm.vo.product.*;
import org.springframework.web.multipart.MultipartFile;
import com.team6.onandthefarm.entity.product.Product;

public interface ProductService {
	Long saveProduct(ProductFormDto productFormDto) throws IOException;
	
    Long updateProduct(ProductUpdateFormDto productUpdateFormDto);

    Long deleteProduct(ProductDeleteDto productDeleteDto);

    ProductWishResultDto addProductToWishList(ProductWishFormDto productWishFormDto);

    List<Long> cancelProductFromWishList(ProductWishCancelDto productWishCancelDto);

    List<ProductWishResponse> getWishList(Long userId);

    List<ProductSelectionResponse> getAllProductListOrderByNewest(Long userId, Integer pageNumber);

    List<ProductSelectionResponse> getProductsListByHighPrice(Long userId, Integer pageNumber);

    List<ProductSelectionResponse> getProductsListByLowPrice(Long userId, Integer pageNumber);

    List<ProductSelectionResponse> getProductsBySoldCount(Long userId, Integer pageNumber);

    List<ProductSelectionResponse> getProductListByCategoryNewest(Long userId, String Category, Integer pageNumber);

    List<ProductSelectionResponse> getProductListByCategoryHighest(Long userId, String Category, Integer pageNumber);

    List<ProductSelectionResponse> getProductListByCategoryLowest(Long userId, String category, Integer pageNumber);

    List<ProductSelectionResponse> getProductsByCategorySoldCount(Long userId, String category, Integer pageNumber);

    List<ProductSelectionResponse> getProductListBySellerNewest(Long userId, Long SellerId, Integer pageNumber);

    List<ProductQnAResponse> findProductQnAList(Long productId);

	List<ProductReviewResponse> getProductsWithoutReview(Long userId);
	
    ProductDetailResponse findProductDetail(Long productId, Long userId);
}