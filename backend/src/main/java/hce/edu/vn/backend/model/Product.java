package hce.edu.vn.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record Product(
		@JsonProperty("ProductID") int productId,
		@JsonProperty("ProductName") String productName,
		@JsonProperty("SupplierID") Integer supplierId,
		@JsonProperty("CategoryID") Integer categoryId,
		@JsonProperty("QuantityPerUnit") String quantityPerUnit,
		@JsonProperty("UnitPrice") BigDecimal unitPrice,
		@JsonProperty("UnitsInStock") Short unitsInStock,
		@JsonProperty("UnitsOnOrder") Short unitsOnOrder,
		@JsonProperty("ReorderLevel") Short reorderLevel,
		@JsonProperty("Discontinued") boolean discontinued) {
}