package org.p2p.solanaj.core;

import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.utils.JsonUtils;

import java.util.*;
import java.util.logging.Logger;

// import static org.junit.Assert.*;


public class AccountInfo2Test {
    public static final Logger LOGGER = Logger.getLogger(AccountInfo2Test.class.getName());
    private final RpcClient client = new RpcClient(Cluster.DEVNET);
    
    @Test
    public void AccountInfoTest01() throws Exception {
        
        List<PublicKey> addresses = List.of(
            new PublicKey("DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc"), // nft mint 
            new PublicKey("BdtBaw3u9bBiBLjF9unbPDhXTWaWqz6ngSDXo16yXcT9"), // token mint
            new PublicKey("FgiSCoaNf6MmvexNPuSb63FdbHPF6bHwCZF6KZg54N3C"), // token account
            new PublicKey("GzZ47NaZbLTy3XxuLsyQfqkPXA4gq7osL5ixgEkTZ8JQ")  // wallet
        );       
        
        for (PublicKey address : addresses) {
            final Object accountInfo = client.getApi().getAccountInfo2(address);
            if (accountInfo instanceof AccountInfo) {
                AccountInfo acc = (AccountInfo)accountInfo;

                LOGGER.info(String.format(
                    "Native account : %s", address.toString()));
                LOGGER.info(JsonUtils.toJsonPrettyString(acc));
            }
            else if (accountInfo instanceof SplAccountInfo) {
                SplAccountInfo acc = (SplAccountInfo)accountInfo;

                LOGGER.info(String.format(
                    "SPL-Token account : %s", address.toString()));
                LOGGER.info(JsonUtils.toJsonPrettyString(acc));
            }
            else if(accountInfo instanceof SplMintInfo) {
                SplMintInfo acc = (SplMintInfo)accountInfo;

                LOGGER.info(String.format(
                    "SPL-Token mint : %s", address.toString()));
                LOGGER.info(JsonUtils.toJsonPrettyString(acc));                
            }      
        }
    }      
}
