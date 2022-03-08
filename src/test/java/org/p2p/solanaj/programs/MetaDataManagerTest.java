package org.p2p.solanaj.programs;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.nft.MetaConst;
import org.p2p.solanaj.rpc.types.nft.MetaData;
import org.p2p.solanaj.rpc.types.nft.MasterEditionV1;
import org.p2p.solanaj.rpc.types.nft.MasterEditionV2;
import org.p2p.solanaj.rpc.types.nft.Edition;
import org.p2p.solanaj.rpc.types.MetaDataAccountInfo;
import org.p2p.solanaj.token.MetaDataManager;

import java.util.Base64;
import java.util.Objects;

import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.near.borshj.BorshBuffer;

import static org.junit.Assert.*;

@Slf4j
public class MetaDataManagerTest {
    //public static final Logger LOGGER = Logger.getLogger(TokenMetaProgramTest.class.getName());
    private final RpcClient client = new RpcClient(Cluster.DEVNET);
    @Test
    // @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testGetMetaDataAddress() throws Exception {        
        String mintAddress = "DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc";
        try {
            String metaDataAddress = MetaDataManager.getMetaDataAddress(mintAddress);
            log.info("NFT Meta Data Address :{}",metaDataAddress.toString());
            MetaDataAccountInfo metaDataAccountInfo = (MetaDataAccountInfo)client.getApi().getAccountInfo2(new PublicKey(metaDataAddress));
            log.info(JsonUtils.toJsonPrettyString(metaDataAccountInfo));
            log.info("Meta Data :  {}",metaDataAccountInfo.getValue().getData().get(0));
            log.info("Meta Data Encoding Method :  {}",metaDataAccountInfo.getValue().getData().get(1));
        } catch(Exception e) {
            assertTrue(false);
        }
    }
    @Test
    public void testGetMasterEditionAddress() throws Exception {        
        String mintAddress = "DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc";
        try {
            MetaDataManager metaDataManager = new MetaDataManager(client);
            MetaData metaData = metaDataManager.getMetaData(mintAddress);  
            
            String masterEditionAddress = MetaDataManager.getMasterEditionAddress(metaData.getMint());
            log.info("Master Edition Address : {}",masterEditionAddress);

            Object obj = client.getApi().getAccountInfo2(new PublicKey(masterEditionAddress));
            if (obj instanceof MetaDataAccountInfo) {
                MetaDataAccountInfo metaDataAccountInfo = (MetaDataAccountInfo)client.getApi().getAccountInfo2(new PublicKey(masterEditionAddress));
                log.info(JsonUtils.toJsonPrettyString(metaDataAccountInfo));
                log.info("Meta Data :  {}",metaDataAccountInfo.getValue().getData().get(0));
                log.info("Meta Data Encoding Method :  {}",metaDataAccountInfo.getValue().getData().get(1));
            }
            else {
                log.info("account {} is not Metadata account info!",masterEditionAddress);
            }
        } catch(Exception e) {
            assertTrue(false);
        }
    }
    @Test
    public void testGetMetaDataFromBinary() throws Exception {

        // String mintAddress = "DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc";
        // String mintAddress = "Bf3RLESNeULBD6XsZBJVHkbJPkp7AkrAKZeJdXipN8yc";
        String mintAddress = "GffBfiJ6EJuPYBZA9H5fE85MwVidKzjES3dmETj77YUK";
        

        String metaDataAddress = MetaDataManager.getMetaDataAddress(mintAddress);
        log.info("NFT Meta Data Address :{}",metaDataAddress.toString());
        MetaDataAccountInfo metaDataAccountInfo = (MetaDataAccountInfo)client.getApi().getAccountInfo2(new PublicKey(metaDataAddress));
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
            MetaConst.Key key =  MetaConst.Key.values()[buffer.readU8()];
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
            MetaConst.TokenStandard tokenStandard = MetaConst.TokenStandard.values()[buffer.readU8()];
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
            MetaConst.UseMethod useMethod = MetaConst.UseMethod.values()[buffer.readU8()];
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
    @Test
    public void testGetMetaDataFromBinary2() throws Exception {

        // String mintAddress = "DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc";
        // String mintAddress = "Bf3RLESNeULBD6XsZBJVHkbJPkp7AkrAKZeJdXipN8yc";
        String mintAddress = "GffBfiJ6EJuPYBZA9H5fE85MwVidKzjES3dmETj77YUK";
        
        
        String metaDataAddress = MetaDataManager.getMetaDataAddress(mintAddress);
        log.info("NFT Meta Data Address :{}",metaDataAddress.toString());
        MetaDataAccountInfo metaDataAccountInfo = (MetaDataAccountInfo)client.getApi().getAccountInfo2(new PublicKey(metaDataAddress));
        log.info(JsonUtils.toJsonPrettyString(metaDataAccountInfo));
        log.info("Meta Data :  {}",metaDataAccountInfo.getValue().getData().get(0));
        log.info("Meta Data Encoding Method :  {}",metaDataAccountInfo.getValue().getData().get(1));

        String data = metaDataAccountInfo.getValue().getData().get(0);
        String decodeMeString = metaDataAccountInfo.getValue().getData().get(1);

        MetaDataManager metaDataManager = new MetaDataManager(client);
        MetaData metaData = metaDataManager.getMetaData(mintAddress);        
        log.info(JsonUtils.toJsonPrettyString(metaData));
    }
    void _getEditionData(String editionAddress) {

       try {
           MetaDataManager metaDataManager = new MetaDataManager(client);
           log.info("Edition Address : {}",editionAddress);            
           
           Object obj = metaDataManager.getEditionDataFromEditionAddress(editionAddress);
           if(Objects.isNull(obj)) {
               log.error("Edition data object is null");
               assertTrue(false);
           }
           if(obj instanceof MasterEditionV1) {
               MasterEditionV1 masterEditionV1 = (MasterEditionV1)obj;
               log.info("Master Edition V1 (Deprecated Master edition)");
           }
           else if(obj instanceof MasterEditionV2) {
               MasterEditionV2 masterEditionV2 = (MasterEditionV2)obj;
               log.info("Master Edition V2 (Master edition)");
           }
           else if(obj instanceof Edition) {
               Edition edition = (Edition)obj;
               log.info("Edition (Limited edition)");
           }
           else {
               log.error("Unkown!");
               assertTrue(false);
           }
           log.info(JsonUtils.toJsonPrettyString(obj));
       } catch(Exception e) {
           log.error(e.toString());
           assertTrue(false);
       }        
    }
    @Test
    public void testGetEditionData() {
        // String mintAddress = "DciQ75PXEUQsUkKJy1N6qyyyeYYZGg4Ut6nvMerC1yQc";
        String mintAddress = "2WUYymgjP6z31pggGK2Sh9LNdDWQ5BXH4HQbwNvFjofq";
        // String mintAddress = "GfkFvEzdwk3w3rfn6u9KjGkjwwhUWGspW3z9txASwanj";
        
        
        try {
            MetaDataManager metaDataManager = new MetaDataManager(client);
            MetaData metaData = metaDataManager.getMetaData(mintAddress);  
            if(Objects.isNull(metaData)) {
                log.error("Meta data is null!");
                assertTrue(false);
            }
            log.info("Metadata mint address : {}",metaData.getMint());      
            String masterEditionAddress = MetaDataManager.getMasterEditionAddress(metaData.getMint());
            log.info("Master Edition Address : {}",masterEditionAddress);            
            
            Object obj = metaDataManager.getEditionData(metaData.getMint());
            if(Objects.isNull(obj)) {
                log.error("Edition data object is null");
                assertTrue(false);
            }
            if(obj instanceof MasterEditionV1) {
                MasterEditionV1 masterEditionV1 = (MasterEditionV1)obj;
                log.info("Master Edition V1 (Deprecated Master edition)");
            }
            else if(obj instanceof MasterEditionV2) {
                MasterEditionV2 masterEditionV2 = (MasterEditionV2)obj;
                log.info("Master Edition V2 (Master edition)");
            }
            else if(obj instanceof Edition) {
                Edition edition = (Edition)obj;
                log.info("Edition (Limited edition)");
                if(!Objects.isNull(edition.getParent())) {
                    log.info("Parent({}) data of this edition({})",
                        edition.getParent(),
                        masterEditionAddress
                        );
                    _getEditionData(edition.getParent());
                }                
            }
            else {
                log.error("Unkown!");
                assertTrue(false);
            }
            log.info(JsonUtils.toJsonPrettyString(obj));
        } catch(Exception e) {
            log.error(e.toString());
            assertTrue(false);
        }
    }
    @Test
    public void testGetEditionDataFromEditionAddress() {
        String additionAddress = "Ag1PPgxGAoTDx9kL9tiZ4f3c3kx8rDRr3Ufa1cDhzxNt";
        MetaDataManager metaDataManager = new MetaDataManager(client);        
        try {
            Object obj = metaDataManager.getEditionDataFromEditionAddress(additionAddress);
            if(Objects.isNull(obj)) {
                log.error("Edition data object is null");
                assertTrue(false);
            }
            if(obj instanceof MasterEditionV1) {
                MasterEditionV1 masterEditionV1 = (MasterEditionV1)obj;
                log.info("Master Edition V1 (Deprecated Master edition)");
            }
            else if(obj instanceof MasterEditionV2) {
                MasterEditionV2 masterEditionV2 = (MasterEditionV2)obj;
                log.info("Master Edition V2 (Master edition)");
            }
            else if(obj instanceof Edition) {
                Edition edition = (Edition)obj;
                log.info("Edition (Limited edition)");
            }
            else {
                log.error("Unkown!");
                assertTrue(false);
            }
            log.info(JsonUtils.toJsonPrettyString(obj));
        } catch(Exception e) {
            log.error(e.toString());
            assertTrue(false);
        }
    }    
}
