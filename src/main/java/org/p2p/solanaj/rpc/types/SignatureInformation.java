package org.p2p.solanaj.rpc.types;

import java.util.AbstractMap;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class SignatureInformation {

    @Json(name = "blockTime")
    private long blockTime;

    @Json(name = "confirmationStatus")
    private String confirmationStatus;

    @Json(name = "err")
    private Object err;   

    @Json(name = "memo")
    private Object memo;

    @Json(name = "signature")
    private String signature;

    @Json(name = "slot")
    private long slot;

    @SuppressWarnings({ "rawtypes" })
    public SignatureInformation(AbstractMap info) {
        this.blockTime = (long) ((double)info.get("blockTime"));
        this.confirmationStatus = (String)info.get("confirmationStatus");
        this.err = info.get("err");
        this.memo = info.get("memo");
        this.signature = (String) info.get("signature");
        this.slot = (long) ((double)info.get("slot"));
    }   
}
