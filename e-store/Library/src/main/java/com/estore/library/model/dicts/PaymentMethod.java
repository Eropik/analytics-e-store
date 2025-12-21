package com.estore.library.model.dicts;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "payment_method")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer methodId;

    @Column(name = "method_name", nullable = false, unique = true, length = 50)
    private String methodName;

    @Column(name = "description")
    private String description;
}