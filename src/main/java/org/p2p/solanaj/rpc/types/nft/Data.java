package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor

public class Data {
    // The name of the asset
    private String name;
    // The symbol for the asset
    private String symbol;
    // URI pointing to JSON representing the asset
    private String uri;
    // Royalty basis points that goes to creators in secondary sales (0-10000)
    private int sellerFeeBasisPoints;
    // Array of creators
    // Optional
    private List<Creator> creators ;
}
