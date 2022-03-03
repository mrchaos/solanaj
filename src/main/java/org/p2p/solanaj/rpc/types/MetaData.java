package org.p2p.solanaj.rpc.types;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import org.p2p.solanaj.core.PublicKey;
import org.near.borshj.*;

public class MetaData {
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
                                            4
                                            + MAX_NAME_LENGTH
                                            + 4
                                            + MAX_SYMBOL_LENGTH
                                            + 4
                                            + MAX_URI_LENGTH
                                            + 2
                                            + 1
                                            + 4
                                            + MAX_CREATOR_LIMIT * MAX_CREATOR_LEN;

    public static final int MAX_METADATA_LEN = 
                                            1 // key;
                                            + 32 // update auth pubkey
                                            + 32 // mint pubkey
                                            + MAX_DATA_SIZE 
                                            + 1 // primary sale
                                            + 1 // mutable
                                            + 9 // nonce (pretty sure this only needs to be 2)
                                            + 34 // collection
                                            + 18 // uses
                                            + 2 // token standard
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

    public static class Creator {
        public PublicKey address;
        public boolean verified;
        // In percentages, NOT basis points ;) Watch out!
        public byte share;
    }
    public static class Collection {
        public boolean verified;
        public PublicKey Pubkey;
    }
    public static class  Uses { // 17 bytes + Option byte
        public UseMethod use_method; //1
        public long remaining; //8
        public long total; //8
    }
    public static class Data {
        // The name of the asset
        public String name;
        // The symbol for the asset
        public String symbol;
        // URI pointing to JSON representing the asset
        public String url;
        // Royalty basis points that goes to creators in secondary sales (0-10000)
        public int seller_fee_basis_points;
        // Array of creators
        // Optional
        public List<Creator> creator ;
    }
    public static class DataV2 {
        // The name of the asset
        public String name;
        // The symbol for the asset
        public String symbol;
        // URI pointing to JSON representing the asset
        public String uri;
        // Royalty basis points that goes to creators in secondary sales (0-10000)
        public int seller_fee_basis_points;
        // Array of creators
        // Optional
        public Creator creator;
        // Collection
        // Optional
        public Collection collection;
        // Uses
        // Optional
        public Uses uses; 
    }
    public static class UseAuthorityRecord {
        public Key key; //1
        public long allowed_uses; //8
        public byte bump;
    }
    public static class CollectionAuthorityRecord {
        public Key key; //1
        public byte bump; //1
    } 
    public static class Metadata {
        public Key key;
        public PublicKey update_authority;
        public PublicKey mint;
        public Data data;
        // Immutable, once flipped, all sales of this metadata are considered secondary.
        public boolean primary_sale_happened;
        // Whether or not the data struct is mutable, default is not
        public boolean is_mutable;
        // nonce for easy calculation of editions, if present
        public Byte edition_nonce;
        // Since we cannot easily change Metadata, we add the new DataV2 fields here at the end.
        // Optional
        public Byte TokenStandard;
        // Collection
        // Optional
        public Collection collection;
        // Uses
        // Optional
        public Uses uses;

        public Metadata(byte[] data) {
            Key key = Key.MetadataV1;
            int maxLen = MAX_METADATA_LEN;
        }
    }

    //======================================================
    public class RawCreator {
        public byte[] address = new byte[32];
        public boolean verified;
        // In percentages, NOT basis points ;) Watch out!
        public byte share;
        public RawCreator() {}
    }
    public static class RawData {
        // The name of the asset
        // 4 + 32 : 사이즈 + MAX LEN
        public String name;
        // 4 + 10
        // The symbol for the asset
        public String symbol;
        // 4 + 200
        // URI pointing to JSON representing the asset
        public String url;
        // 2
        // Royalty basis points that goes to creators in secondary sales (0-10000)
        public short seller_fee_basis_points;
        // Array of creators
        // Optional        
        public Optional<List<RawCreator>> creator ;

        public RawData() {}
    }
    public class RawCollection {
        public boolean verified;
        public byte[] pubkey;
        public RawCollection() {
            this.pubkey = new byte[32];
        }
    }
    public  class RawMetadata implements Borsh {
        // 1 byte
        public byte key; 
        // 32 byte
        public byte[] update_authority = new byte[32];
        // 32 byte
        public byte[] mint = new byte[32];
        public RawData data;
        // Immutable, once flipped, all sales of this metadata are considered secondary.
        public boolean primary_sale_happened;
        // Whether or not the data struct is mutable, default is not
        public boolean is_mutable;
        // nonce for easy calculation of editions, if present
        public Optional<Byte> edition_nonce;
        // Since we cannot easily change Metadata, we add the new DataV2 fields here at the end.
        // Optional
        public Optional<Byte> TokenStandard;
        // Collection
        // Optional
        public Optional<RawCollection> collection;
        // Uses
        // Optional
        public Uses uses;

        public RawMetadata() {}
        public RawMetadata(byte key,byte[] update_authority) {}
    }


    public static class MasterEditionV2 {
        public Key key;    
        public long supply;
        // Optional
        public Long max_supply;
    }                

    public static class Edition {
        public Key key; 
    
        // Points at MasterEdition struct
        public PublicKey parent;
    
        // Starting at 0 for master record, this is incremented for each edition minted.
        public long edition;
    } 

    public static class Reservation {
        public PublicKey address;
        public long spots_remaining;
        public long total_spots;
    }

    public static class ReservationV1 {
        public PublicKey address;
        public byte spots_remaining;
        public byte total_spots;
    }
    public static class ReservationListV1 {
        public Key key;
        // Present for reverse lookups
        public PublicKey master_edition;
    
        // What supply counter was on master_edition when this reservation was created.
        // Optional
        public Long supply_snapshot;
        public List<ReservationV1> reservations;
    }
    public static class ReservationListV2 {
        public Key key;
        // Present for reverse lookups
        public PublicKey master_edition;
    
        // What supply counter was on master_edition when this reservation was created.
        // Optional
        public Long supply_snapshot;
        public List<Reservation> reservations;
        // How many reservations there are going to be, given on first set_reservation call
        public long total_reservation_spots;
        // Cached count of reservation spots in the reservation vec to save on CPU.
        public long current_reservation_spots;
    }

    public static class EditionMarker {
        public Key key;
        // size : 31
        public byte[] ledger;
    }
}
