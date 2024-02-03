package com.akashmicroservices.orderservice.service;

import com.akashmicroservices.orderservice.dto.OrderLineItemsDto;
import com.akashmicroservices.orderservice.dto.OrderRequest;
import com.akashmicroservices.orderservice.model.Order;
import com.akashmicroservices.orderservice.model.OrderLineItems;
import com.akashmicroservices.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(String.valueOf(UUID.randomUUID()));
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItemsObject = new OrderLineItems();
        orderLineItemsObject.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItemsObject.setPrice(orderLineItemsDto.getPrice());
        orderLineItemsObject.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItemsObject;
    }
}
