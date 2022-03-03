package org.p2p.solanaj.rpc.types.nft;

public class MetaConst {
    public static final String  PREFIX = "metadata";
    
    // Used in seeds to make Edition model pda address
    public static final String EDITION = "edition";
    public static final String RESERVATION = "reservation";
    public static final String USER = "user";
    public static final String BURN = "burn";
    public static final String COLLECTION_AUTHORITY = "collection_authority";

    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_SYMBOL_LENGTH = 10;
    public static final int MAX_URI_LENGTH = 200;

    public static final int MAX_EDITION_LEN = 1 + 32 + 8 + 200;

    // Large buffer because the older master editions have two pubkeys in them,
    // need to keep two versions same size because the conversion process actually changes the same account
    // by rewriting it.
    public static final int MAX_MASTER_EDITION_LEN = 1 + 9 + 8 + 264;    
    public static final int MAX_CREATOR_LIMIT = 5;    
    public static final int MAX_CREATOR_LEN = 32 + 1 + 1;
    
    public static final int MAX_RESERVATIONS = 200;
    
    // can hold up to 200 keys per reservation, note: the extra 8 is for number of elements in the vec
    public static final int MAX_RESERVATION_LIST_V1_SIZE = 1 + 32 + 8 + 8 + MAX_RESERVATIONS * 34 + 100;
    
    // can hold up to 200 keys per reservation, note: the extra 8 is for number of elements in the vec
    public static final int MAX_RESERVATION_LIST_SIZE = 1 + 32 + 8 + 8 + MAX_RESERVATIONS * 48 + 8 + 8 + 84;
    
    public static final int MAX_EDITION_MARKER_SIZE = 32;    
    public static final long EDITION_MARKER_BIT_SIZE = 248;
    
    public static final int USE_AUTHORITY_RECORD_SIZE = 18; //8 byte padding
    
    public static final int COLLECTION_AUTHORITY_RECORD_SIZE = 11; //10 byte padding

    public static final int  MAX_DATA_SIZE = 
        // name string
        4 // length of name
        + MAX_NAME_LENGTH // name
        // symbol string
        + 4 // length of symbol
        + MAX_SYMBOL_LENGTH // symbol
        // uri string
        + 4 // length of uri
        + MAX_URI_LENGTH  // uri
        
        + 2 // seller _fee_basis_points
        
        + 1 // creator list optional check(boolean)
        + 4 // number of creators
        + MAX_CREATOR_LIMIT * MAX_CREATOR_LEN; 
        // MAX_CREATOR_LIMIT(5) : max creators number
        // MAX_CREATOR_LEN(34 byte) : Creator{address(pubkey) : 32byte, verified(boolean) 1byte, share(byte) 1byte}

    public static final int MAX_METADATA_LEN = 
        1 // key;
        + 32 // update auth pubkey
        + 32 // mint pubkey
        + MAX_DATA_SIZE 
        + 1 // primary sale (boolean)
        + 1 // mutable (boolean)

        + 1 // edition nonce optional check (boolean)
        + 1 // edition nonce

        + 1 // token standard optional check(boolean) 1 byte
        + 1 // token standard

        + 1 // collection optional(boolean) check
        + 33 // collection {verfied(boolean) 1 byte, key(pubkey): 32 byte}

        + 1  // uses optional check(boolean)
        + 17 // uses {useMethos(UseMethod) 1byte, remaning(long) 8byte, total(long) 8byte}

        + 118; // Padding     
        public static enum Key {
            Uninitialized,
            EditionV1,
            MasterEditionV1,
            ReservationListV1,
            MetadataV1,
            ReservationListV2,
            MasterEditionV2,
            EditionMarker,
            UseAuthorityRecord,
            CollectionAuthorityRecord;
                 
        }
        public static enum UseMethod {
            Burn,
            Multiple,
            Single,        
        }
    
        public static enum TokenStandard {
            NonFungible,  // This is a master edition
            FungibleAsset, // A token with metadata that can also have attrributes
            Fungible,     // A token with simple metadata
            NonFungibleEdition,      // This is a limited edition
        }           
}
