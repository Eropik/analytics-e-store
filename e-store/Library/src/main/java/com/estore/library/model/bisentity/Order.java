package com.estore.library.model.bisentity;
import com.estore.library.model.dicts.City;
import com.estore.library.model.dicts.DeliveryMethod;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.model.dicts.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"order\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Column(name = "order_id")
    private UUID id = UUID.randomUUID();  // Генерация UUID по умолчанию

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate = new Date();  // По умолчанию текущая дата

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "shipping_city_id")
    private City shippingCity;

    @Column(name = "shipping_address_text")
    private String shippingAddressText;

    @ManyToOne
    @JoinColumn(name = "delivery_method_id")
    private DeliveryMethod deliveryMethod;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "discount_applied")
    private Double discountApplied = 0.00;

    @Column(name = "actual_delivery_date")
    @Temporal(TemporalType.DATE)
    private Date actualDeliveryDate;

    @ManyToOne
    @JoinColumn(name = "source_warehouse_id")
    private Warehouse sourceWarehouse;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public UUID getId() {
        return id;
    }
}
