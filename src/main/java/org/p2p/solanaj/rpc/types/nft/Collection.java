package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Collection {
        // 1 byte
        private boolean verified;
        // 4 byte + n byte
        private String pubkey;
}
