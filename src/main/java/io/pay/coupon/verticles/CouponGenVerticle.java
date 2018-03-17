package io.pay.coupon.verticles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pay.coupon.database.CouponGenDao;
import io.pay.coupon.database.DaoReturnCodes;
import io.pay.coupon.database.DatabaseType;
import io.pay.coupon.database.factory.DaoFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class CouponGenVerticle extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(CouponGenVerticle.class);

	private CouponGenDao coupons;
	private String couponDatabaseQueue;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		coupons = DaoFactory
				.getDaoFactory(DatabaseType.valueOf(
						config().getString("couponGenDaoFactory", DatabaseType.POJOMAP.name())))
				.getCouponGenDao();
		couponDatabaseQueue = config().getString("couponDatabaseQueue", "coupon.database.queue");

		String queue = config().getString("couponGenQueue", "coupon.gen.queue");
		vertx.eventBus().consumer(queue, this::onMessage);

		LOGGER.info("Coupon Gen Verticle running on " + queue);
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
		case "set-coupon":
			setCoupon(message);
			break;
		default:
			LOGGER.error("Unknown action: " + action);
			message.fail(ErrorCodes.UNKNOWN_ACTION.ordinal(), "Unknown action: " + action);
		}
	}

	private void setCoupon(Message<JsonObject> message) {
		DaoReturnCodes ret = coupons.setCouponId(message.body().getString("id", ""));

		if (ret == DaoReturnCodes.NEW) {
			JsonObject request = new JsonObject()
					.put("email", message.body().getString("email", ""))
					.put("id", message.body().getString("id", ""));
			DeliveryOptions options = new DeliveryOptions().addHeader("action", "set-coupon");
			vertx.eventBus().send(couponDatabaseQueue, request, options, ar -> {
				JsonObject body = (JsonObject) ar.result().body();
				if (!body.getString("code").equals(DaoReturnCodes.SUCCESS.name())) {
					coupons.delCouponId(message.body().getString("id", ""));
				}
				message.reply(body);
			});
			return;
		}

		message.reply(new JsonObject().put("code", ret.name()));
	}

}
