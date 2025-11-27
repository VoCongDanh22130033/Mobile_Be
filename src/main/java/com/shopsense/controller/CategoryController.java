package com.shopsense.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsense.dao.CategoryDA;
import com.shopsense.model.Category;
import com.shopsense.model.Product;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
public class CategoryController {

	CategoryDA da = new CategoryDA();

	// ✅ Lấy tất cả category
	@GetMapping(value = "/category/all")
	public List<Category> getCategories() {
		return da.getCategories();
	}

	// ✅ Thêm category
	@PostMapping(value = "/category")
	public Category create(@RequestBody Category c) {
		return da.create(c);
	}

	// ✅ Cập nhật category
	@PutMapping(value = "/category")
	public boolean update(@RequestBody Category c) {
		return da.update(c);
	}

	// ✅ Xóa category
	@DeleteMapping(value = "/category")
	public boolean delete(@RequestParam int id) {
		return da.delete(id);
	}

	// ✅ Lấy danh sách sản phẩm theo category_id
	@GetMapping(value = "/category/products")
	public List<Product> getProductsByCategory(
			@RequestParam int categoryId,
			@RequestParam int page,
			@RequestParam int pageSize) {

		int offset = (page - 1) * pageSize;
		return da.getProductsByCategory(categoryId, offset, pageSize);
	}

}
