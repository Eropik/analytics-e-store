package com.estore.library.service.impl;

import com.estore.library.dto.analyze.dto.*;
import com.estore.library.repository.analyze.AnalyzeRepository;
import com.estore.library.service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyzeServiceImpl implements AnalyzeService {

    private final AnalyzeRepository analyzeRepository;

    @Override
    public List<BestSellerDto> getBestSellers(int limit) {
        return analyzeRepository.getBestSellers(limit);
    }

    @Override
    public List<CategoryBrandAnalysisDto> getCategoryBrandAnalysis() {
        return analyzeRepository.getCategoryBrandAnalysis();
    }

    @Override
    public List<AgeGroupAnalysisDto> getAgeGroupAnalysis() {
        return analyzeRepository.getAgeGroupAnalysis();
    }

    @Override
    public List<RouteAnalysisDto> getRouteAnalysis() {
        return analyzeRepository.getRouteAnalysis();
    }

    @Override
    public List<PaymentDeliveryAnalysisDto> getPaymentMethodAnalysis() {
        return analyzeRepository.getPaymentMethodAnalysis();
    }

    @Override
    public List<PaymentDeliveryAnalysisDto> getDeliveryMethodAnalysis() {
        return analyzeRepository.getDeliveryMethodAnalysis();
    }
}


