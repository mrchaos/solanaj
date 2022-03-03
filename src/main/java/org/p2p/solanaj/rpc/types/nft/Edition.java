package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Edition {
    private MetaConst.Key key; 

    // Points at MasterEdition struct
    private String parent;

    // Starting at 0 for master record, this is incremented for each edition minted.
    private long edition;
} 
