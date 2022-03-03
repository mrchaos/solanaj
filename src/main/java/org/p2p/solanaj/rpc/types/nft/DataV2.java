package org.p2p.solanaj.rpc.types.nft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DataV2 {
    // The name of the asset
    private String name;
    // The symbol for the asset
    private String symbol;
    // URI pointing to JSON representing the asset
    private String uri;
    // Royalty basis points that goes to creators in secondary sales (0-10000)
    private int seller_fee_basis_points;
    // Array of creators
    // Optional
    private Creator creator;
    // Collection
    // Optional
    private Collection collection;
    // Uses
    // Optional
    private Uses uses; 
}
