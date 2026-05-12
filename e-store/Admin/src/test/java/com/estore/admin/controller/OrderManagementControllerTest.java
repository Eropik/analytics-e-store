package com.estore.admin.controller;

import com.estore.library.model.bisentity.Order;
import com.estore.library.model.bisentity.Warehouse;
import com.estore.library.model.dicts.City;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.CityRouteService;
import com.estore.library.service.OrderItemService;
import com.estore.library.service.OrderService;
import com.estore.library.service.UserService;
import com.estore.library.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderManagementControllerTest {

    @Mock
    private AdminProfileService adminProfileService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private OrderStatusRepository orderStatusRepository;
    @Mock
    private UserService userService;
    @Mock
    private WarehouseService warehouseService;
    @Mock
    private CityRouteService cityRouteService;

    private OrderManagementController controller;

    @BeforeEach
    void setUp() {
        controller = new OrderManagementController(
                adminProfileService,
                orderService,
                orderItemService,
                orderStatusRepository,
                userService,
                warehouseService,
                cityRouteService
        );
    }

    @Test
    void updateOrderStatus_shouldReturnForbidden_whenAccessDenied() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(adminProfileService.hasOrderManagementAccess(adminId)).thenReturn(false);

        ResponseEntity<?> response = controller.updateOrderStatus(adminId, orderId, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateOrderStatus_shouldReturnBadRequest_whenStatusNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(adminProfileService.hasOrderManagementAccess(adminId)).thenReturn(true);
        when(orderStatusRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateOrderStatus(adminId, orderId, 999);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateLogistics_shouldUpdateOrder_whenInputsAreValid() {
        UUID adminId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Long warehouseId = 10L;

        Order order = new Order();
        order.setId(orderId);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(warehouseId);
        warehouse.setName("WH-1");
        City city = new City();
        city.setCityId(1);
        city.setCityName("Moscow");
        warehouse.setCity(city);

        OrderStatus status = new OrderStatus();
        status.setStatusId(2);
        status.setStatusName("IN_TRANSIT");

        when(adminProfileService.hasOrderManagementAccess(adminId)).thenReturn(true);
        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));
        when(warehouseService.getWarehouseById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(orderStatusRepository.findById(2)).thenReturn(Optional.of(status));

        ResponseEntity<?> response = controller.updateLogistics(
                adminId, orderId, warehouseId, LocalDate.of(2026, 5, 12), 2
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(order.getSourceWarehouse()).isEqualTo(warehouse);
        assertThat(order.getStatus()).isEqualTo(status);
        assertThat(order.getActualDeliveryDate()).isEqualTo(Date.valueOf(LocalDate.of(2026, 5, 12)));
        verify(orderService).updateOrder(orderId, order);
        assertThat(((Map<?, ?>) response.getBody()).get("success")).isEqualTo(true);
    }
}
