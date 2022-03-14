package org.p2p.solanaj.core;

import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
// import static org.junit.Assert.*;

@Slf4j
@Ignore
public class AccountInfo2Test {
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

                log.info(String.format(
                    "Native account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));
            }
            else if (accountInfo instanceof SplAccountInfo) {
                SplAccountInfo acc = (SplAccountInfo)accountInfo;

                log.info(String.format(
                    "SPL-Token account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));
            }
            else if(accountInfo instanceof SplMintInfo) {
                SplMintInfo acc = (SplMintInfo)accountInfo;

                log.info(String.format(
                    "SPL-Token mint : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));                
            }      
            else if(accountInfo instanceof MetaDataAccountInfo) {
                MetaDataAccountInfo acc = (MetaDataAccountInfo)accountInfo;

                log.info(String.format(
                    "NFT Account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));                
            }
            else {
                UnkownAccountInfo acc = (UnkownAccountInfo)accountInfo;

                log.info(String.format(
                    "Unkown Account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));                    
            }
        }
    }
    @Test
    public void NftMetaData() throws Exception {
        
        List<PublicKey> addresses = List.of(
            new PublicKey("FxpDAaru3qUw3Kyp5apXJDxVCmfTLtyrWLQWzDQzU5Pt"), // nft meta data
            new PublicKey("Gend8BtTQwBkZYXLJHcrEzgCiEzbPRFpwE7crx3CR1p1")
        );       
        
        for (PublicKey address : addresses) {
            final Object accountInfo = client.getApi().getAccountInfo2(address);
            if (accountInfo instanceof AccountInfo) {
                AccountInfo acc = (AccountInfo)accountInfo;

                log.info(String.format(
                    "Native account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));
            }
            else if (accountInfo instanceof SplAccountInfo) {
                SplAccountInfo acc = (SplAccountInfo)accountInfo;

                log.info(String.format(
                    "SPL-Token account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));
            }
            else if(accountInfo instanceof SplMintInfo) {
                SplMintInfo acc = (SplMintInfo)accountInfo;

                log.info(String.format(
                    "SPL-Token mint : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));                
            }      
            else if(accountInfo instanceof MetaDataAccountInfo) {
                MetaDataAccountInfo acc = (MetaDataAccountInfo)accountInfo;

                log.info(String.format(
                    "NFT Account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));                
            }
            else {
                UnkownAccountInfo acc = (UnkownAccountInfo)accountInfo;

                log.info(String.format(
                    "Unkown Account : %s", address.toString()));
                log.info(JsonUtils.toJsonPrettyString(acc));                    
            }            
        }
    }       
}
