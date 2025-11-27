package com.shopsense.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.shopsense.db;
import com.shopsense.model.Category;
import com.shopsense.model.Product;

public class CategoryDA {
	PreparedStatement pst;

	// ✅ Lấy danh sách thể loại
	public List<Category> getCategories() {
		List<Category> list = new ArrayList<>();
		try {
			pst = db.get().prepareStatement("SELECT id, title, description, icon, parent_id FROM categories");
			ResultSet rs = pst.executeQuery();
			Category p;
			while (rs.next()) {
				p = new Category();
				p.setId(rs.getInt(1));
				p.setTitle(rs.getString(2));
				p.setDescription(rs.getString(3));
				p.setIcon(rs.getString(4));
				p.setParent(rs.getInt(5));
				list.add(p);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return list;
	}

	// ✅ Thêm mới category
	public Category create(Category c) {
		try {
			pst = db.get().prepareStatement(
					"INSERT INTO categories (title, description, icon, parent_id) VALUES (?, ?, ?, ?)");
			pst.setString(1, c.getTitle());
			pst.setString(2, c.getDescription());
			pst.setString(3, c.getIcon());
			pst.setInt(4, c.getParent());
			int x = pst.executeUpdate();
			if (x != -1) {
				return c;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	// ✅ Cập nhật category
	public boolean update(Category c) {
		try {
			pst = db.get().prepareStatement(
					"UPDATE categories SET title = ?, description = ?, icon = ?, parent_id = ? WHERE id = ?");
			pst.setString(1, c.getTitle());
			pst.setString(2, c.getDescription());
			pst.setString(3, c.getIcon());
			pst.setInt(4, c.getParent());
			pst.setInt(5, c.getId());
			int x = pst.executeUpdate();
			if (x != -1) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}

	// ✅ Xóa category
	public boolean delete(int id) {
		try {
			pst = db.get().prepareStatement("DELETE FROM categories WHERE id = ?");
			pst.setInt(1, id);
			int x = pst.executeUpdate();
			if (x != -1) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}

	// ✅ Lấy danh sách sản phẩm theo category_id
	public List<Product> getProductsByCategory(int categoryId, int offset, int limit) {
		List<Product> list = new ArrayList<>();
		try {
			pst = db.get().prepareStatement(
					"SELECT p.id AS product_id, p.title AS product_title, p.description, " +
							"p.sale_price, p.regular_price, p.thumbnail_url, p.category_id " +
							"FROM products p " +
							"WHERE p.category_id = ? " +
							"LIMIT ?, ?"
			);
			pst.setInt(1, categoryId);
			pst.setInt(2, offset);
			pst.setInt(3, limit);
			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				Product p = new Product();
				p.setId(rs.getInt("product_id"));
				p.setTitle(rs.getString("product_title"));
				p.setDescription(rs.getString("description"));
				p.setSalePrice(String.valueOf(rs.getDouble("sale_price")));
				p.setRegularPrice(String.valueOf(rs.getDouble("regular_price")));
				p.setThumbnailUrl(rs.getString("thumbnail_url"));
				p.setCategoryId(rs.getInt("category_id"));
				list.add(p);
			}

		} catch (Exception e) {
			System.out.println("Lỗi getProductsByCategory: " + e.getMessage());
		}
		return list;
	}


}
