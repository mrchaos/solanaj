package org.p2p.solanaj.token;

import org.bitcoinj.core.Base58;
import org.near.borshj.BorshBuffer;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.PublicKey.ProgramDerivedAddress;
import org.p2p.solanaj.rpc.types.nft.MetaData;
import org.p2p.solanaj.rpc.types.nft.Uses;
import org.p2p.solanaj.rpc.types.nft.MetaConst.Key;

import lombok.extern.slf4j.Slf4j;

import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.MetaDataAccountInfo;
import org.p2p.solanaj.rpc.types.nft.Collection;
import org.p2p.solanaj.rpc.types.nft.Creator;
import org.p2p.solanaj.rpc.types.nft.Data;
import org.p2p.solanaj.rpc.types.nft.Edition;
import org.p2p.solanaj.rpc.types.nft.MasterEditionV1;
import org.p2p.solanaj.rpc.types.nft.MasterEditionV2;
import org.p2p.solanaj.rpc.types.nft.MetaConst;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Slf4j
public class MetaDataManager {
    // Token Meta Program address
    public static final PublicKey PROGRAM_ID = new PublicKey("metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s");
    // prefix used for PDAs to avoid certain collision attacks (https://en.wikipedia.org/wiki/Collision_attack#Chosen-prefix_collision_attack)

    private final RpcClient client;

    public MetaDataManager(final RpcClient client) {
        this.client = client;
    }
    public static String getMetaDataAddress(String mintAddress) throws Exception {
        ProgramDerivedAddress programAddress = PublicKey.findProgramAddress(
            Arrays.asList(
                MetaConst.PREFIX.getBytes(),
                PROGRAM_ID.toByteArray(),                 
                new PublicKey(mintAddress).toByteArray()
            ), PROGRAM_ID);
        return programAddress.getAddress().toString();
    }

    public static String getMasterEditionAddress(String mintAddress) throws Exception {
        ProgramDerivedAddress programAddress = PublicKey.findProgramAddress(
            Arrays.asList(
                MetaConst.PREFIX.getBytes(),
                PROGRAM_ID.toByteArray(),                 
                new PublicKey(mintAddress).toByteArray(),
                MetaConst.EDITION.getBytes()
            ), PROGRAM_ID);
        return programAddress.getAddress().toString();
    }

    public MetaData getMetaData(String mintAddress) throws Exception {

        String metaDataAddress = getMetaDataAddress(mintAddress);
        Object obj = client.getApi().getAccountInfo2(new PublicKey(metaDataAddress));
        MetaDataAccountInfo metaDataAccountInfo = null;
        if (obj instanceof MetaDataAccountInfo) {
            metaDataAccountInfo = (MetaDataAccountInfo)obj;
        }
        else {
            log.error("Metadta address({}) of mint({}) is not Metadata account type",
            metaDataAddress, mintAddress);
            return null;        
        }        
        String binData = metaDataAccountInfo.getValue().getData().get(0);
        String decodeMeString = metaDataAccountInfo.getValue().getData().get(1);

        BorshBuffer buffer =null;
        if(decodeMeString.equalsIgnoreCase("base58")) {
            buffer = BorshBuffer.wrap(Base58.decode(binData));
        } 
        else if(decodeMeString.equalsIgnoreCase("base64")) {
            buffer = BorshBuffer.wrap(Base64.getDecoder().decode(binData));
        }
        else {
            log.error("Unkown data decode string : {}",decodeMeString);
            return null;
        }       
        // key
        MetaConst.Key key =  MetaConst.Key.values()[buffer.readU8()];    
        // update_authority
        String update_authority = new PublicKey(buffer.readFixedArray(32)).toString();    
        // mint
        String mint = new PublicKey(buffer.readFixedArray(32)).toString();
        // data
        String name = buffer.readString().trim();
        String symbol = buffer.readString().trim();
        String uri = buffer.readString().trim();
        int seller_fee_basis_points = (int)buffer.readU16();
        List<Creator> creators = null;
        // Creators optional check
        if(buffer.readBoolean()) {
            creators = new ArrayList<Creator>();
            // creator수
            long nCreators = buffer.readU32();            
            for(int i =0; i < nCreators; i++) {
                String address = new PublicKey(buffer.readFixedArray(32)).toString();
                boolean verified = buffer.readBoolean();
                byte share =  buffer.readU8();
                Creator creator  = new Creator(address, verified, share);
                creators.add(creator);
            }
        }
        Data data = new Data(name, symbol, uri, seller_fee_basis_points, creators);
        // primary_sale_happened
        boolean primary_sale_happened = buffer.readBoolean();           
        // is_mutable
        boolean is_mutable = buffer.readBoolean();    
        // edition_nonce
        // optional로 존재여부 확인
        Integer edition_nonce = null;
        if(buffer.readBoolean()) {
            edition_nonce = (int)buffer.readU8();
            edition_nonce = edition_nonce < 0 ? 256 + edition_nonce : edition_nonce;
        } 
        MetaConst.TokenStandard tokenStandard = null;
        // tokenStandard는 optional로 존재 유무 확인
        if(buffer.readBoolean()) {
            tokenStandard = MetaConst.TokenStandard.values()[buffer.readU8()];
        }
        Collection collection = null;
        // Collection은 optional로 존재 유무 확인            
        if(buffer.readBoolean()) {
            boolean verified = buffer.readBoolean();
            String ckey = new PublicKey(buffer.readFixedArray(32)).toString();
            collection = new Collection(verified, ckey);
        }
        Uses uses = null;
        // Uses는 optional로 존재 유무 확인
        if(buffer.readBoolean()) {
            MetaConst.UseMethod useMethod = MetaConst.UseMethod.values()[buffer.readU8()];
            long remaining = buffer.readU64();
            long total =  buffer.readU64();
            uses = new Uses(useMethod, remaining, total);    
        }
        return new MetaData(key, update_authority, mint, data, primary_sale_happened, 
                            is_mutable, edition_nonce, tokenStandard, collection, uses);    
    }
    
