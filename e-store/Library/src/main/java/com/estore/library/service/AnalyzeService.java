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
}



