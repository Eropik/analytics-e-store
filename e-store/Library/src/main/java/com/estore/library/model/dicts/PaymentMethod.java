package com.estore.library.model.dicts;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payment_method")
@Data
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer methodId;

    @Column(name = "method_name", nullable = false, unique = true, length = 50)
    private String methodName;

    @Column(name = "description")
    private String description;
}