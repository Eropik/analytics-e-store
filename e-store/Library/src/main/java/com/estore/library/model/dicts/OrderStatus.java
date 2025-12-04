package com.estore.library.model.dicts;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "order_status")
public class OrderStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "status_name", length = 50, nullable = false, unique = true)
    private String statusName;


}

