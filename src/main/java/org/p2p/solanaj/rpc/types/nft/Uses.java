package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class  Uses { // 17 bytes + Option byte
    private MetaConst.UseMethod useMethod; //1
    private long remaining; //8
    private long total; //8
}