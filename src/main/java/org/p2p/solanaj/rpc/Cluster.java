package org.p2p.solanaj.rpc;

public enum Cluster {
    // DEVNET("https://api.devnet.solana.com"),
    DEVNET("https://dark-wispy-firefly.solana-devnet.quiknode.pro/b72f227c3011758f04b7251d32e2034df62e415c/"),
    TESTNET("https://api.testnet.solana.com"),
    MAINNET("https://api.mainnet-beta.solana.com");

    private String endpoint;

    Cluster(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
