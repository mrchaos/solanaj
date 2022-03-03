package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EditionMarker {
    public MetaConst.Key key;
    // size : 31
    public byte[] ledger;
}