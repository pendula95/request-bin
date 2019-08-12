package org.pendula95.web.requestbin;


import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler;
import org.pendula95.web.requestbin.dto.RequestDTO;
import org.pendula95.web.requestbin.impl.RequestBinServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestBinVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(RequestBinVerticle.class);

    private RequestBinService requestBinService;
    private EventBus eventBus;

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(RequestBinVerticle.class.getName());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        requestBinService = new RequestBinServiceImpl(vertx);
        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.post("/bin/").handler(this::createBin);

        router.get("/api/bin/:uuid").produces("application/json").handler(this::getBin);
        router.post("/api/bin/").handler(this::createBinApi);

        router.route().pathRegex("/([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8}).*").handler(this::request);

        router.route("/eventbus/*").handler(eventBusHandler());

        router.route().handler(StaticHandler.create());

        eventBus = vertx.eventBus();

        vertx.createHttpServer().requestHandler(router).rxListen(8888, "0.0.0.0")
                .flatMapCompletable(result -> ((RequestBinServiceImpl)requestBinService).connect())
                .subscribe(startPromise::complete, startPromise::fail);
    }

    private void createBinApi(RoutingContext routingContext) {
        requestBinService.createBin().subscribe(result -> {
            routingContext.response().setStatusCode(201).end(new JsonObject().put("uuid", result).encode());
        });
    }

    private void request(RoutingContext routingContext) {
        Map<String, String> headers = new HashMap<>();
        routingContext.request().headers().iterator().forEachRemaining(x -> headers.put(x.getKey(), x.getValue()));
        Map<String, String> queryParams = new HashMap<>();
        routingContext.queryParams().iterator().forEachRemaining(x -> queryParams.put(x.getKey(), x.getValue()));
        RequestDTO requestDTO = new RequestDTO(routingContext.request().rawMethod(), headers, queryParams, routingContext.getBodyAsString(),
                System.currentTimeMillis(), routingContext.request().connection().remoteAddress().host(), routingContext.normalisedPath(), UUID.randomUUID().toString());
        String uuid = routingContext.request().getParam("param0");
        logger.info("Request on = {}", uuid);
        requestBinService.recordRequest(requestDTO, uuid)
                .doOnSuccess(result -> eventBus.publish(uuid, Json.encode(requestDTO)))
                .subscribe(result -> routingContext.response().end(), ex ->
                        routingContext.response().setStatusCode(500).end(ex.getMessage()), () -> routingContext.response().setStatusCode(404).end());
    }

    private void getBin(RoutingContext routingContext) {
        requestBinService.getRequests(routingContext.request().getParam("uuid")).map(Json::encodePrettily)
                .subscribe(result -> routingContext.response().end(result),
                        error -> routingContext.response().setStatusCode(500).end(error.getMessage()),
                        () -> routingContext.response().setStatusCode(404).end());
    }

    private void createBin(RoutingContext routingContext) {
        requestBinService.createBin().subscribe(result -> {
            routingContext.response().putHeader("location", "/bin.html?uuid=" + result).setStatusCode(303).end();
        });
    }

    private SockJSHandler eventBusHandler() {
        BridgeOptions options = new BridgeOptions()
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})"));
        return SockJSHandler.create(vertx).bridge(options);
    }
}
