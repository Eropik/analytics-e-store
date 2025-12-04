package com.estore.library.service;

import com.estore.library.model.dicts.PaymentMethod;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodService {
    
    PaymentMethod createPaymentMethod(PaymentMethod paymentMethod);
    
    PaymentMethod updatePaymentMethod(Integer methodId, PaymentMethod paymentMethod);
    
    void deletePaymentMethod(Integer methodId);
    
    Optional<PaymentMethod> getPaymentMethodById(Integer methodId);
    
    Optional<PaymentMethod> getPaymentMethodByName(String methodName);
    
    List<PaymentMethod> getAllPaymentMethods();
    
    boolean existsByName(String methodName);
}
