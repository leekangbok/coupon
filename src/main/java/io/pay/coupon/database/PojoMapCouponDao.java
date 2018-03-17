package io.pay.coupon.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pay.coupon.database.model.Coupon;

public class PojoMapCouponDao implements CouponDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(PojoMapCouponDao.class);

	private static final Map<String, Coupon> coupons = new HashMap<>();
	private static final List<String> emails = new LinkedList<>();

	@Override
	public List<Coupon> getAll(long offset, long limit) {
		List<Coupon> items = new LinkedList<>();

		long len = emails.size();
		if (offset >= len) {
			return items;
		}
		limit = limit + offset;
		if (limit > len) {
			limit = len;
		}

		for (; offset < limit; offset = offset + 1) {
			items.add(coupons.get(emails.get((int) offset)));
		}

		return items;
	}

	@Override
	public DaoReturnCodes setCoupon(String email, String id) {
		if (coupons.containsKey(email)) {
			return DaoReturnCodes.EXISTS;
		}

		Date from = new Date();
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String to = transFormat.format(from);
		coupons.put(email, new Coupon(email, id, to));
		emails.add(0, email);
		return DaoReturnCodes.SUCCESS;
	}

	@Override
	public DaoReturnCodes isExistCoupon(String email) {
		if (coupons.containsKey(email)) {
			return DaoReturnCodes.EXISTS;
		}
		return DaoReturnCodes.NEW;
	}

	@Override
	public long getTotalCount() {
		return emails.size();
	}
}
