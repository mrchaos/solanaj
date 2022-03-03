package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.PublicKey.ProgramDerivedAddress;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * Class for creating Token Program {@link TransactionInstruction}s
 */
public class TokenProgram extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    // 2022.02.16 added, MrChaos
    public static final PublicKey ASSOCIATED_TOKEN_PROGRAM_ID = new PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");
    private static final PublicKey SYSVAR_RENT_PUBKEY = new PublicKey("SysvarRent111111111111111111111111111111111");

    private static final int INITIALIZE_METHOD_ID = 1;
    private static final int TRANSFER_METHOD_ID = 3;
    private static final int CLOSE_ACCOUNT_METHOD_ID = 9;
    private static final int TRANSFER_CHECKED_METHOD_ID = 12;

    /**
    * Transfers an SPL token from the owner's source account to destination account.
     * Destination pubkey must be the Token Account (created by token mint), and not the main SOL address. 
     * @param source  SPL token wallet funding this transaction
     * @param destination Destined SPL token wallet     
     * @param amount 64 bit amount of tokens to send
     * @param owner account/private key signing this transaction
     * @param payer fee payer account/private key signing this transaction
     * @return
     */
    public static TransactionInstruction transfer(
            PublicKey source, 
            PublicKey destination, 
            long amount, 
            PublicKey owner,
            PublicKey payer) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(source,false, true));
        keys.add(new AccountMeta(destination,false, true));
        keys.add(new AccountMeta(owner,true, false));

        if (!Objects.isNull(payer)) {
            keys.add(new AccountMeta(payer,true, false));
        }

        byte[] transactionData = encodeTransferTokenInstructionData(
                amount
        );

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                transactionData
        );
    }

    public static TransactionInstruction transfer(
            PublicKey source, 
            PublicKey destination, 
            long amount, 
            PublicKey owner) {
        
        return transfer(
                source, destination, amount, owner,null);
    }    
    /**
    * Transfers an SPL token from the owner's source account to destination account.
    * Destination pubkey must be the Token Account (created by token mint), and not the main SOL address. 
    * @param source sender associated token address
    * @param destination recipient associated token address
    * @param amount token amount to send
    * @param decimals token decimals
    * @param owner owner wallet address of sender associated token address
    * @param tokenMint token mint address
    * @param payer fee payer wallet address
    * @return TransactionInstruction
    */
    public static TransactionInstruction transferChecked(
        PublicKey source, 
        PublicKey destination, 
        long amount, 
        int decimals, 
        PublicKey owner, 
        PublicKey tokenMint,
        PublicKey payer ) {

        final List<AccountMeta> keys = new ArrayList<>();
        
        keys.add(new AccountMeta(source,false, true));
        // index 1 = token mint (https://docs.rs/spl-token/3.1.0/spl_token/instruction/enum.TokenInstruction.html#variant.TransferChecked)
        keys.add(new AccountMeta(tokenMint, false, false));
        keys.add(new AccountMeta(destination,false, true));        
        keys.add(new AccountMeta(owner,true, false));
        
        if (!Objects.isNull(payer)) {
            keys.add(new AccountMeta(payer,true, false));
        }       

        byte[] transactionData = encodeTransferCheckedTokenInstructionData(
                amount,
                (byte)decimals
        );

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                transactionData
        );
    }    
    public static TransactionInstruction transferChecked(
            PublicKey source, 
            PublicKey destination, 
            long amount, 
            int decimals, 
            PublicKey owner, 
            PublicKey tokenMint) {
                
        return transferChecked(
                source,destination,amount,decimals,owner,tokenMint,null);
    }

    public static TransactionInstruction initializeAccount(final PublicKey account, final PublicKey mint, final PublicKey owner) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(account,false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(owner,false, true));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY,false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) INITIALIZE_METHOD_ID);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    public static TransactionInstruction closeAccount(final PublicKey source, final PublicKey destination, final PublicKey owner) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(source,false, true));
        keys.add(new AccountMeta(destination, false, true));
        keys.add(new AccountMeta(owner,true, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) CLOSE_ACCOUNT_METHOD_ID);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    private static byte[] encodeTransferTokenInstructionData(long amount) {
        ByteBuffer result = ByteBuffer.allocate(9);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put((byte) TRANSFER_METHOD_ID);
        result.putLong(amount);

        return result.array();
    }

    private static byte[] encodeTransferCheckedTokenInstructionData(long amount, byte decimals) {
        ByteBuffer result = ByteBuffer.allocate(10);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put((byte) TRANSFER_CHECKED_METHOD_ID);
        result.putLong(amount);
        result.put(decimals);

        return result.array();
    }
    /**
     * get associated token address
     * @param owner wallet address
     * @param tokenMint token mint address
     * @return associated token address
     */
    public static PublicKey getAssociatedTokenAddress(PublicKey owner, 
        PublicKey tokenMint) throws Exception {
            ProgramDerivedAddress associatedTokenAccount = PublicKey
            .findProgramAddress(
                Arrays.asList(owner.toByteArray(),
                    PROGRAM_ID.toByteArray(),
                    tokenMint.toByteArray()),
                ASSOCIATED_TOKEN_PROGRAM_ID);
            return associatedTokenAccount.getAddress();
    }
    /**
     * Create associated token account
     * @param payer feepayer
     * @param ownertokenAccount associated token account address of owner
     * @param owner owner wallet address
     * @param tokenMint token mint address
     * @return TransactionInstruction
     * @throws Exception
     */
    public static TransactionInstruction createAssociatedTokenAccount(
        final PublicKey payer, 
        final PublicKey ownertokenAccount,
        final PublicKey owner,
        final PublicKey tokenMint
        ) throws Exception {

        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(payer,true, true));
        keys.add(new AccountMeta(ownertokenAccount,false, true));
        keys.add(new AccountMeta(owner, false, false));
        keys.add(new AccountMeta(tokenMint,false, false));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));
        keys.add(new AccountMeta(PROGRAM_ID, false, false));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY,false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 0);

        return createTransactionInstruction(
                ASSOCIATED_TOKEN_PROGRAM_ID,
                keys,
                buffer.array()
        );
    }     
}
