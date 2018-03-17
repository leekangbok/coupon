package io.pay.coupon;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pay.coupon.database.DatabaseType;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		int numOfProcessors = Runtime.getRuntime().availableProcessors();
		JsonObject config = new JsonObject().put("couponDatabaseQueue", "coupon.database.queue")
				.put("couponIdGenQueue", "coupon.id.gen.queue")
				.put("couponGenQueue", "coupon.gen.queue")
				.put("couponDaoFactory", DatabaseType.POJOMAP.name())
				.put("couponGenDaoFactory", DatabaseType.POJOMAP.name());

		Future<String> httpServerFuture = Future.future();
		vertx.deployVerticle("io.pay.coupon.verticles.HttpServerVerticle",
				new DeploymentOptions().setInstances(numOfProcessors).setConfig(config),
				httpServerFuture.completer());

		Future<String> couponDatabaseVerticleFuture = Future.future();
		vertx.deployVerticle("io.pay.coupon.verticles.CouponDatabaseVerticle",
				new DeploymentOptions().setInstances(1).setConfig(config),
				couponDatabaseVerticleFuture.completer());

		Future<String> couponGenVerticleFuture = Future.future();
		vertx.deployVerticle("io.pay.coupon.verticles.CouponGenVerticle",
				new DeploymentOptions().setInstances(1).setConfig(config),
				couponGenVerticleFuture.completer());

		Future<String> couponIdGenVerticleFuture = Future.future();
		vertx.deployVerticle(
				"io.pay.coupon.verticles.CouponIdGenVerticle", new DeploymentOptions()
						.setInstances(numOfProcessors).setConfig(config).setWorker(true),
				couponIdGenVerticleFuture.completer());

		CompositeFuture.all(Arrays.asList(httpServerFuture, couponDatabaseVerticleFuture,
				couponIdGenVerticleFuture)).setHandler(ar -> {
					if (ar.succeeded()) {
						LOGGER.info("Application start");
					} else {
						LOGGER.error("Could not start Application", ar.cause());
					}
				});
	}

}
