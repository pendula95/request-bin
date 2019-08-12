package org.pendula95.web.requestbin.impl;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.Json;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.net.SocketAddress;
import io.vertx.reactivex.redis.client.Command;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.Request;
import io.vertx.reactivex.redis.client.Response;
import org.pendula95.web.requestbin.RequestBinService;
import org.pendula95.web.requestbin.dto.RequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestBinServiceImpl implements RequestBinService {

    private static final Logger logger = LoggerFactory.getLogger(RequestBinService.class);

    private static final long EXPIRATION = 86400;

    private Vertx vertx;
    private Redis redis;


    public Completable connect() {
        return Redis.createClient(vertx, SocketAddress.inetSocketAddress(6379, "localhost"))
                .rxConnect().doOnSuccess(result -> redis = result).ignoreElement();
    }

    public RequestBinServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Single<String> createBin() {
        String uuid = UUID.randomUUID().toString();
        return redis.rxSend(Request.cmd(Command.SETEX).arg(uuid).arg(EXPIRATION).arg(uuid))
                .map(result -> {
                    return uuid;
                }).toSingle();
    }

    @Override
    public Maybe<String> recordRequest(RequestDTO request, String uuid) {
        return redis.rxSend(Request.cmd(Command.GET).arg(uuid))
                .flatMap(exists -> redis.rxSend(Request.cmd(Command.RPUSH).arg("list:bin:" + uuid).arg(Json.encode(request))))
                .map(Response::toString);
    }

    @Override
    public Maybe<List<RequestDTO>> getRequests(String uuid) {
        return redis.rxSend(Request.cmd(Command.GET).arg(uuid))
                .flatMap(exists -> redis.rxSend(Request.cmd(Command.LRANGE).arg("list:bin:" + uuid).arg(0).arg(-1)).map(responses -> {
                    List<RequestDTO> requests = new ArrayList<>();
                    for (Response response : responses) {
                        requests.add(Json.decodeValue(response.toString(), RequestDTO.class));
                    }
                    return requests;
                }));
    }
}
