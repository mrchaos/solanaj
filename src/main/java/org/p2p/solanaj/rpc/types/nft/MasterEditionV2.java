package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
    
@Getter
@Setter
@AllArgsConstructor
public class MasterEditionV2 {
    private MetaConst.Key key;    
    private long supply;
    // Optional
    private Long max_supply;
}