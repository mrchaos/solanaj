package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.PublicKey.ProgramDerivedAddress;

import org.p2p.solanaj.rpc.types.MetaData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

public class TokenMetaProgram extends Program {
    // Token Meta Program address
    public static final PublicKey PROGRAM_ID = new PublicKey("metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s");
    // prefix used for PDAs to avoid certain collision attacks (https://en.wikipedia.org/wiki/Collision_attack#Chosen-prefix_collision_attack)



    public static PublicKey getMetaDataAddress(PublicKey mint) throws Exception {
        ProgramDerivedAddress programAddress = PublicKey.findProgramAddress(
            Arrays.asList(
                MetaData.PREFIX.getBytes(),
                PROGRAM_ID.toByteArray(),                 
                mint.toByteArray()
            ), PROGRAM_ID);
        return programAddress.getAddress();
    } 
}