    public Object getEditionData(String mintAddress) throws Exception {

        String masterEditionAddress = getMasterEditionAddress(mintAddress);
        Object obj = client.getApi().getAccountInfo2(new PublicKey(masterEditionAddress));
        MetaDataAccountInfo metaDataAccountInfo = null;
        if (obj instanceof MetaDataAccountInfo) {
            metaDataAccountInfo = (MetaDataAccountInfo)obj;
        }
        else {
            log.error("Master edition address({}) of mint({}) is not Metadata account type",
                masterEditionAddress, mintAddress);
            return null;        
        }
        String binData = metaDataAccountInfo.getValue().getData().get(0);
        String decodeMeString = metaDataAccountInfo.getValue().getData().get(1);

        BorshBuffer buffer =null;
        if(decodeMeString.equalsIgnoreCase("base58")) {
            buffer = BorshBuffer.wrap(Base58.decode(binData));
        } 
        else if(decodeMeString.equalsIgnoreCase("base64")) {
            buffer = BorshBuffer.wrap(Base64.getDecoder().decode(binData));
        }
        else {
            log.error("Unkown data decode string : {}",decodeMeString);
            return null;
        }       
        // key 1 byte
        MetaConst.Key key =  MetaConst.Key.values()[buffer.readU8()];
        
        if (key == MetaConst.Key.MasterEditionV1) {
            long supply = buffer.readU64();
            Long maxSupply = null;
            // Optional 이 존재하는 경우
            if(buffer.readBoolean()) {
                maxSupply = buffer.readU64();
            }
            String printingMint =  new PublicKey(buffer.readFixedArray(32)).toString();
            String oneTimePrintingAuthorizationMint =  new PublicKey(buffer.readFixedArray(32)).toString();
            return new MasterEditionV1(key, supply, maxSupply, printingMint, oneTimePrintingAuthorizationMint);        
        }
        else if(key == MetaConst.Key.MasterEditionV2) {
            long supply = buffer.readU64();
            Long maxSupply = null;
            // Optional 이 존재하는 경우
            if(buffer.readBoolean()) {
                maxSupply = buffer.readU64();
            }
            return new MasterEditionV2(key, supply, maxSupply);
        }
        else {
            String parent =  new PublicKey(buffer.readFixedArray(32)).toString();
            long edition =  buffer.readU64();
            return new Edition(key, parent, edition);
        }
    }
}

