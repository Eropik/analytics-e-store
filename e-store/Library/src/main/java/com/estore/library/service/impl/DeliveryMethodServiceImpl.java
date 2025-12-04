package com.estore.library.service.impl;

import com.estore.library.model.dicts.DeliveryMethod;
import com.estore.library.repository.dicts.DeliveryMethodRepository;
import com.estore.library.service.DeliveryMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryMethodServiceImpl implements DeliveryMethodService {
    
    private final DeliveryMethodRepository deliveryMethodRepository;
    
    @Override
    @Transactional
    public DeliveryMethod createDeliveryMethod(DeliveryMethod deliveryMethod) {
        if (deliveryMethodRepository.existsByMethodName(deliveryMethod.getMethodName())) {
            throw new IllegalArgumentException("Delivery method already exists: " + deliveryMethod.getMethodName());
        }
        return deliveryMethodRepository.save(deliveryMethod);
    }
    
    @Override
    @Transactional
    public DeliveryMethod updateDeliveryMethod(Integer methodId, DeliveryMethod deliveryMethod) {
        DeliveryMethod existing = deliveryMethodRepository.findById(methodId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery method not found with id: " + methodId));
        
        if (!existing.getMethodName().equals(deliveryMethod.getMethodName()) && 
            deliveryMethodRepository.existsByMethodName(deliveryMethod.getMethodName())) {
            throw new IllegalArgumentException("Delivery method name already exists");
        }
        
        existing.setMethodName(deliveryMethod.getMethodName());
        existing.setDescription(deliveryMethod.getDescription());
        
        return deliveryMethodRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteDeliveryMethod(Integer methodId) {
        if (!deliveryMethodRepository.existsById(methodId)) {
            throw new IllegalArgumentException("Delivery method not found with id: " + methodId);
        }
        deliveryMethodRepository.deleteById(methodId);
    }
    
    @Override
    public Optional<DeliveryMethod> getDeliveryMethodById(Integer methodId) {
        return deliveryMethodRepository.findById(methodId);
    }
    
    @Override
    public Optional<DeliveryMethod> getDeliveryMethodByName(String methodName) {
        return deliveryMethodRepository.findByMethodName(methodName);
    }
    
    @Override
    public List<DeliveryMethod> getAllDeliveryMethods() {
        return deliveryMethodRepository.findAllOrderByMethodNameAsc();
    }
    
    @Override
    public boolean existsByName(String methodName) {
        return deliveryMethodRepository.existsByMethodName(methodName);
    }
}
