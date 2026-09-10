package edu.cit.laurino.shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/seed-test.sql", executionPhase = BEFORE_TEST_METHOD)
class OrderApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void confirmsOrderAndReturnsRemainingInventory() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"P100\",\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.reason").value("Inventory reserved."))
                .andExpect(jsonPath("$.inventory.productId").value("P100"))
                .andExpect(jsonPath("$.inventory.stock").value(22));
    }

    @Test
    void rejectsOrderWhenStockIsInsufficient() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"P300\",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.reason").value(
                        "Only 0 unit(s) of USB-C Hub remain."))
                .andExpect(jsonPath("$.inventory.productId").value("P300"))
                .andExpect(jsonPath("$.inventory.stock").value(0));
    }
}
