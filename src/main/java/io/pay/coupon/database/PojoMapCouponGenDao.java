package io.pay.coupon.database;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PojoMapCouponGenDao implements CouponGenDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(PojoMapCouponGenDao.class);

	private static final Set<String> coupons = new HashSet<>();

	@Override
	public DaoReturnCodes setCouponId(String couponId) {
		if (!coupons.add(couponId)) {
			LOGGER.error("dup coupon id");
			return DaoReturnCodes.EXISTS;
		}
		return DaoReturnCodes.NEW;
	}

	@Override
	public DaoReturnCodes delCouponId(String couponId) {
		coupons.remove(couponId);
		return DaoReturnCodes.SUCCESS;
	}

}
