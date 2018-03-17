package io.pay.coupon.verticles;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

public class HttpServerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	private String couponDatabaseQueue = "coupon.database.queue";
	private String couponIdGenQueue = "coupon.id.gen.queue";

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		couponDatabaseQueue = config().getString("couponDatabaseQueue", "coupon.database.queue");
		couponIdGenQueue = config().getString("couponIdGenQueue", "coupon.id.gen.queue");

		Router router = setRouter();
		HttpServer server = vertx.createHttpServer();

		int portNumber = config().getInteger("httpServerPort", 8080);
		server.requestHandler(router::accept).listen(portNumber, ar -> {
			if (ar.succeeded()) {
				LOGGER.info("HTTP server running on port " + portNumber);
				startFuture.complete();
			} else {
				LOGGER.error("Could not start a HTTP server", ar.cause());
				startFuture.fail(ar.cause());
			}
		});
	}

	private Router setRouter() {
		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());

		router.get("/").handler(this::indexHandler);

		router.mountSubRouter("/couponApi", setCouponApi());

		router.route("/static/*")
				.handler(StaticHandler.create("webroot/static").setCachingEnabled(false));

		return router;
	}

	private Router setCouponApi() {
		Router router = Router.router(vertx);

		router.get("/coupon").handler(this::couponGetHandler);
		router.post("/coupon").handler(this::couponAddHandler);
		return router;
	}

	private void indexHandler(RoutingContext routeContext) {
		routeContext.response().sendFile("webroot/index.html");
	}

	private void couponGetHandler(RoutingContext routeContext) {

		MultiMap params = routeContext.queryParams();

		long offset;
		long limit;

		try {
			offset = Integer.parseUnsignedInt(params.get("offset"));
			limit = Integer.parseUnsignedInt(params.get("limit"));

			if (offset < 0 || limit < 0) {
				throw new InvalidParameterException();
			}
		} catch (Exception e) {
			offset = 0;
			limit = 100;
		}

		JsonObject request = new JsonObject().put("offset", offset).put("limit", limit);
		DeliveryOptions options = new DeliveryOptions().addHeader("action", "get-all");

		vertx.eventBus().send(couponDatabaseQueue, request, options, ar -> {
			if (ar.succeeded()) {
				routeContext.response().end(Json.encodePrettily(ar.result().body()));
			} else {
				routeContext.fail(ar.cause());
			}
		});
	}

	private void couponAddHandler(RoutingContext routeContext) {
		String email = routeContext.request().getParam("email");

		JsonObject request = new JsonObject().put("email", email);
		DeliveryOptions options = new DeliveryOptions().addHeader("action", "is-exist-coupon");

		vertx.eventBus().send(couponDatabaseQueue, request, options, ar -> {
			if (ar.succeeded()) {
				JsonObject body = (JsonObject) ar.result().body();
				HttpServerResponse response = routeContext.response();

				if (body.getString("code", "").equals("NEW")) {
					DeliveryOptions genIdoptions = new DeliveryOptions().addHeader("action",
							"gen-id");
					vertx.eventBus().send(couponIdGenQueue, request, genIdoptions, genIdAr -> {
						response.end(Json.encodePrettily(genIdAr.result().body()));
					});
				} else {
					response.end(Json.encodePrettily(ar.result().body()));
				}
			} else {
				routeContext.fail(ar.cause());
			}
		});
	}

}
