package com.estore.library.model.bisentity;

import com.estore.library.model.dicts.City;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "city_route")
@Data
public class CityRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer routeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_a_id", nullable = false)
    private City cityA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_b_id", nullable = false)
    private City cityB;

    @Column(name = "distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceKm;
}