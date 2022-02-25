package org.p2p.solanaj.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.rpc.types.TokenResultObjects.*;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.token.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedHashTreeMap;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import static org.junit.Assert.*;


public class AccountInfoTest2 {
    private final RpcClient client = new RpcClient(Cluster.DEVNET);

    /**
     * json object에서 원하는 값/object를 가져온다.
     * @param path json tree map에서 최종 key값 까지의 경로
     *              예) "value.data.parsed.info.mint"
     * @param map map object
     * @return path에 해당하는 object
     * @throws Exception
     */
    static public Object getObject(String path, Object map ) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String,Object> m = (Map<String,Object>)map;
        String[] keys = path.split("\\.");
        Object obj = null;
        String key = null;
        for (int i=0; i < keys.length; i++) {
            key = keys[i];
            obj = m.getOrDefault(key, null);
            // object가 null이거나 key경로의 마지막인 경우
            if(Objects.isNull(obj)
                || i == keys.length - 1) {
                return obj;
            }
            // key의 value가 Map instance인 경우 mao으로 mapping
            else if (obj instanceof Map) {
                m = (Map<String,Object>)Map.class.cast(obj);
            }
            else {
                return null;
            }
        }
        return obj;
    }
    /**
     * json object를 class object로 casting
     * @param <T>
     * @param jsonObject
     * @param clazz
     * @return
     * @throws RpcException
     * @throws IOException
     */
    static public <T> T castJsonObject(Object jsonObject, Class<T> clazz) throws RpcException, IOException {

        JsonAdapter<T> resultAdapter = new Moshi.Builder().build()
                .adapter(Type.class.cast(clazz));
        T rpcResult = resultAdapter.fromJsonValue(jsonObject);
        return rpcResult;
    } 

    static public void printJson(Object jsonObject) {
        if (!Objects.isNull(jsonObject)) {
            Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
            String j = gson.toJson(jsonObject);
            System.out.println(gson.toJson(jsonObject));
        } 
    }
    
    @Test
    public void AccountInfoTest01() throws Exception {
        // nft mint : DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc
        // token mint : BdtBaw3u9bBiBLjF9unbPDhXTWaWqz6ngSDXo16yXcT9
        // token account : FgiSCoaNf6MmvexNPuSb63FdbHPF6bHwCZF6KZg54N3C
        // wallet : GzZ47NaZbLTy3XxuLsyQfqkPXA4gq7osL5ixgEkTZ8JQ         
        List<PublicKey> addresses = List.of(
            new PublicKey("DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc"),
            new PublicKey("BdtBaw3u9bBiBLjF9unbPDhXTWaWqz6ngSDXo16yXcT9"),
            new PublicKey("FgiSCoaNf6MmvexNPuSb63FdbHPF6bHwCZF6KZg54N3C"),
            new PublicKey("GzZ47NaZbLTy3XxuLsyQfqkPXA4gq7osL5ixgEkTZ8JQ")
        );       
        
        for (PublicKey address : addresses) {
            final Object accountObj = client.getApi().getAccountInfoJson(address);
            final String progID = (String) getObject("value.owner", accountObj);
            // SystemProgram인 경우 Native Account
            if(progID.equals(SystemProgram.PROGRAM_ID.toString())) {                
                System.out.println(String.format("Native account : %s", address.toString()));
                AccountInfo accountInfo = 
                castJsonObject(accountObj,AccountInfo.class);
                printJson(accountInfo);                
            }            
            else if(progID.equals(TokenProgram.PROGRAM_ID.toString())) {
                //@SuppressWarnings("unchecked")
                String type = (String)getObject("value.data.parsed.type", accountObj);
                if(type.equals("account")) {
                    System.out.println(String.format("SPL-Token account : %s", address.toString()));
                    SplTokenAccountInfo splTokenAccountInfo = 
                    castJsonObject(accountObj,SplTokenAccountInfo.class);
                    printJson(splTokenAccountInfo);
                }
                else if(type.equals("mint")) {
                    System.out.println(String.format("SPL-Token mint : %s", address.toString()));
                    SplTokenMintInfo splTokenMintInfo = 
                    castJsonObject(accountObj,SplTokenMintInfo.class);
                    printJson(splTokenMintInfo);
                }
                else {
                    throw new RuntimeException("Not supported account type");
                }
            }         
        }
    }      
}
