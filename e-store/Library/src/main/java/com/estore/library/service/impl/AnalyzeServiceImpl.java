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

    @Override
    public ForecastDto getMonthlySalesForecast(Integer categoryId, int windowSize) {
        return analyzeRepository.getMonthlySalesForecast(categoryId, windowSize);
    }

    @Override
    public List<PieItemDto> getCategoryShare() {
        return analyzeRepository.getCategoryShare();
    }

    @Override
    public List<PieItemDto> getBrandShare() {
        return analyzeRepository.getBrandShare();
    }

    @Override
    public List<BucketItemDto> getPriceBuckets() {
        return analyzeRepository.getPriceBuckets();
    }

    @Override
    public List<PieItemDto> getTopCitiesInRoutes() {
        return analyzeRepository.getTopCitiesInRoutes();
    }

    @Override
    public List<BucketItemDto> getRouteDistanceBuckets() {
        return analyzeRepository.getRouteDistanceBuckets();
    }

    @Override
    public List<AgeBucketDto> getAgeBuckets5y() {
        return analyzeRepository.getAgeBuckets5y();
    }

    @Override
    public List<BucketItemDto> getLoginByHourLast30d() {
        return analyzeRepository.getLoginByHourLast30d();
    }

    @Override
    public List<PieItemDto> getTopBrands() {
        return analyzeRepository.getTopBrands();
    }

    @Override
    public List<PieItemDto> getTopCategories() {
        return analyzeRepository.getTopCategories();
    }

    @Override
    public List<PieItemDto> getTopProducts() {
        return analyzeRepository.getTopProducts();
    }

    @Override
    public List<TimeSeriesItemDto> getRevenueByMonthLastYear() {
        return analyzeRepository.getRevenueByMonthLastYear();
    }

    @Override
    public List<TimeSeriesItemDto> getBestsellersByMonth() {
        return analyzeRepository.getBestsellersByMonth();
    }

    @Override
    public List<PieItemDto> analyzeGeneric(String scope, String gender, String ageGroup, Integer month) {
        return analyzeRepository.analyzeGeneric(scope, gender, ageGroup, month);
    }

    @Override
    public List<PieItemDto> getOrderBrandsByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId) {
        return analyzeRepository.getOrderBrandsByFilter(status, gender, ageGroup, categoryId, brandId);
    }

    @Override
    public List<PieItemDto> getOrderCategoriesByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId) {
        return analyzeRepository.getOrderCategoriesByFilter(status, gender, ageGroup, categoryId, brandId);
    }

    @Override
    public List<PieItemDto> getOrderProductsByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId) {
        return analyzeRepository.getOrderProductsByFilter(status, gender, ageGroup, categoryId, brandId);
    }
}



