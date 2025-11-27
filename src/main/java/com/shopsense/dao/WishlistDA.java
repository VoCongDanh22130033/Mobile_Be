package com.shopsense.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.shopsense.db;
import com.shopsense.dto.WishlistDetail;

@Service
public class WishlistDA {
	PreparedStatement pst;

	public List<WishlistDetail> findAllByCustomerId(int customerId) {
		List<WishlistDetail> l = new ArrayList<>();
		try {
			pst = db.get().prepareStatement(
					"SELECT w.customer_id, w.product_id, p.title, p.thumbnail_url, p.sale_price, p.stock_status " +
							"FROM wishlist w LEFT JOIN products p ON w.product_id = p.id " +
							"WHERE w.customer_id = ?"
			);
			pst.setInt(1, customerId);

			pst.setInt(1, customerId);
			ResultSet rs = pst.executeQuery();
			WishlistDetail w;
			while (rs.next()) {
				w = new WishlistDetail();
				w.setCustomerId(rs.getInt(1));
				w.setProductId(rs.getInt(2));
				w.setTitle(rs.getString(3));
				w.setThumbnailUrl(rs.getString(4));
				w.setSalePrice(rs.getInt(5));
				w.setStockStatus(rs.getString(6));
				l.add(w);
				System.out.println("Wishlist row: customerId=" + customerId
						+ ", productId=" + w.getProductId()
						+ ", title=" + (w.getTitle() == null ? "NULL" : w.getTitle())
						+ ", thumbnailUrl=" + (w.getThumbnailUrl() == null ? "NULL" : w.getThumbnailUrl() )
						+ ", salePrice=" + w.getSalePrice()
						+ ", stockStatus=" + (w.getStockStatus() == null ? "NULL" : w.getStockStatus()));

			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return l;
	}
}
