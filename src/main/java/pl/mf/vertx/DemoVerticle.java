package pl.mf.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public class DemoVerticle extends AbstractVerticle {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new DemoVerticle());
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Router router = Router.router(vertx);
		
		BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"))
				.addInboundPermitted(new PermittedOptions().setAddressRegex(".*")).setPingTimeout(300000).setReplyTimeout(300000);

		router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options, new CustomBridgeEvent()));
		
		vertx.eventBus().consumer("news-feed", message -> {
			LogUtils.printMessageWithDate("CONSUMER -> BODY: " + message.body() + ", ADDRESS: " + message.address() + ", REPLY_ADDRESS: " + message.replyAddress());
			JsonObject rep = new JsonObject(message.body().toString()).put("consumer_ack", "ok");
			message.reply(rep);
		});
		
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}