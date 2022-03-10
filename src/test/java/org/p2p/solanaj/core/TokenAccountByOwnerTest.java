package org.p2p.solanaj.core;

import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenInfo;
import org.p2p.solanaj.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import static org.junit.Assert.assertTrue;

@Slf4j
public class TokenAccountByOwnerTest  {
    private final RpcClient client = new RpcClient(Cluster.DEVNET);
    PublicKey owner = new PublicKey("GzZ47NaZbLTy3XxuLsyQfqkPXA4gq7osL5ixgEkTZ8JQ");
    PublicKey mint = new PublicKey("BdtBaw3u9bBiBLjF9unbPDhXTWaWqz6ngSDXo16yXcT9");
    PublicKey programId = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");

    @Test
    public void testTokenAccountInfoList() {
        try {
            log.info("======================mint========================");
            TokenAccountInfo tokenAccountInfo = client.getApi().getTokenAccountsByOwnerWithMint(owner,mint);
            TokenInfo tokenInfo = tokenAccountInfo.getValue().get(0).getAccount().getData().getParsed().getInfo();
            log.info("mint : {}",tokenInfo.getMint());
            log.info(JsonUtils.toJsonPrettyString(tokenAccountInfo.getValue()));

            log.info("======================program id========================");
            tokenAccountInfo = client.getApi().getTokenAccountsByOwnerWithProgramID(owner);
            log.info(JsonUtils.toJsonPrettyString(tokenAccountInfo.getValue()));
        }
        catch(Exception e) {
            log.error(e.toString());
            assertTrue(false);
        }
    }
    // TODO : AccountToken Info에서 원하는 Token Info 찾기
}
