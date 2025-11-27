package com.shopsense.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id") // khớp với DB
	private int wishlistId;

	@Column(name = "customer_id") // khớp với DB
	private int customerId;

	@Column(name = "product_id") // khớp với DB
	private int productId;

	@Column(name = "added_date", insertable = false, updatable = false)
	private LocalDateTime addedDate; // mặc định DB sẽ set current_timestamp()
}
