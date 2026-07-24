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
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {




    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);



    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate AND o.orderDate <= :endDate")
    Page<Order> findByOrderDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    Page<Order> findByOrderDateBetweenAndStatus_StatusId(LocalDateTime startDate, LocalDateTime endDate, Integer statusId, Pageable pageable);


    Page<Order> findByStatus_StatusId(Integer statusId, Pageable pageable); // Если в Order есть поле private OrderStatus status;

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

    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId AND o.status.statusId = :statusId")
    Page findByUserIdAndStatus_StatusId(
            @Param("userId") UUID userId,
            @Param("statusId") Integer statusId,
            Pageable pageable
    );

}