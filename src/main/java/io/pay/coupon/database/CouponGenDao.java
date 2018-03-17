package io.pay.coupon.database;

public interface CouponGenDao {

	DaoReturnCodes setCouponId(String couponId);

	DaoReturnCodes delCouponId(String couponId);
}
