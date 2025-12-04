package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.DeliveryMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryMethodRepository extends JpaRepository<DeliveryMethod, Integer> {
    
    Optional<DeliveryMethod> findByMethodName(String methodName);
    
    boolean existsByMethodName(String methodName);
    
    @Query("SELECT dm FROM DeliveryMethod dm ORDER BY dm.methodName ASC")
    List<DeliveryMethod> findAllOrderByMethodNameAsc();
}
