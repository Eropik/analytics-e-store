package com.estore.library.repository.analyze;

import com.estore.library.dto.analyze.dto.*;
import com.estore.library.dto.order.dto.OrderItemDto;
import com.estore.library.model.bisentity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.UUID;

@Repository
public class AnalyzeRepository {

    @PersistenceContext
    private EntityManager entityManager;



    public List<OrderItemDto> getOrderForAnalysis( Date startDate, Date endDate) {
        String jpql = """
            SELECT p.name, oi.unitPrice, oi.quantity
            FROM Order o 
            JOIN o.orderItems oi 
            JOIN oi.product  p
            WHERE o.orderDate BETWEEN :start AND :end 
            AND o.status.statusName <> 'CANCELLED'
            """;

        return entityManager.createQuery(jpql, OrderItemDto.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();



    }





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

    // ===== Product analytics =====
    public List<PieItemDto> getCategoryShare() {
        String jpql = """
            SELECT c.categoryName, COUNT(p)
            FROM Product p
            JOIN p.category c
            GROUP BY c.categoryName
            ORDER BY COUNT(p) DESC
            """;
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class).getResultList();
        List<PieItemDto> list = new ArrayList<>();
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            long cnt = ((Number) r[1]).longValue();
            dto.setValue(BigDecimal.valueOf(cnt));
            dto.setPercent(total > 0 ? BigDecimal.valueOf(cnt * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            list.add(dto);
        }
        return list;
    }

    public List<PieItemDto> getBrandShare() {
        String jpql = """
            SELECT b.brandName, COUNT(p)
            FROM Product p
            JOIN p.brand b
            GROUP BY b.brandName
            ORDER BY COUNT(p) DESC
            """;
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class).getResultList();
        List<PieItemDto> list = new ArrayList<>();
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            long cnt = ((Number) r[1]).longValue();
            dto.setValue(BigDecimal.valueOf(cnt));
            dto.setPercent(total > 0 ? BigDecimal.valueOf(cnt * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            list.add(dto);
        }
        return list;
    }

    public List<BucketItemDto> getPriceBuckets() {
        String sql = """
            SELECT bucket, COUNT(*)
            FROM (
              SELECT CASE
                     WHEN price < 100 THEN '0-100'
                     WHEN price < 500 THEN '100-500'
                     WHEN price < 1000 THEN '500-1000'
                     WHEN price < 2000 THEN '1000-2000'
                     WHEN price < 3000 THEN '2000-3000'
                     WHEN price < 4000 THEN '3000-4000'
                     WHEN price < 5000 THEN '4000-5000'
                     ELSE '5000+'
                 END AS bucket
              FROM product
            ) t
            GROUP BY bucket
            ORDER BY bucket
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<BucketItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            BucketItemDto dto = new BucketItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    public List<PieItemDto> getTopCitiesInRoutes() {
        String sql = """
            SELECT c.city_name, COUNT(cr.route_id) AS cnt
            FROM city_route cr
            JOIN city c ON c.city_id = cr.city_a_id
            GROUP BY c.city_name
            ORDER BY cnt DESC
            LIMIT 10
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<PieItemDto> list = new ArrayList<>();
        long total = rows.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            long cnt = ((Number) r[1]).longValue();
            dto.setValue(BigDecimal.valueOf(cnt));
            dto.setPercent(total > 0 ? BigDecimal.valueOf(cnt * 100.0 / total).setScale(2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            list.add(dto);
        }
        return list;
    }

    public List<BucketItemDto> getRouteDistanceBuckets() {
        String sql = """
            SELECT bucket, COUNT(*)
            FROM (
              SELECT CASE
                     WHEN distance_km < 50 THEN '0-50'
                     WHEN distance_km < 100 THEN '50-100'
                     WHEN distance_km < 150 THEN '100-150'
                     WHEN distance_km < 200 THEN '150-200'
                     WHEN distance_km < 300 THEN '200-300'
                     WHEN distance_km < 400 THEN '300-400'
                     ELSE '400+'
                 END AS bucket
              FROM city_route
            ) t
            GROUP BY bucket
            ORDER BY bucket
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<BucketItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            BucketItemDto dto = new BucketItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    // ===== User analytics =====
    public List<AgeBucketDto> getAgeBuckets5y() {

            String sql = """
            SELECT 
                CASE 
                    WHEN cp.date_of_birth IS NULL THEN 'Unknown'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) < 18 THEN '0-17'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 18 AND 24 THEN '18-24'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 25 AND 29 THEN '25-29'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 30 AND 34 THEN '30-34'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 35 AND 39 THEN '35-39'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 40 AND 44 THEN '40-44'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 45 AND 49 THEN '45-49'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 50 AND 54 THEN '50-54'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 55 AND 59 THEN '55-59'
                    ELSE '60+'
                END as age_bucket,
                COUNT(*) as users_count
            FROM customer_profile cp
            GROUP BY age_bucket
            ORDER BY age_bucket
            """;
            // ... остальной код метода остается без изменений
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<AgeBucketDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            AgeBucketDto dto = new AgeBucketDto();
            dto.setBucket((String) r[0]);
            dto.setCount(((Number) r[1]).longValue());
            list.add(dto);
        }
        return list;
    }

    public List<BucketItemDto> getLoginByHourLast30d() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        String sql = """
            SELECT EXTRACT(HOUR FROM logged_at) as hour, COUNT(*) 
            FROM login_log
            WHERE logged_at >= :from
            GROUP BY hour
            ORDER BY hour
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("from", from)
                .getResultList();
        List<BucketItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            BucketItemDto dto = new BucketItemDto();
            dto.setLabel(String.valueOf(((Number) r[0]).intValue()));
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    // ===== Order analytics =====
    public List<PieItemDto> getTopBrands() {
        String jpql = """
            SELECT p.brand.brandName, SUM(oi.quantity) as qty
            FROM OrderItem oi
            JOIN oi.order o
            JOIN oi.product p
            WHERE o.status.statusName = 'DELIVERED'
            GROUP BY p.brand.brandName
            ORDER BY qty DESC
            """;
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setMaxResults(10)
                .getResultList();
        List<PieItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    public List<PieItemDto> getTopCategories() {
        String jpql = """
            SELECT p.category.categoryName, SUM(oi.quantity) as qty
            FROM OrderItem oi
            JOIN oi.order o
            JOIN oi.product p
            WHERE o.status.statusName = 'DELIVERED'
            GROUP BY p.category.categoryName
            ORDER BY qty DESC
            """;
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setMaxResults(10)
                .getResultList();
        List<PieItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    public List<PieItemDto> getTopProducts() {
        String jpql = """
            SELECT p.name, SUM(oi.quantity) as qty
            FROM OrderItem oi
            JOIN oi.order o
            JOIN oi.product p
            WHERE o.status.statusName = 'DELIVERED'
            GROUP BY p.name
            ORDER BY qty DESC
            """;
        List<Object[]> rows = entityManager.createQuery(jpql, Object[].class)
                .setMaxResults(10)
                .getResultList();
        List<PieItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    public List<TimeSeriesItemDto> getRevenueByMonthLastYear() {
        String sql = """
            SELECT to_char(date_trunc('month', o.order_date), 'YYYY-MM') AS ym,
                   SUM(o.total_amount) as revenue
            FROM "order" o
            JOIN order_status os ON os.status_id = o.status_id
            WHERE os.status_name = 'DELIVERED'
              AND o.order_date >= (CURRENT_DATE - INTERVAL '12 months')
            GROUP BY ym
            ORDER BY ym
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<TimeSeriesItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            TimeSeriesItemDto dto = new TimeSeriesItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue((BigDecimal) r[1]);
            list.add(dto);
        }
        return list;
    }

    public List<TimeSeriesItemDto> getBestsellersByMonth() {
        String sql = """
            SELECT to_char(date_trunc('month', o.order_date), 'YYYY-MM') AS ym,
                   p.name,
                   SUM(oi.quantity) as qty
            FROM order_item oi
            JOIN "order" o ON o.order_id = oi.order_id
            JOIN product p ON p.product_id = oi.product_id
            JOIN order_status os ON os.status_id = o.status_id
            WHERE os.status_name = 'DELIVERED'
              AND o.order_date >= (CURRENT_DATE - INTERVAL '12 months')
            GROUP BY ym, p.name
            ORDER BY ym, qty DESC
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        List<TimeSeriesItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            TimeSeriesItemDto dto = new TimeSeriesItemDto();
            dto.setLabel((String) r[0] + " - " + r[1]);
            dto.setValue(BigDecimal.valueOf(((Number) r[2]).longValue()));
            list.add(dto);
        }
        return list;
    }

    // ===== Analyze generic =====
    public List<PieItemDto> analyzeGeneric(String scope, String gender, String ageGroup, Integer month) {
        // scope: products/categories/brands
        String base = switch (scope) {
            case "categories" -> "p.category.categoryName";
            case "brands" -> "p.brand.brandName";
            default -> "p.name";
        };

        StringBuilder jpql = new StringBuilder("""
            SELECT %s, SUM(oi.quantity) as qty
            FROM OrderItem oi
            JOIN oi.order o
            JOIN oi.product p
            JOIN o.user u
            LEFT JOIN u.customerProfile cp
            WHERE o.status.statusName = 'DELIVERED'
        """.formatted(base));

        if (gender != null && !gender.isBlank()) {
            jpql.append(" AND (cp.gender = :gender) ");
        }
        if (ageGroup != null && !ageGroup.isBlank()) {
            jpql.append("""
                AND (
                    CASE 
                        WHEN cp.dateOfBirth IS NULL THEN 'Unknown'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) < 18 THEN '0-17'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 18 AND 24 THEN '18-24'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 25 AND 29 THEN '25-29'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 30 AND 34 THEN '30-34'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 35 AND 39 THEN '35-39'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 40 AND 44 THEN '40-44'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 45 AND 49 THEN '45-49'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 50 AND 54 THEN '50-54'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 55 AND 59 THEN '55-59'
                        ELSE '60+'
                    END = :ageGroup
                )
            """);
        }
        if (month != null) {
            jpql.append(" AND EXTRACT(MONTH FROM o.orderDate) = :month ");
        }
        jpql.append(" GROUP BY %s ORDER BY qty DESC".formatted(base));

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        if (gender != null && !gender.isBlank()) query.setParameter("gender", gender);
        if (ageGroup != null && !ageGroup.isBlank()) query.setParameter("ageGroup", ageGroup);
        if (month != null) query.setParameter("month", month);

        List<Object[]> rows = query.getResultList();
        List<PieItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
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
        // Используем нативный SQL запрос, так как JPA не поддерживает AGE напряму
            String sql = """
            SELECT 
                CASE 
                    WHEN cp.date_of_birth IS NULL THEN 'Unknown'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 18 AND 25 THEN '18-25'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 26 AND 35 THEN '26-35'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 36 AND 45 THEN '36-45'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) BETWEEN 46 AND 60 THEN '46-60'
                    WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM cp.date_of_birth)) > 60 THEN '60+'
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
            // ... остальной код метода без изменений
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

    // ===== Order filtered by status/gender/age/category/brand =====
    private void appendAgeGroupFilter(StringBuilder sb) {
        sb.append("""
                AND (
                    CASE 
                        WHEN cp.dateOfBirth IS NULL THEN 'Unknown'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) < 18 THEN '0-17'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 18 AND 24 THEN '18-24'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 25 AND 29 THEN '25-29'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 30 AND 34 THEN '30-34'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 35 AND 39 THEN '35-39'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 40 AND 44 THEN '40-44'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 45 AND 49 THEN '45-49'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 50 AND 54 THEN '50-54'
                        WHEN (YEAR(CURRENT_DATE) - YEAR(cp.dateOfBirth)) BETWEEN 55 AND 59 THEN '55-59'
                        ELSE '60+'
                    END = :ageGroup
                )
            """);
    }

    private TypedQuery<Object[]> buildOrderFilterQuery(String selectLabel,
                                                       String status,
                                                       String gender,
                                                       String ageGroup,
                                                       Integer categoryId,
                                                       Integer brandId) {
        StringBuilder jpql = new StringBuilder("""
            SELECT %s, SUM(oi.quantity) as qty
            FROM OrderItem oi
            JOIN oi.order o
            JOIN oi.product p
            JOIN o.user u
            LEFT JOIN u.customerProfile cp
            WHERE 1=1
        """.formatted(selectLabel));

        if (status != null && !status.isBlank()) {
            jpql.append(" AND o.status.statusName = :status ");
        } else {
            jpql.append(" AND o.status.statusName IN ('PROCESSING','IN_TRANSIT','DELIVERED','CANCELLED') ");
        }
        if (gender != null && !gender.isBlank()) {
            jpql.append(" AND cp.gender = :gender ");
        }
        if (ageGroup != null && !ageGroup.isBlank()) {
            appendAgeGroupFilter(jpql);
        }
        if (categoryId != null) {
            jpql.append(" AND p.category.categoryId = :categoryId ");
        }
        if (brandId != null) {
            jpql.append(" AND p.brand.brandId = :brandId ");
        }

        jpql.append(" GROUP BY %s ORDER BY qty DESC".formatted(selectLabel));

        TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);
        if (status != null && !status.isBlank()) query.setParameter("status", status);
        if (gender != null && !gender.isBlank()) query.setParameter("gender", gender);
        if (ageGroup != null && !ageGroup.isBlank()) query.setParameter("ageGroup", ageGroup);
        if (categoryId != null) query.setParameter("categoryId", categoryId);
        if (brandId != null) query.setParameter("brandId", brandId);

        return query;
    }

    private List<PieItemDto> mapPie(List<Object[]> rows) {
        List<PieItemDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            PieItemDto dto = new PieItemDto();
            dto.setLabel((String) r[0]);
            dto.setValue(BigDecimal.valueOf(((Number) r[1]).longValue()));
            list.add(dto);
        }
        return list;
    }

    public List<PieItemDto> getOrderBrandsByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId) {
        var q = buildOrderFilterQuery("p.brand.brandName", status, gender, ageGroup, categoryId, brandId);
        return mapPie(q.getResultList());
    }

    public List<PieItemDto> getOrderCategoriesByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId) {
        var q = buildOrderFilterQuery("p.category.categoryName", status, gender, ageGroup, categoryId, brandId);
        return mapPie(q.getResultList());
    }

    public List<PieItemDto> getOrderProductsByFilter(String status, String gender, String ageGroup, Integer categoryId, Integer brandId) {
        var q = buildOrderFilterQuery("p.name", status, gender, ageGroup, categoryId, brandId);
        return mapPie(q.getResultList());
    }

    public TurnoverPlanReportDto buildTurnoverPlanReport(String metric, String startMonth, String endMonth, List<Double> plannedValues) {
        boolean byRevenue = !"volume".equalsIgnoreCase(metric);
        List<String> months = buildMonthRange(startMonth, endMonth);
        if (months.isEmpty()) {
            throw new IllegalArgumentException("Некорректный диапазон месяцев");
        }
        if (plannedValues == null || plannedValues.size() != months.size()) {
            throw new IllegalArgumentException("Количество плановых значений должно совпадать с количеством месяцев в периоде");
        }

        YearMonth from = YearMonth.parse(startMonth);
        YearMonth to = YearMonth.parse(endMonth);
        LocalDateTime fromTs = from.atDay(1).atStartOfDay();
        LocalDateTime toTs = to.atEndOfMonth().atTime(23, 59, 59);

        String valueExpr = byRevenue ? "SUM(oi.quantity * oi.unit_price)" : "SUM(oi.quantity)";
        String sql = """
                SELECT to_char(date_trunc('month', o.order_date), 'YYYY-MM') AS ym,
                       %s AS val
                FROM order_item oi
                JOIN "order" o ON o.order_id = oi.order_id
                JOIN order_status os ON os.status_id = o.status_id
                WHERE os.status_name = 'DELIVERED'
                  AND o.order_date >= :fromTs
                  AND o.order_date <= :toTs
                GROUP BY ym
                ORDER BY ym
                """.formatted(valueExpr);

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("fromTs", fromTs)
                .setParameter("toTs", toTs)
                .getResultList();

        Map<String, BigDecimal> actualByMonth = rows.stream().collect(Collectors.toMap(
                r -> (String) r[0],
                r -> toDecimal(r[1]),
                BigDecimal::add
        ));

        List<BigDecimal> actualValues = months.stream()
                .map(m -> actualByMonth.getOrDefault(m, BigDecimal.ZERO))
                .toList();
        BigDecimal totalActual = actualValues.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPlanned = plannedValues.stream().map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgActual = months.isEmpty()
                ? BigDecimal.ZERO
                : totalActual.divide(BigDecimal.valueOf(months.size()), 4, RoundingMode.HALF_UP);

        BigDecimal varianceSum = actualValues.stream()
                .map(v -> v.subtract(avgActual).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = months.isEmpty()
                ? BigDecimal.ZERO
                : varianceSum.divide(BigDecimal.valueOf(months.size()), 6, RoundingMode.HALF_UP);
        BigDecimal sigma = BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal variation = avgActual.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : sigma.divide(avgActual, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

        List<TurnoverPlanRowDto> reportRows = new ArrayList<>();
        List<TimeSeriesItemDto> executionSeries = new ArrayList<>();
        List<TimeSeriesItemDto> sigmaSeries = new ArrayList<>();
        List<TimeSeriesItemDto> variationSeries = new ArrayList<>();
        BigDecimal cumulativeSum = BigDecimal.ZERO;

        for (int i = 0; i < months.size(); i++) {
            String month = months.get(i);
            BigDecimal actual = actualValues.get(i);
            BigDecimal planned = BigDecimal.valueOf(plannedValues.get(i)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal execution = planned.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : actual.divide(planned, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

            cumulativeSum = cumulativeSum.add(actual);
            int sampleSize = i + 1;
            BigDecimal cumulativeAvg = cumulativeSum.divide(BigDecimal.valueOf(sampleSize), 10, RoundingMode.HALF_UP);
            BigDecimal cumulativeVarianceSum = BigDecimal.ZERO;
            for (int j = 0; j <= i; j++) {
                BigDecimal deviation = actualValues.get(j).subtract(cumulativeAvg);
                cumulativeVarianceSum = cumulativeVarianceSum.add(deviation.multiply(deviation));
            }
            BigDecimal cumulativeVariance = cumulativeVarianceSum.divide(BigDecimal.valueOf(sampleSize), 10, RoundingMode.HALF_UP);
            BigDecimal cumulativeSigma = BigDecimal.valueOf(Math.sqrt(cumulativeVariance.doubleValue())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cumulativeVariation = cumulativeAvg.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : cumulativeSigma.divide(cumulativeAvg, 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            TurnoverPlanRowDto row = new TurnoverPlanRowDto();
            row.setMonth(month);
            row.setActualTurnover(actual.setScale(2, RoundingMode.HALF_UP));
            row.setPlannedTurnover(planned);
            row.setPlanExecutionPercent(execution);
            row.setSigma(sigma);
            row.setVariationPercent(variation);
            row.setCumulativeSigma(cumulativeSigma);
            row.setCumulativeVariationPercent(cumulativeVariation);
            reportRows.add(row);

            TimeSeriesItemDto execTs = new TimeSeriesItemDto();
            execTs.setLabel(month);
            execTs.setValue(execution);
            executionSeries.add(execTs);

            TimeSeriesItemDto sigmaTs = new TimeSeriesItemDto();
            sigmaTs.setLabel(month);
            sigmaTs.setValue(cumulativeSigma);
            sigmaSeries.add(sigmaTs);

            TimeSeriesItemDto varTs = new TimeSeriesItemDto();
            varTs.setLabel(month);
            varTs.setValue(cumulativeVariation);
            variationSeries.add(varTs);
        }

        TurnoverPlanReportDto dto = new TurnoverPlanReportDto();
        dto.setMetric(byRevenue ? "revenue" : "volume");
        dto.setStartMonth(startMonth);
        dto.setEndMonth(endMonth);
        dto.setRows(reportRows);
        dto.setTotalActual(totalActual.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalPlanned(totalPlanned.setScale(2, RoundingMode.HALF_UP));
        dto.setAvgActual(avgActual.setScale(2, RoundingMode.HALF_UP));
        dto.setSigma(sigma);
        dto.setVariationPercent(variation);
        dto.setExecutionSeries(executionSeries);
        dto.setSigmaSeries(sigmaSeries);
        dto.setVariationSeries(variationSeries);
        return dto;
    }

    public GroupShareReportDto buildGroupShareReport(String metric, String groupBy, String startMonth, String endMonth) {
        boolean byRevenue = !"volume".equalsIgnoreCase(metric);
        boolean byCategory = !"brand".equalsIgnoreCase(groupBy);

        YearMonth from = YearMonth.parse(startMonth);
        YearMonth to = YearMonth.parse(endMonth);
        LocalDateTime fromTs = from.atDay(1).atStartOfDay();
        LocalDateTime toTs = to.atEndOfMonth().atTime(23, 59, 59);

        String labelExpr = byCategory ? "c.category_name" : "b.brand_name";
        String joinExpr = byCategory
                ? "JOIN category c ON c.category_id = p.category_id"
                : "JOIN brand b ON b.brand_id = p.brand_id";
        String valueExpr = byRevenue ? "SUM(oi.quantity * oi.unit_price)" : "SUM(oi.quantity)";

        String totalSql = """
                SELECT %s AS grp,
                       %s AS val
                FROM order_item oi
                JOIN "order" o ON o.order_id = oi.order_id
                JOIN order_status os ON os.status_id = o.status_id
                JOIN product p ON p.product_id = oi.product_id
                %s
                WHERE os.status_name = 'DELIVERED'
                  AND o.order_date >= :fromTs
                  AND o.order_date <= :toTs
                GROUP BY grp
                ORDER BY val DESC
                """.formatted(labelExpr, valueExpr, joinExpr);

        List<Object[]> totalRowsRaw = entityManager.createNativeQuery(totalSql)
                .setParameter("fromTs", fromTs)
                .setParameter("toTs", toTs)
                .getResultList();

        BigDecimal totalTurnover = totalRowsRaw.stream()
                .map(r -> toDecimal(r[1]))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GroupShareRowDto> rows = new ArrayList<>();
        for (Object[] raw : totalRowsRaw) {
            GroupShareRowDto row = new GroupShareRowDto();
            row.setGroupName(raw[0] == null ? "Без группы" : String.valueOf(raw[0]));
            row.setGroupTurnover(toDecimal(raw[1]).setScale(2, RoundingMode.HALF_UP));
            row.setTotalTurnover(totalTurnover.setScale(2, RoundingMode.HALF_UP));
            BigDecimal share = totalTurnover.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : row.getGroupTurnover().divide(totalTurnover, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            row.setSharePercent(share);
            rows.add(row);
        }

        List<String> topGroups = rows.stream().limit(5).map(GroupShareRowDto::getGroupName).toList();
        if (topGroups.isEmpty()) {
            GroupShareReportDto empty = new GroupShareReportDto();
            empty.setMetric(byRevenue ? "revenue" : "volume");
            empty.setGroupBy(byCategory ? "category" : "brand");
            empty.setStartMonth(startMonth);
            empty.setEndMonth(endMonth);
            empty.setRows(List.of());
            empty.setDynamics(List.of());
            return empty;
        }

        String dynamicsSql = """
                SELECT to_char(date_trunc('month', o.order_date), 'YYYY-MM') AS ym,
                       %s AS grp,
                       %s AS val
                FROM order_item oi
                JOIN "order" o ON o.order_id = oi.order_id
                JOIN order_status os ON os.status_id = o.status_id
                JOIN product p ON p.product_id = oi.product_id
                %s
                WHERE os.status_name = 'DELIVERED'
                  AND o.order_date >= :fromTs
                  AND o.order_date <= :toTs
                GROUP BY ym, grp
                ORDER BY ym
                """.formatted(labelExpr, valueExpr, joinExpr);
        List<Object[]> dynRaw = entityManager.createNativeQuery(dynamicsSql)
                .setParameter("fromTs", fromTs)
                .setParameter("toTs", toTs)
                .getResultList();

        Map<String, BigDecimal> monthTotals = new LinkedHashMap<>();
        Map<String, Map<String, BigDecimal>> monthByGroup = new LinkedHashMap<>();
        for (Object[] raw : dynRaw) {
            String month = String.valueOf(raw[0]);
            String group = raw[1] == null ? "Без группы" : String.valueOf(raw[1]);
            BigDecimal val = toDecimal(raw[2]);
            monthTotals.put(month, monthTotals.getOrDefault(month, BigDecimal.ZERO).add(val));
            monthByGroup.computeIfAbsent(month, k -> new LinkedHashMap<>())
                    .put(group, monthByGroup.getOrDefault(month, new LinkedHashMap<>()).getOrDefault(group, BigDecimal.ZERO).add(val));
        }

        List<String> months = buildMonthRange(startMonth, endMonth);
        List<Map<String, Object>> dynamics = new ArrayList<>();
        for (String month : months) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month);
            BigDecimal monthTotal = monthTotals.getOrDefault(month, BigDecimal.ZERO);
            Map<String, BigDecimal> groupsMap = monthByGroup.getOrDefault(month, Map.of());
            for (String group : topGroups) {
                BigDecimal groupVal = groupsMap.getOrDefault(group, BigDecimal.ZERO);
                BigDecimal share = monthTotal.compareTo(BigDecimal.ZERO) == 0
                        ? BigDecimal.ZERO
                        : groupVal.divide(monthTotal, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                row.put(group, share);
            }
            dynamics.add(row);
        }

        GroupShareReportDto dto = new GroupShareReportDto();
        dto.setMetric(byRevenue ? "revenue" : "volume");
        dto.setGroupBy(byCategory ? "category" : "brand");
        dto.setStartMonth(startMonth);
        dto.setEndMonth(endMonth);
        dto.setRows(rows);
        dto.setDynamics(dynamics);
        return dto;
    }

    private List<String> buildMonthRange(String startMonth, String endMonth) {
        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        if (end.isBefore(start)) return List.of();
        List<String> months = new ArrayList<>();
        YearMonth cursor = start;
        while (!cursor.isAfter(end)) {
            months.add(cursor.toString());
            cursor = cursor.plusMonths(1);
        }
        return months;
    }

    private BigDecimal toDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}

