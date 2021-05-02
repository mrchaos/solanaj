package org.p2p.solanaj.token;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

public class TokenManager {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);

    public String transferTokensToSolAddress(final Account owner, final PublicKey source, final PublicKey destination, final PublicKey tokenMint, long amount) {
        final Transaction transaction = new Transaction();

        // SPL token instruction
        transaction.addInstruction(
                TokenProgram.transfer(
                        source,
                        destination,
                        amount,
                        owner.getPublicKey()
                )
        );

        // Memo
        transaction.addInstruction(
                MemoProgram.writeUtf8(
                        owner,
                        "Hello from SolanaJ"
                )
        );

        // Call sendTransaction
        String result = null;
        try {
            result = client.getApi().sendTransaction(transaction, owner);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        return result;
    }

}