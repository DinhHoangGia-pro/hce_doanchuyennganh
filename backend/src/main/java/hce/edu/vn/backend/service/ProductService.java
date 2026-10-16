package hce.edu.vn.backend.service;

import hce.edu.vn.backend.dto.ProductRequest;
import hce.edu.vn.backend.exception.ApiException;
import hce.edu.vn.backend.model.Product;
import hce.edu.vn.backend.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public List<Product> findAll() {
		return productRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Product findById(int productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm."));
	}

	@Transactional
	public Product create(ProductRequest request) {
		validateReferences(request);
		return productRepository.insert(toProduct(request, 0));
	}

	@Transactional
	public Product update(int productId, ProductRequest request) {
		findById(productId);
		validateReferences(request);
		if (productRepository.update(productId, toProduct(request, productId)) == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm.");
		}
		return findById(productId);
	}

	@Transactional
	public void delete(int productId) {
		findById(productId);
		try {
			if (productRepository.deleteById(productId) == 0) {
				throw new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm.");
			}
		} catch (DataIntegrityViolationException exception) {
			throw new ApiException(HttpStatus.CONFLICT,
					"Không thể xóa sản phẩm vì sản phẩm đang được sử dụng trong dữ liệu khác.");
		}
	}

	private void validateReferences(ProductRequest request) {
		if (request.supplierId() != null && !productRepository.supplierExists(request.supplierId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "SupplierID không tồn tại.");
		}
		if (request.categoryId() != null && !productRepository.categoryExists(request.categoryId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "CategoryID không tồn tại.");
		}
	}

	private Product toProduct(ProductRequest request, int productId) {
		return new Product(
				productId,
				request.productName(),
				request.supplierId(),
				request.categoryId(),
				request.quantityPerUnit(),
				request.unitPrice() == null ? BigDecimal.ZERO : request.unitPrice(),
				request.unitsInStock() == null ? (short) 0 : request.unitsInStock(),
				request.unitsOnOrder() == null ? (short) 0 : request.unitsOnOrder(),
				request.reorderLevel() == null ? (short) 0 : request.reorderLevel(),
				Boolean.TRUE.equals(request.discontinued()));
	}
}