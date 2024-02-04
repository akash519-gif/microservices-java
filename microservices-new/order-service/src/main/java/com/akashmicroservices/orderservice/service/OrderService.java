package com.akashmicroservices.orderservice.service;

import com.akashmicroservices.orderservice.dto.InventoryResponse;
import com.akashmicroservices.orderservice.dto.OrderLineItemsDto;
import com.akashmicroservices.orderservice.dto.OrderRequest;
import com.akashmicroservices.orderservice.model.Order;
import com.akashmicroservices.orderservice.model.OrderLineItems;
import com.akashmicroservices.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(String.valueOf(UUID.randomUUID()));
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        // Call Inventory service and place order if product is in stock

        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);

        if (allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in Stock");
        }

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItemsObject = new OrderLineItems();
        orderLineItemsObject.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItemsObject.setPrice(orderLineItemsDto.getPrice());
        orderLineItemsObject.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItemsObject;
    }
}
