package com.estore.library.model.dicts;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "brand")
@Data
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer brandId;

    @Column(name = "brand_name", nullable = false, unique = true, length = 100)
    private String brandName;
}