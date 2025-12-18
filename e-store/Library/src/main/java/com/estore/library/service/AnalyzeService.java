package com.estore.library.service;

import com.estore.library.dto.analyze.dto.*;

import java.util.List;

public interface AnalyzeService {
    /**
     * Получить топ товаров по объему продаж
     */
    List<BestSellerDto> getBestSellers(int limit);

    /**
     * Получить анализ по категориям и брендам
     */
    List<CategoryBrandAnalysisDto> getCategoryBrandAnalysis();

    /**
     * Получить анализ по возрастным группам покупателей
     */
    List<AgeGroupAnalysisDto> getAgeGroupAnalysis();

    /**
     * Получить анализ по маршрутам доставки
     */
    List<RouteAnalysisDto> getRouteAnalysis();

    /**
     * Получить анализ по способам оплаты
     */
    List<PaymentDeliveryAnalysisDto> getPaymentMethodAnalysis();

    /**
     * Получить анализ по способам доставки
     */
    List<PaymentDeliveryAnalysisDto> getDeliveryMethodAnalysis();

    /**
     * Прогноз продаж по месяцам по категории
     */
    ForecastDto getMonthlySalesForecast(Integer categoryId, int windowSize);

    // Product analytics
    List<PieItemDto> getCategoryShare();
    List<PieItemDto> getBrandShare();
    List<BucketItemDto> getPriceBuckets();
    List<PieItemDto> getTopCitiesInRoutes();
    List<BucketItemDto> getRouteDistanceBuckets();

    // User analytics
    List<AgeBucketDto> getAgeBuckets5y();
    List<BucketItemDto> getLoginByHourLast30d();

    // Order analytics
    List<PieItemDto> getTopBrands();
    List<PieItemDto> getTopCategories();
    List<PieItemDto> getTopProducts();
    List<TimeSeriesItemDto> getRevenueByMonthLastYear();
    List<TimeSeriesItemDto> getBestsellersByMonth();

    // Analyze combined
    List<PieItemDto> analyzeGeneric(String scope, String gender, String ageGroup, Integer month);

    // Order filtered by status/gender/age/category/brand
    List<PieItemDto> getOrderBrandsByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId);
    List<PieItemDto> getOrderCategoriesByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId);
    List<PieItemDto> getOrderProductsByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId);
}



