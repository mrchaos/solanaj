package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MetaData {
    // 1 byte
    private MetaConst.Key key;
    // 5 byte : 4 + 1
    private String updateAuthority;
    // 5 byte : 4 + 1
    private String mint;
    // n byte : n <= MAX_DATA_SIZE(431 byte)
    private Data data;
    // 1 byte
    // Immutable, once flipped, all sales of this metadata are considered secondary.
    private boolean primarySaleHappened;
    // 1 byte
    // Whether or not the data struct is mutable, default is not
    private boolean isMutable;
    // 1 byte Optional check
    // 1 byte Optional check가 true인 경우 
    // nonce for easy calculation of editions, if present
    private Integer editionNonce;
    // 1 byte Optional check
    // 1 byte Optional check가 true인 경우
    // Since we cannot easily change Metadata, we add the new DataV2 fields here at the end.    
    private MetaConst.TokenStandard tokenStandard;
    // 1 byte Optional check
    // k byte Optional check가 true인 경우    
    // 33 byte : 1 + 32
    private Collection collection;
    // Uses
    // Optional
    private Uses uses;
} 