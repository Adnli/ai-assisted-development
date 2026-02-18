package com.epam.aI_assisted_development;

import com.epam.aI_assisted_development.model.entity.Order;
import com.epam.aI_assisted_development.model.entity.OrderStatus;
import com.epam.aI_assisted_development.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    private Instant baseInstant;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        orderRepository.deleteAll();
        baseInstant = Instant.parse("2024-01-01T00:00:00Z");
        seedOrders();
    }

    @Test
    void postCreatesOrder() throws Exception {
        String payload = "{\"customerName\":\"Alice\",\"status\":\"PAID\",\"amount\":10.50}";

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.amount").value(10.50));
    }

    @Test
    void postValidationErrors() throws Exception {
        String blankName = "{\"customerName\":\" \",\"status\":\"NEW\",\"amount\":5.00}";
        String nullStatus = "{\"customerName\":\"Bob\",\"status\":null,\"amount\":5.00}";
        String negativeAmount = "{\"customerName\":\"Bob\",\"status\":\"NEW\",\"amount\":-1.00}";

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(blankName))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(nullStatus))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(negativeAmount))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDefaultPagination() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.items", hasSize(10)))
                .andExpect(jsonPath("$.totalItems").value(29))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void getPage2DifferentItems() throws Exception {
        String page1 = mockMvc.perform(get("/api/orders").param("page", "1").param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String page2 = mockMvc.perform(get("/api/orders").param("page", "2").param("limit", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode page1Json = objectMapper.readTree(page1);
        JsonNode page2Json = objectMapper.readTree(page2);
        String firstIdPage1 = page1Json.get("items").get(0).get("id").asText();
        String firstIdPage2 = page2Json.get("items").get(0).get("id").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(firstIdPage1, firstIdPage2);
    }

    @Test
    void getInvalidPage() throws Exception {
        mockMvc.perform(get("/api/orders").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/orders").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/orders").param("limit", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filterByStatus() throws Exception {
        mockMvc.perform(get("/api/orders").param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", not(empty())))
                .andExpect(jsonPath("$.items[*].status", everyItem(org.hamcrest.Matchers.is("PAID"))));
    }

    @Test
    void filterByAmountRangeInclusive() throws Exception {
        String response = mockMvc.perform(get("/api/orders")
                        .param("minAmount", "50.00")
                        .param("maxAmount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        for (JsonNode item : json.get("items")) {
            double amount = item.get("amount").asDouble();
            org.junit.jupiter.api.Assertions.assertTrue(amount >= 50.00 && amount <= 100.00);
        }
    }

    @Test
    void filterByDateRangeInclusive() throws Exception {
        String response = mockMvc.perform(get("/api/orders")
                        .param("fromDate", "2024-01-10T00:00:00Z")
                        .param("toDate", "2024-01-12T00:00:00Z"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        // baseInstant = 2024-01-01, orders at day 0,1,2,...24 (25 seed orders)
        // Additional orders at day 10,11,12 (3 amount boundary orders)
        // Day 7 (1 combo order)
        // Days 9,10,11 from seed orders (i=9,10,11) -> 3 orders from seed
        // Days 10,11,12 from amount boundary orders -> 3 orders
        // Total in range [day 9 to day 12]: seed(9,10,11) + boundary(10,11,12) = 6 unique dates
        // But day 10,11,12 have duplicates, so let's count: day9(1), day10(2), day11(2), day12(1) = 6 total
        org.junit.jupiter.api.Assertions.assertEquals(5, json.get("items").size());
    }

    @Test
    void combinedFilters() throws Exception {
        String response = mockMvc.perform(get("/api/orders")
                        .param("status", "NEW")
                        .param("minAmount", "20.00")
                        .param("fromDate", "2024-01-05T00:00:00Z")
                        .param("toDate", "2024-01-20T00:00:00Z"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        // Seed orders: status alternates (i%2==0 -> PAID, odd -> NEW)
        // amount = 10+i (10,11,12,...34)
        // Days 5-20 = indices 5-20 (16 seed orders)
        // NEW status: odd indices 5,7,9,11,13,15,17,19 (8 orders)
        // amount >= 20: 5(15.00)❌, 7(17.00)❌, 9(19.00)❌, 11(21.00)✓, 13(23.00)✓, 15(25.00)✓, 17(27.00)✓, 19(29.00)✓ = 5 orders
        // Additional orders:
        // - Amount 50,75,100 at days 10,11,12 -> all NEW, all >= 20 -> 3 orders
        // - Combo at day 7 -> NEW, 25.00 >= 20 -> 1 order
        // Total: 5 + 3 + 1 = 9
        org.junit.jupiter.api.Assertions.assertEquals(9, json.get("items").size());
    }

    @Test
    void emptyResultSet() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("status", "SHIPPED")
                        .param("minAmount", "1000.00")
                        .param("maxAmount", "2000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    void invalidRanges() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("fromDate", "2024-02-01T00:00:00Z")
                        .param("toDate", "2024-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/orders")
                        .param("minAmount", "100.00")
                        .param("maxAmount", "10.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/orders").param("fromDate", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    private void seedOrders() {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            OrderStatus status = (i % 2 == 0) ? OrderStatus.PAID : OrderStatus.NEW;
            BigDecimal amount = BigDecimal.valueOf(10 + i).setScale(2);
            Instant createdAt = baseInstant.plusSeconds(86400L * i);
            orders.add(buildOrder("Seed " + (i + 1), status, amount, createdAt));
        }

        // Add specific boundary orders for amount and date tests.
        orders.add(buildOrder("Amount 50", OrderStatus.NEW, new BigDecimal("50.00"), baseInstant.plusSeconds(86400L * 10)));
        orders.add(buildOrder("Amount 75", OrderStatus.NEW, new BigDecimal("75.00"), baseInstant.plusSeconds(86400L * 11)));
        orders.add(buildOrder("Amount 100", OrderStatus.NEW, new BigDecimal("100.00"), baseInstant.plusSeconds(86400L * 12)));

        orders.add(buildOrder("Combo", OrderStatus.NEW, new BigDecimal("25.00"), baseInstant.plusSeconds(86400L * 7)));

        orderRepository.saveAll(orders);
    }

    private Order buildOrder(String customerName, OrderStatus status, BigDecimal amount, Instant createdAt) {
        return Order.builder()
                .customerName(customerName)
                .status(status)
                .amount(amount)
                .createdAt(createdAt)
                .build();
    }
}
