package org.p2p.solanaj.rpc.types;

import java.util.AbstractMap;
import java.util.List;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class MetaDataAccountInfo extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Value(AbstractMap am) {
            this.data = (List) am.get("data");
            this.executable = (boolean) am.get("executable");
            this.lamports = (long) (double) am.get("lamports");
            this.owner = (String) am.get("owner");
            this.rentEpoch = (long) (double) am.get("rentEpoch");
        }

        @Json(name = "data")
        private List<String> data;

        @Json(name = "executable")
        private boolean executable;

        @Json(name = "lamports")
        private long lamports;

        @Json(name = "owner")
        private String owner;

        @Json(name = "rentEpoch")
        private long rentEpoch;
    }

    @Json(name = "value")
    private Value value;
}
