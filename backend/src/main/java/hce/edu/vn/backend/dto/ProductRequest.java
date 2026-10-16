package hce.edu.vn.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@JsonIgnoreProperties("ProductID")
public record ProductRequest(
		@JsonProperty("ProductName") @NotBlank @Size(max = 40) String productName,
		@JsonProperty("SupplierID") @Positive Integer supplierId,
		@JsonProperty("CategoryID") @Positive Integer categoryId,
		@JsonProperty("QuantityPerUnit") @Size(max = 20) String quantityPerUnit,
		@JsonProperty("UnitPrice") @DecimalMin("0.0") @Digits(integer = 15, fraction = 4) BigDecimal unitPrice,
		@JsonProperty("UnitsInStock") @PositiveOrZero Short unitsInStock,
		@JsonProperty("UnitsOnOrder") @PositiveOrZero Short unitsOnOrder,
		@JsonProperty("ReorderLevel") @PositiveOrZero Short reorderLevel,
		@JsonProperty("Discontinued") Boolean discontinued) {
}