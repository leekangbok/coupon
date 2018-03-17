package io.pay.coupon.database.factory;

import io.pay.coupon.database.CouponGenDao;
import io.pay.coupon.database.CouponDao;
import io.pay.coupon.database.DatabaseType;

public abstract class DaoFactory {

	public abstract CouponDao getCouponDao();

	public abstract CouponGenDao getCouponGenDao();

	public static DaoFactory getDaoFactory(DatabaseType type) {
		switch (type) {
		case POJOMAP:
			return new PojoMapDaoFactory();

		default:
			return null;
		}
	}
}
