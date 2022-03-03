package org.p2p.solanaj.rpc.types.nft;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.types.nft.MetaConst.Key;
import org.p2p.solanaj.rpc.types.nft.MetaConst.TokenStandard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MetaData {
    public MetaData(Key key2, PublicKey update_authority2, PublicKey mint2, Data data2, boolean primary_sale_happened2,
            boolean is_mutable2, Integer edition_nonce2, TokenStandard tokenStandard2, Collection collection2,
            Uses uses2) {
    }
    private MetaConst.Key key;
    private String update_authority;
    private String mint;
    private Data data;
    // Immutable, once flipped, all sales of this metadata are considered secondary.
    private boolean primary_sale_happened;
    // Whether or not the data struct is mutable, default is not
    private boolean is_mutable;
    // Optional
    // nonce for easy calculation of editions, if present
    private Integer edition_nonce;
    // Since we cannot easily change Metadata, we add the new DataV2 fields here at the end.
    // Optional
    private MetaConst.TokenStandard tokenStandard;
    // Collection
    // Optional
    private Collection collection;
    // Uses
    // Optional
    private Uses uses;
} 