package com.estore.library.service;

import com.estore.library.dto.analyze.dto.*;
import com.estore.library.dto.order.dto.OrderItemDto;
import com.estore.library.model.bisentity.Order;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.util.Date;
import java.util.List;

public interface AnalyzeService {


    List<OrderItemDto> getAnalyze(Date startDate, Date endDate);



    List<BestSellerDto> getBestSellers(int limit);


    List<CategoryBrandAnalysisDto> getCategoryBrandAnalysis();


    List<AgeGroupAnalysisDto> getAgeGroupAnalysis();

    List<RouteAnalysisDto> getRouteAnalysis();


    List<PaymentDeliveryAnalysisDto> getPaymentMethodAnalysis();


    List<PaymentDeliveryAnalysisDto> getDeliveryMethodAnalysis();


    ForecastDto getMonthlySalesForecast(Integer categoryId, int windowSize);

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

    TurnoverPlanReportDto buildTurnoverPlanReport(String metric, String startMonth, String endMonth, List<Double> plannedValues);

    GroupShareReportDto buildGroupShareReport(String metric, String groupBy, String startMonth, String endMonth);
}



