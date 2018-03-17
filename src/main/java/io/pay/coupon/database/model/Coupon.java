package io.pay.coupon.database.model;

public class Coupon {

	public final String email;
	public final String couponId;
	public final String date;

	public Coupon(String email, String couponId, String date) {
		this.email = email;
		this.couponId = couponId;
		this.date = date;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == this)
			return true;
		if (!(arg0 instanceof Coupon))
			return false;
		Coupon coupon = (Coupon) arg0;
		return email.equals(coupon.email);
	}

	@Override
	public int hashCode() {
		return email.hashCode();
	}

}
