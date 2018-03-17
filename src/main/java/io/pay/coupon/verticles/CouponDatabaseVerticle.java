package io.pay.coupon.verticles;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pay.coupon.database.CouponDao;
import io.pay.coupon.database.DaoReturnCodes;
import io.pay.coupon.database.DatabaseType;
import io.pay.coupon.database.factory.DaoFactory;
import io.pay.coupon.database.model.Coupon;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CouponDatabaseVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CouponDatabaseVerticle.class);

	private CouponDao coupons;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		coupons = DaoFactory
				.getDaoFactory(DatabaseType.valueOf(
						config().getString("couponDaoFactory", DatabaseType.POJOMAP.name())))
				.getCouponDao();

		String queue = config().getString("couponDatabaseQueue", "coupon.database.queue");
		vertx.eventBus().consumer(queue, this::onMessage);

		LOGGER.info("Coupon Email Database Verticle running on " + queue);
		startFuture.complete();
	}

	public void onMessage(Message<JsonObject> message) {
		if (!message.headers().contains("action")) {
			LOGGER.error("No action");
			message.fail(ErrorCodes.NO_ACTION.ordinal(), "No action");
			return;
		}

		String action = message.headers().get("action");
		switch (action) {
		case "get-all":
			getAll(message);
			break;
		case "is-exist-coupon":
			isExistCoupon(message);
			break;
		case "set-coupon":
			setCoupon(message);
			break;
		default:
			LOGGER.error("Unknown action: " + action);
			message.fail(ErrorCodes.UNKNOWN_ACTION.ordinal(), "Unknown action: " + action);
		}
	}

	private void getAll(Message<JsonObject> message) {
		List<Coupon> items = coupons.getAll(message.body().getLong("offset", (long) 0),
				message.body().getLong("limit", (long) 40));

		JsonArray array = new JsonArray();

		for (Coupon s : items) {
			array.add(JsonObject.mapFrom(s));
		}
		
		message.reply(new JsonObject().put("total", coupons.getTotalCount()).put("items", array));
	}

	private void isExistCoupon(Message<JsonObject> message) {
		DaoReturnCodes ret = coupons.isExistCoupon(message.body().getString("email", ""));
		message.reply(new JsonObject().put("code", ret.name()));
	}

	private void setCoupon(Message<JsonObject> message) {
		DaoReturnCodes ret = coupons.setCoupon(message.body().getString("email"),
				message.body().getString("id"));
		message.reply(new JsonObject().put("code", ret.name()));
	}

}
