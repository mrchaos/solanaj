package org.p2p.solanaj.programs;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.programs.TokenMetaProgram;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.MetaData;
import org.p2p.solanaj.rpc.types.MetaDataAccountInfo;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction.Meta;
import org.p2p.solanaj.rpc.types.MetaData.RawCreator;
import org.p2p.solanaj.rpc.types.MetaData.RawMetadata;

import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.io.ByteArrayDataInput;

import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.near.borshj.Borsh;
import org.near.borshj.BorshBuffer;

import static org.junit.Assert.*;

@Slf4j
public class TokenMetaProgramTest {
    //public static final Logger LOGGER = Logger.getLogger(TokenMetaProgramTest.class.getName());
    private final RpcClient client = new RpcClient(Cluster.DEVNET);
    @Test
    // @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testGetMetaDataAddress() throws Exception {        
        PublicKey mint = new PublicKey("DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc");
        try {
            PublicKey metaDataAddress = TokenMetaProgram.getMetaDataAddress(mint);
            log.info("NFT Meta Data Address :{}",metaDataAddress.toString());
            MetaDataAccountInfo metaDataAccountInfo = (MetaDataAccountInfo)client.getApi().getAccountInfo2(metaDataAddress);
            log.info(JsonUtils.toJsonPrettyString(metaDataAccountInfo));
            log.info("Meta Data :  {}",metaDataAccountInfo.getValue().getData().get(0));
            log.info("Meta Data Encoding Method :  {}",metaDataAccountInfo.getValue().getData().get(1));
        } catch(Exception e) {
            assertTrue(false);
        }
    }
    @Test
    public void testGetMetaDataFromBinary() throws Exception {

        // String mintAddress = "DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc";
        String mintAddress = "Bf3RLESNeULBD6XsZBJVHkbJPkp7AkrAKZeJdXipN8yc";
        
        PublicKey mintPubkey = new PublicKey(mintAddress);

        PublicKey metaDataAddress = TokenMetaProgram.getMetaDataAddress(mintPubkey);
        log.info("NFT Meta Data Address :{}",metaDataAddress.toString());
        MetaDataAccountInfo metaDataAccountInfo = (MetaDataAccountInfo)client.getApi().getAccountInfo2(metaDataAddress);
        log.info(JsonUtils.toJsonPrettyString(metaDataAccountInfo));
        log.info("Meta Data :  {}",metaDataAccountInfo.getValue().getData().get(0));
        log.info("Meta Data Encoding Method :  {}",metaDataAccountInfo.getValue().getData().get(1));

        String data = metaDataAccountInfo.getValue().getData().get(0);
        String decodeMeString = metaDataAccountInfo.getValue().getData().get(1);

        BorshBuffer buffer =null;
        if(decodeMeString.equalsIgnoreCase("base58")) {
            buffer = BorshBuffer.wrap(Base58.decode(data));
        } 
        else if(decodeMeString.equalsIgnoreCase("base64")) {
            buffer = BorshBuffer.wrap(Base64.getDecoder().decode(data));
        }
        else {
            log.error("Unkown data decode string : {}",decodeMeString);
            return;
        }

        // key
        {
            MetaData.Key key =  MetaData.Key.values()[buffer.readU8()];
            log.info("Key : {}", key.name());
        }

        // update_authority
        PublicKey update_authority = new PublicKey(buffer.readFixedArray(32));
        log.info("update_authority : {}", update_authority.toString());        

        // update_authority
        PublicKey mint = new PublicKey(buffer.readFixedArray(32));
        log.info("mint : {}", mint.toString().trim());

        //=================Data========================
        log.info("=============== {} =================","Data");
        // read 4 byte for data size
        // dSize = buffer.readU32();
        // data
        //BorshBuffer dataBuffer = BorshBuffer.wrap(buffer.read(MetaData.MAX_DATA_SIZE).clone());
        // data - name
        String name = buffer.readString().trim();
        log.info("name : {}", name);    

        // data- symbol
        String symbol = buffer.readString().trim();
        log.info("symbol : {}", symbol);

        // data - uri
        String uri = buffer.readString().trim();
        log.info("uri : {}", uri);

        // data - seller_fee_basis_points
        short seller_fee_basis_points = buffer.readU16();
        log.info("seller_fee_basis_points : {}", seller_fee_basis_points);

        //=================DATA========================
        log.info("=============== {} =================","Data - Creators");        
        // data - creators
        // optional 이기 떄문에 존재여부 확인
        if(buffer.readBoolean()) {
            // creator수
            long nCreators = buffer.readU32();            
            for(int i =0; i < nCreators; i++) {
                PublicKey address = new PublicKey(buffer.readFixedArray(32));
                boolean verified = buffer.readBoolean();
                byte share =  buffer.readU8();
                log.info("creator address : {}", address.toString());
                log.info("creator verified : {}", verified);
                log.info("creator share : {}", share);
            }
        }
        else {
            log.info("creators : {}", "None");
        }
        log.info("===================================");
        // primary_sale_happened
        boolean primary_sale_happened = buffer.readBoolean();
        log.info("primary_sale_happened : {}", primary_sale_happened);
        
        // is_mutable
        boolean is_mutable = buffer.readBoolean();
        log.info("is_mutable : {}", is_mutable); 

        // edition_nonce
        // optional로 존재여부 확인
        if(buffer.readBoolean()) {
            int edition_nonce = buffer.readU8();
            edition_nonce = edition_nonce < 0 ? 256 + edition_nonce : edition_nonce;
            log.info("edition_nonce : {}", edition_nonce);
        } 
        else {
            log.info("edition_nonce : {}", "None");
        }
        //======================= Token Standard ==================
        // tokenStandard는 optional로 존재 유무 확인
        if(buffer.readBoolean()) {
            MetaData.TokenStandard tokenStandard = MetaData.TokenStandard.values()[buffer.readU8()];
            log.info("TokenStandard : {}", tokenStandard.name());
        }
        else  {
            log.info("TokenStandard : {}", "None");
        }
        //======================= Collection ==================
        // Collection은 optional로 존재 유무 확인
        if(buffer.readBoolean()) {
            boolean verified = buffer.readBoolean();
            PublicKey key = new PublicKey(buffer.readFixedArray(32));
            log.info("verified : {}", verified);
            log.info("key : {}", key);
        }
        else {
            log.info("Collection : {}", "None");
        }
        //======================= Uses ==================
        // Uses는 optional로 존재 유무 확인
        if(buffer.readBoolean()) {
            MetaData.UseMethod useMethod = MetaData.UseMethod.values()[buffer.readU8()];
            long remaining = buffer.readU64();
            long total =  buffer.readU64();
            log.info("useMethod : {}", useMethod.name());
            log.info("remaining : {}", remaining);
            log.info("total : {}", total);
        }
        else {
            log.info("UseMethod : {}", "None");
        }
    }
}
