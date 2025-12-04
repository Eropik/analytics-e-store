package com.estore.library.service.impl;

import com.estore.library.model.dicts.PaymentMethod;
import com.estore.library.repository.dicts.PaymentMethodRepository;
import com.estore.library.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodServiceImpl implements PaymentMethodService {
    
    private final PaymentMethodRepository paymentMethodRepository;
    
    @Override
    @Transactional
    public PaymentMethod createPaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethodRepository.existsByMethodName(paymentMethod.getMethodName())) {
            throw new IllegalArgumentException("Payment method already exists: " + paymentMethod.getMethodName());
        }
        return paymentMethodRepository.save(paymentMethod);
    }
    
    @Override
    @Transactional
    public PaymentMethod updatePaymentMethod(Integer methodId, PaymentMethod paymentMethod) {
        PaymentMethod existing = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found with id: " + methodId));
        
        if (!existing.getMethodName().equals(paymentMethod.getMethodName()) && 
            paymentMethodRepository.existsByMethodName(paymentMethod.getMethodName())) {
            throw new IllegalArgumentException("Payment method name already exists");
        }
        
        existing.setMethodName(paymentMethod.getMethodName());
        existing.setDescription(paymentMethod.getDescription());
        
        return paymentMethodRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deletePaymentMethod(Integer methodId) {
        if (!paymentMethodRepository.existsById(methodId)) {
            throw new IllegalArgumentException("Payment method not found with id: " + methodId);
        }
        paymentMethodRepository.deleteById(methodId);
    }
    
    @Override
    public Optional<PaymentMethod> getPaymentMethodById(Integer methodId) {
        return paymentMethodRepository.findById(methodId);
    }
    
    @Override
    public Optional<PaymentMethod> getPaymentMethodByName(String methodName) {
        return paymentMethodRepository.findByMethodName(methodName);
    }
    
    @Override
    public List<PaymentMethod> getAllPaymentMethods() {
        return paymentMethodRepository.findAllOrderByMethodNameAsc();
    }
    
    @Override
    public boolean existsByName(String methodName) {
        return paymentMethodRepository.existsByMethodName(methodName);
    }
}
