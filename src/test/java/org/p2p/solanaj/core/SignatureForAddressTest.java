package org.p2p.solanaj.core;

import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
@Slf4j
// @Ignore
public class SignatureForAddressTest {
    private final RpcClient client = new RpcClient(Cluster.DEVNET);
    PublicKey mint = new PublicKey("BdtBaw3u9bBiBLjF9unbPDhXTWaWqz6ngSDXo16yXcT9");

    @Test
    public void testSignatureForAddress() throws RpcException, IOException {
        RpcApi api = new RpcApi(client);
        List<SignatureInformation> sigs = api.getConfirmedSignaturesForAddress2(mint, 2);
        log.info(JsonUtils.toJsonPrettyString(sigs));
    }
}
