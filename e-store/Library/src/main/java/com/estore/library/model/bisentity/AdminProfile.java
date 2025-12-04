package com.estore.library.model.bisentity;

import com.estore.library.model.dicts.AdminDepartment;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "admin_profile")
@Data
public class AdminProfile {
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

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private AdminDepartment department; // Ссылка на справочник

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
}