package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CollectionAuthorityRecord {
    private MetaConst.Key key; //1
    private int bump; //1
} 
