package com.estore.library.model.dicts;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admin_department")
@Data
public class AdminDepartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer departmentId;

    @Column(name = "department_name", nullable = false, unique = true, length = 50)
    private String departmentName;
}