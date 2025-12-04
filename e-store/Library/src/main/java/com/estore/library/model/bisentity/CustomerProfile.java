package com.estore.library.model.bisentity;
import com.estore.library.model.dicts.City;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;



@Entity
@Table(name = "customer_profile")
@Data
public class CustomerProfile {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "total_spent", precision = 15, scale = 2)
    private BigDecimal totalSpent;

    @Column(name = "orders_count")
    private Integer ordersCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
}