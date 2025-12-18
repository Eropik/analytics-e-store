package com.estore.library.repository.analyze;

import com.estore.library.dto.analyze.dto.*;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                     WHEN price < 5000 THEN '1000-5000'
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
                     WHEN distance_km < 10 THEN '0-10'
                     WHEN distance_km < 50 THEN '10-50'
                     WHEN distance_km < 200 THEN '50-200'
                     ELSE '200+'
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
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) < 18 THEN '0-17'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 18 AND 24 THEN '18-24'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 25 AND 29 THEN '25-29'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 30 AND 34 THEN '30-34'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 35 AND 39 THEN '35-39'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 40 AND 44 THEN '40-44'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 45 AND 49 THEN '45-49'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 50 AND 54 THEN '50-54'
                    WHEN EXTRACT(YEAR FROM AGE(cp.date_of_birth)) BETWEEN 55 AND 59 THEN '55-59'
                    ELSE '60+'
                END as age_bucket,
                COUNT(*) as users_count
            FROM customer_profile cp
            GROUP BY age_bucket
            ORDER BY age_bucket
            """;
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
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) < 18 THEN '0-17'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 18 AND 24 THEN '18-24'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 25 AND 29 THEN '25-29'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 30 AND 34 THEN '30-34'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 35 AND 39 THEN '35-39'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 40 AND 44 THEN '40-44'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 45 AND 49 THEN '45-49'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 50 AND 54 THEN '50-54'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 55 AND 59 THEN '55-59'
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

    // ===== Order filtered by status/gender/age/category/brand =====
    private void appendAgeGroupFilter(StringBuilder sb) {
        sb.append("""
                AND (
                    CASE 
                        WHEN cp.dateOfBirth IS NULL THEN 'Unknown'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) < 18 THEN '0-17'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 18 AND 24 THEN '18-24'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 25 AND 29 THEN '25-29'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 30 AND 34 THEN '30-34'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 35 AND 39 THEN '35-39'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 40 AND 44 THEN '40-44'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 45 AND 49 THEN '45-49'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 50 AND 54 THEN '50-54'
                        WHEN EXTRACT(YEAR FROM AGE(cp.dateOfBirth)) BETWEEN 55 AND 59 THEN '55-59'
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
}

