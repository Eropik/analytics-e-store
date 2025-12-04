package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);



    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status.statusName = :status")
    Page<Order> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") String status,
            Pageable pageable
    );
    
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Page<Order> findByOrderDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    Page<Order> findByTotalAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.shippingCity.cityId = :cityId")
    Page<Order> findByShippingCityId(@Param("cityId") Integer cityId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.deliveryMethod.methodId = :methodId")
    Page<Order> findByDeliveryMethodId(@Param("methodId") Integer methodId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.paymentMethod.methodId = :methodId")
    Page<Order> findByPaymentMethodId(@Param("methodId") Integer methodId, Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    Long countByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.userId = :userId")
    BigDecimal sumTotalAmountByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.orderDate DESC")
    Page<Order> findByUserIdOrderByOrderDateDesc(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findByStatusIn(@Param("statuses") List<String> statuses, Pageable pageable);

    // 🚀 НОВЫЙ МЕТОД: для getOrdersByDateRangeAndStatus
    Page<Order> findByOrderDateBetweenAndStatus_StatusId(LocalDateTime startDate, LocalDateTime endDate, Integer statusId, Pageable pageable);


    Page<Order> findByStatus_StatusId(Integer statusId, Pageable pageable); // Если в Order есть поле private OrderStatus status;
    Page<Order> findByStatus_StatusIdIn(List<Integer> statusIds, Pageable pageable);

    // OrderRepository (Исправленная версия)
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.user.userId = :userId AND o.status.statusId = :statusId")
    Long countByUserIdAndStatus_StatusId(
            @Param("userId") UUID userId,
            @Param("statusId") Integer statusId
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.user.userId = :userId AND o.status.statusId = :statusId")
    BigDecimal sumTotalAmountByUserIdAndStatus_StatusId(
            @Param("userId") UUID userId,
            @Param("statusId") Integer statusId
    );
    // OrderRepository (Альтернатива через @Query)
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status.statusId = :statusId")
    Page findByUserIdAndStatus_StatusId(
            @Param("userId") UUID userId,
            @Param("statusId") Integer statusId,
            Pageable pageable
    );

}