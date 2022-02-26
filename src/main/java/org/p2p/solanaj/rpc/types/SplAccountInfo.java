package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SplAccountInfo extends RpcResultObject {

    @Json(name = "value")
    private TokenResultObjects.TokenValue value;
}
