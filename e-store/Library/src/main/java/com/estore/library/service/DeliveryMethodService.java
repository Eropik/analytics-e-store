package com.estore.library.service;

import com.estore.library.model.dicts.DeliveryMethod;

import java.util.List;
import java.util.Optional;

public interface DeliveryMethodService {
    
    DeliveryMethod createDeliveryMethod(DeliveryMethod deliveryMethod);
    
    DeliveryMethod updateDeliveryMethod(Integer methodId, DeliveryMethod deliveryMethod);
    
    void deleteDeliveryMethod(Integer methodId);
    
    Optional<DeliveryMethod> getDeliveryMethodById(Integer methodId);
    
    Optional<DeliveryMethod> getDeliveryMethodByName(String methodName);
    
    List<DeliveryMethod> getAllDeliveryMethods();
    
    boolean existsByName(String methodName);
}
