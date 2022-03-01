package org.p2p.solanaj.rpc.types.config;

import lombok.Setter;

@SuppressWarnings("unused")
@Setter
public class BlockConfig {

    private String encoding = "json";

    private String transactionDetails = "full";

    private Boolean rewards = true;

    private String commitment;
}