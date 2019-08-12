package org.pendula95.web.requestbin;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import org.pendula95.web.requestbin.dto.RequestDTO;

import java.util.List;

public interface RequestBinService {

    Single<String> createBin();

    Maybe<String> recordRequest(RequestDTO request, String uuid);

    Maybe<List<RequestDTO>> getRequests(String uuid);
}
