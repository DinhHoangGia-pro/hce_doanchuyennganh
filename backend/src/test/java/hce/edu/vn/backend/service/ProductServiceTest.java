package hce.edu.vn.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import hce.edu.vn.backend.dto.ProductRequest;
import hce.edu.vn.backend.exception.ApiException;
import hce.edu.vn.backend.model.Product;
import hce.edu.vn.backend.repository.ProductRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	void createChecksReferencesAndUsesDatabaseDefaultsForOmittedFields() {
		ProductRequest request = request("Vietnam Coffee", 1, 1);
		when(productRepository.supplierExists(1)).thenReturn(true);
		when(productRepository.categoryExists(1)).thenReturn(true);
		when(productRepository.insert(any(Product.class))).thenAnswer(invocation -> {
			Product product = invocation.getArgument(0);
			return new Product(100, product.productName(), product.supplierId(), product.categoryId(),
					product.quantityPerUnit(), product.unitPrice(), product.unitsInStock(), product.unitsOnOrder(),
					product.reorderLevel(), product.discontinued());
		});

		Product created = productService.create(request);

		ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
		verify(productRepository).insert(productCaptor.capture());
		assertEquals(100, created.productId());
		assertEquals(BigDecimal.ZERO, productCaptor.getValue().unitPrice());
		assertEquals((short) 0, productCaptor.getValue().unitsInStock());
		assertTrue(productRepository.supplierExists(1));
		assertTrue(productRepository.categoryExists(1));
	}

	@Test
	void createRejectsUnknownSupplier() {
		when(productRepository.supplierExists(999)).thenReturn(false);

		ApiException exception = assertThrows(ApiException.class,
				() -> productService.create(request("Vietnam Coffee", 999, null)));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
		verify(productRepository, never()).insert(any(Product.class));
	}

	@Test
	void findByIdReturnsNotFoundWhenProductDoesNotExist() {
		when(productRepository.findById(999)).thenReturn(java.util.Optional.empty());

		ApiException exception = assertThrows(ApiException.class, () -> productService.findById(999));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
	}

	@Test
	void deleteReturnsConflictWhenAnotherTableReferencesProduct() {
		when(productRepository.findById(10)).thenReturn(java.util.Optional.of(product(10)));
		when(productRepository.deleteById(10)).thenThrow(new DataIntegrityViolationException("database detail"));

		ApiException exception = assertThrows(ApiException.class, () -> productService.delete(10));

		assertEquals(HttpStatus.CONFLICT, exception.getStatus());
		assertTrue(exception.getMessage().contains("đang được sử dụng"));
	}

	@Test
	void requestValidationEnforcesSchemaLengthsAndNonNegativeValues() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		ProductRequest invalid = new ProductRequest(" ".repeat(41), null, null, "x".repeat(21),
				new BigDecimal("-1.00000"), (short) -1, (short) 0, (short) 0, false);

		assertTrue(validator.validate(invalid).size() >= 5);
	}

	private ProductRequest request(String name, Integer supplierId, Integer categoryId) {
		return new ProductRequest(name, supplierId, categoryId, "20 bags x 500 g", null, null, null, null, null);
	}

	private Product product(int id) {
		return new Product(id, "Sample", null, null, null, BigDecimal.ZERO,
				(short) 0, (short) 0, (short) 0, false);
	}
}