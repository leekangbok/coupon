package io.pay.coupon.database.factory;

import io.pay.coupon.database.CouponGenDao;
import io.pay.coupon.database.CouponDao;
import io.pay.coupon.database.PojoMapCouponGenDao;
import io.pay.coupon.database.PojoMapCouponDao;

public class PojoMapDaoFactory extends DaoFactory {

	@Override
	public CouponDao getCouponDao() {
		return new PojoMapCouponDao();
	}

	@Override
	public CouponGenDao getCouponGenDao() {
		return new PojoMapCouponGenDao();
	}

}
