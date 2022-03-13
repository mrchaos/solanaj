package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UseAuthorityRecord {
    private MetaConst.Key key; //1
    private long allowedUses; //8
    private byte bump;
}