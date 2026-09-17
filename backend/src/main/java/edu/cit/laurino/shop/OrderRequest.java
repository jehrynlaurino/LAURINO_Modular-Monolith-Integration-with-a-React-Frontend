package edu.cit.laurino.shop;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "items must contain at least one line item")
        @Valid
        List<OrderItemRequest> items) {
}
