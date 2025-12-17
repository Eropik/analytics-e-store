package com.estore.library.repository.analyze;

import com.estore.library.dto.analyze.dto.*;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class AnalyzeRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Топ товаров по объему продаж (количество и сумма)
     */
    public List<BestSellerDto> getBestSellers(int limit) {
        String jpql = """
            SELECT oi.product.productId, oi.product.name, 
                   SUM(oi.quantity) as totalQuantity,
                   SUM(oi.quantity * oi.unitPrice) as totalRevenue,
                   oi.product.averageRating
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status.statusName = 'DELIVERED'
            GROUP BY oi.product.productId, oi.product.name, oi.product.averageRating
            ORDER BY totalQuantity DESC, totalRevenue DESC
            """;
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setMaxResults(limit);
        
        List<Object[]> results = query.getResultList();
        List<BestSellerDto> bestSellers = new ArrayList<>();
        
        for (Object[] row : results) {
            BestSellerDto dto = new BestSellerDto();
            dto.setProductId((UUID) row[0]);
            dto.setProductName((String) row[1]);
            dto.setTotalQuantitySold(((Number) row[2]).longValue());
            dto.setTotalRevenue((BigDecimal) row[3]);
            dto.setAverageRating((BigDecimal) row[4]);
            bestSellers.add(dto);
        }
        
        return bestSellers;
    }

    /**
     * Анализ по категориям и брендам
     */
    public List<CategoryBrandAnalysisDto> getCategoryBrandAnalysis() {
        String jpql = """
            SELECT p.category.categoryId, p.category.categoryName,
                   p.brand.brandId, p.brand.brandName,
                   COUNT(DISTINCT o.id) as ordersCount,
                   SUM(oi.quantity) as unitsSold,
                   SUM(oi.quantity * oi.unitPrice) as totalRevenue
            FROM OrderItem oi
            JOIN oi.order o
            JOIN oi.product p
            WHERE o.status.statusName = 'DELIVERED'
            GROUP BY p.category.categoryId, p.category.categoryName,
                     p.brand.brandId, p.brand.brandName
            ORDER BY totalRevenue DESC
            """;
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> results = query.getResultList();
        List<CategoryBrandAnalysisDto> analysis = new ArrayList<>();
        
        for (Object[] row : results) {
            CategoryBrandAnalysisDto dto = new CategoryBrandAnalysisDto();
            dto.setCategoryId((Integer) row[0]);
            dto.setCategoryName((String) row[1]);
            dto.setBrandId((Integer) row[2]);
            dto.setBrandName((String) row[3]);
            dto.setOrdersCount(((Number) row[4]).longValue());
            dto.setUnitsSold(((Number) row[5]).longValue());
            dto.setTotalRevenue((BigDecimal) row[6]);
            analysis.add(dto);
        }
        
        return analysis;
    }

    /**
     * Анализ по возрастным категориям покупателей
     */
    public List<AgeGroupAnalysisDto> getAgeGroupAnalysis() {
        // Используем нативный SQL запрос, так как JPA не поддерживает AGE напрямую
        String sql = """
            SELECT 
                CASE 
                    WHEN cp.date_of_birth IS NULL THEN 'Unknown'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 18 AND 25 THEN '18-25'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 26 AND 35 THEN '26-35'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 36 AND 45 THEN '36-45'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 46 AND 60 THEN '46-60'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) > 60 THEN '60+'
                    ELSE 'Unknown'
                END as age_group,
                COUNT(DISTINCT cp.user_id) as customers_count,
                COUNT(DISTINCT o.order_id) as orders_count,
                COALESCE(SUM(o.total_amount), 0) as total_revenue
            FROM "order" o
            JOIN "user" u ON o.user_id = u.user_id
            LEFT JOIN customer_profile cp ON cp.user_id = u.user_id
            JOIN order_status os ON o.status_id = os.status_id
            WHERE os.status_name = 'DELIVERED'
            GROUP BY age_group
            ORDER BY age_group
            """;
        
        jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        List<AgeGroupAnalysisDto> analysis = new ArrayList<>();
        
        for (Object[] row : results) {
            AgeGroupAnalysisDto dto = new AgeGroupAnalysisDto();
            dto.setAgeGroup((String) row[0]);
            dto.setCustomersCount(((Number) row[1]).longValue());
            dto.setOrdersCount(((Number) row[2]).longValue());
            dto.setTotalRevenue((BigDecimal) row[3]);
            analysis.add(dto);
        }
        
        return analysis;
    }

    /**
     * Анализ по маршрутам (города доставки)
     */
    public List<RouteAnalysisDto> getRouteAnalysis() {
        String jpql = """
            SELECT o.shippingCity.cityId, o.shippingCity.cityName,
                   COUNT(o.id) as ordersCount,
                   SUM(o.totalAmount) as totalRevenue
            FROM Order o
            WHERE o.status.statusName = 'DELIVERED'
              AND o.shippingCity IS NOT NULL
            GROUP BY o.shippingCity.cityId, o.shippingCity.cityName
            ORDER BY totalRevenue DESC
            """;
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> results = query.getResultList();
        List<RouteAnalysisDto> analysis = new ArrayList<>();
        
        for (Object[] row : results) {
            RouteAnalysisDto dto = new RouteAnalysisDto();
            dto.setCityId((Integer) row[0]);
            dto.setCityName((String) row[1]);
            dto.setOrdersCount(((Number) row[2]).longValue());
            dto.setTotalRevenue((BigDecimal) row[3]);
            analysis.add(dto);
        }
        
        return analysis;
    }

    /**
     * Анализ по способам оплаты
     */
    public List<PaymentDeliveryAnalysisDto> getPaymentMethodAnalysis() {
        String jpql = """
            SELECT o.paymentMethod.methodId, o.paymentMethod.methodName,
                   COUNT(o.id) as ordersCount,
                   SUM(o.totalAmount) as totalRevenue
            FROM Order o
            WHERE o.status.statusName = 'DELIVERED'
              AND o.paymentMethod IS NOT NULL
            GROUP BY o.paymentMethod.methodId, o.paymentMethod.methodName
            ORDER BY totalRevenue DESC
            """;
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> results = query.getResultList();
        List<PaymentDeliveryAnalysisDto> analysis = new ArrayList<>();
        
        for (Object[] row : results) {
            PaymentDeliveryAnalysisDto dto = new PaymentDeliveryAnalysisDto();
            dto.setMethodId((Integer) row[0]);
            dto.setMethodName((String) row[1]);
            dto.setMethodType("PAYMENT");
            dto.setOrdersCount(((Number) row[2]).longValue());
            dto.setTotalRevenue((BigDecimal) row[3]);
            analysis.add(dto);
        }
        
        return analysis;
    }

    /**
     * Анализ по способам доставки
     */
    public List<PaymentDeliveryAnalysisDto> getDeliveryMethodAnalysis() {
        String jpql = """
            SELECT o.deliveryMethod.methodId, o.deliveryMethod.methodName,
                   COUNT(o.id) as ordersCount,
                   SUM(o.totalAmount) as totalRevenue
            FROM Order o
            WHERE o.status.statusName = 'DELIVERED'
              AND o.deliveryMethod IS NOT NULL
            GROUP BY o.deliveryMethod.methodId, o.deliveryMethod.methodName
            ORDER BY totalRevenue DESC
            """;
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        List<Object[]> results = query.getResultList();
        List<PaymentDeliveryAnalysisDto> analysis = new ArrayList<>();
        
        for (Object[] row : results) {
            PaymentDeliveryAnalysisDto dto = new PaymentDeliveryAnalysisDto();
            dto.setMethodId((Integer) row[0]);
            dto.setMethodName((String) row[1]);
            dto.setMethodType("DELIVERY");
            dto.setOrdersCount(((Number) row[2]).longValue());
            dto.setTotalRevenue((BigDecimal) row[3]);
            analysis.add(dto);
        }
        
        return analysis;
    }


    /**
     * Прогнозирование продаж по месяцам по категории товаров
     */
    public ForecastDto getMonthlySalesForecast(Integer categoryId, int windowSize) {

        String sql = """
        SELECT 
            DATE_TRUNC('month', o.order_date) AS month,
            SUM(oi.quantity * oi.unit_price) AS revenue
        FROM order_item oi
        JOIN "order" o ON oi.order_id = o.order_id
        JOIN product p ON oi.product_id = p.product_id
        JOIN order_status os ON o.status_id = os.status_id
        WHERE os.status_name = 'DELIVERED'
          AND p.category_id = :categoryId
        GROUP BY month
        ORDER BY month
        """;

        jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
        query.setParameter("categoryId", categoryId);

        List<Object[]> rows = query.getResultList();

        List<BigDecimal> monthlySales = new ArrayList<>();
        for (Object[] row : rows) {
            monthlySales.add((BigDecimal) row[1]);
        }

        // Расчет скользящего среднего
        List<BigDecimal> movingAvg = new ArrayList<>();

        for (int i = 0; i < monthlySales.size(); i++) {
            if (i + 1 < windowSize) {
                movingAvg.add(null); // для первых месяцев нет MA
            } else {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - windowSize + 1; j <= i; j++) {
                    sum = sum.add(monthlySales.get(j));
                }
                movingAvg.add(sum.divide(BigDecimal.valueOf(windowSize), 2, BigDecimal.ROUND_HALF_UP));
            }
        }

        // Прогноз = последнее скользящее среднее
        BigDecimal forecast = movingAvg.get(movingAvg.size() - 1);

        ForecastDto dto = new ForecastDto();
        dto.setMonthlySales(monthlySales);
        dto.setMovingAverage(movingAvg);
        dto.setForecast(forecast);

        return dto;
    }

}

