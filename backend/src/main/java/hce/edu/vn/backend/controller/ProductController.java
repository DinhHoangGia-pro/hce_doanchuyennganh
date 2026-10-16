package hce.edu.vn.backend.controller;

import hce.edu.vn.backend.dto.ProductRequest;
import hce.edu.vn.backend.model.Product;
import hce.edu.vn.backend.service.ProductService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/danhsachsanpham")
public class ProductController {
	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<Product> findAll() {
		return productService.findAll();
	}

	@GetMapping("/{id}")
	public Product findById(@PathVariable int id) {
		return productService.findById(id);
	}

	@PostMapping
	public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
		Product product = productService.create(request);
		return ResponseEntity.created(URI.create("/api/danhsachsanpham/" + product.productId())).body(product);
	}

	@PutMapping("/{id}")
	public Product update(@PathVariable int id, @Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}
}