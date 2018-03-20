package io.pay.coupon.verticles;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pay.coupon.database.DaoReturnCodes;
import io.pay.coupon.utils.GenCouponId;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class CouponIdGenVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CouponIdGenVerticle.class);

	private String couponGenQueue = "coupon.gen.queue";

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		couponGenQueue = config().getString("couponGenQueue", "coupon.gen.queue");
		String queue = config().getString("couponIdGenQueue", "coupon.id.gen.queue");
		vertx.eventBus().consumer(queue, this::onMessage);

		LOGGER.info("Coupon Id Gen Verticle running on " + queue);
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
		case "gen-id":
			genId(message);
			break;
		default:
			LOGGER.error("Unknown action: " + action);
			message.fail(ErrorCodes.UNKNOWN_ACTION.ordinal(), "Unknown action: " + action);
		}
	}

	private void genId(Message<JsonObject> message) {
		if (!isValidEmailAddress(message.body().getString("email", ""))) {
			message.reply(new JsonObject().put("code", DaoReturnCodes.INVALIDEMAILADDRESS.name()));
			return;
		}

		String email = message.body().getString("email");
		JsonObject request = new JsonObject().put("id", GenCouponId.generatePayCouponId(email))
				.put("email", email);
		DeliveryOptions options = new DeliveryOptions().addHeader("action", "set-coupon");

		vertx.eventBus().send(couponGenQueue, request, options,
				new Handler<AsyncResult<Message<JsonObject>>>() {
					private int tryCount = 1;
					private int MAX_TRY_COUNT = 10;

					@Override
					public void handle(AsyncResult<Message<JsonObject>> ar) {
						if (isSuccess(ar)) {
							message.reply((JsonObject) ar.result().body());
							return;
						}
						if (++tryCount > MAX_TRY_COUNT) {
							message.reply(new JsonObject().put("code", DaoReturnCodes.FAIL.name()));
							return;
						}
						request.put("id",
								GenCouponId.generatePayCouponId(GenCouponId.suffleString(email)));
						vertx.eventBus().send(couponGenQueue, request, options, this);
					}

					private boolean isSuccess(AsyncResult<Message<JsonObject>> ar) {
						if (ar.succeeded() && isSuccessCode(
								((JsonObject) ar.result().body()).getString("code"))) {
							return true;
						}
						return false;
					}

					private boolean isSuccessCode(String code) {
						if (code.equals(DaoReturnCodes.SUCCESS.name())
								|| code.equals(DaoReturnCodes.DUP.name())) {
							return true;
						}
						return false;
					}
				});
	}

	public static boolean isValidEmailAddress(String email) {
		return EmailValidator.getInstance().isValid(email);
	}

}
