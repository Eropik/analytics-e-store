package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    
    Optional<PaymentMethod> findByMethodName(String methodName);
    
    boolean existsByMethodName(String methodName);
    
    @Query("SELECT pm FROM PaymentMethod pm ORDER BY pm.methodName ASC")
    List<PaymentMethod> findAllOrderByMethodNameAsc();
}
