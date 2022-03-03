package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public class Creator {
    private String address;
    private boolean verified;
    // In percentages, NOT basis points ;) Watch out!
    private int share; //1 byte
}