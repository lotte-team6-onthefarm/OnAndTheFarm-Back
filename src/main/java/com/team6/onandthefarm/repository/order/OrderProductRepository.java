package com.team6.onandthefarm.repository.order;

import com.team6.onandthefarm.entity.order.OrderProduct;
import com.team6.onandthefarm.entity.order.Orders;
import com.team6.onandthefarm.vo.order.OrderProductGroupByProduct;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderProductRepository extends CrudRepository<OrderProduct,Long> {
    List<OrderProduct> findByOrders(Orders orders);

    List<OrderProduct> findByOrdersAndSellerIdAndOrderProductStatus(Orders orders, Long sellerId, String status);

    List<OrderProduct> findBySellerIdAndOrderProductStatus(Long sellerId, String orderStatus);

    List<OrderProduct> findBySellerId(Long sellerId);

    List<OrderProduct> findBySellerIdAndOrderProductDateBetween(Long sellerId, String startDate, String endDate);

    List<OrderProduct> findBySellerIdAndOrderProductDateStartingWith(Long sellerId, String date);

    List<OrderProduct> findByProductId(Long productId);

    List<OrderProduct> findOrderProductsByOrders(Orders orders);
}
