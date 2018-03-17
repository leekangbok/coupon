package io.pay.coupon.database;

import java.util.List;

import io.pay.coupon.database.model.Coupon;

public interface CouponDao {

	List<Coupon> getAll(long offset, long limit);

	long getTotalCount();
	
	DaoReturnCodes isExistCoupon(String email);

	DaoReturnCodes setCoupon(String email, String id);

}
