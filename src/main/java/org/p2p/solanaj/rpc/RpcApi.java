package org.p2p.solanaj.rpc;

import java.util.*;
import java.util.stream.Collectors;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.rpc.types.config.BlockConfig;
import org.p2p.solanaj.rpc.types.ConfirmedSignFAddr2;
import org.p2p.solanaj.rpc.types.DataSize;
import org.p2p.solanaj.rpc.types.Filter;
import org.p2p.solanaj.rpc.types.Memcmp;
import org.p2p.solanaj.rpc.types.config.LargestAccountConfig;
import org.p2p.solanaj.rpc.types.config.LeaderScheduleConfig;
import org.p2p.solanaj.rpc.types.config.ProgramAccountConfig;
import org.p2p.solanaj.rpc.types.config.RpcEpochConfig;
import org.p2p.solanaj.rpc.types.RpcResultTypes.ValueLong;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;
import org.p2p.solanaj.token.MetaDataManager;
import org.p2p.solanaj.rpc.types.config.SignatureStatusConfig;
import org.p2p.solanaj.rpc.types.config.SimulateTransactionConfig;
import org.p2p.solanaj.rpc.types.TokenResultObjects.*;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.rpc.types.config.VoteAccountConfig;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

import org.p2p.solanaj.utils.JsonUtils;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;

public class RpcApi {
    private RpcClient client;

    public RpcApi(RpcClient client) {
        this.client = client;
    }

    public String getRecentBlockhash() throws RpcException {
        return getRecentBlockhash(null);
    }

    public String getRecentBlockhash(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getRecentBlockhash", params, RecentBlockhash.class).getValue().getBlockhash();
    }

    public String sendTransaction(Transaction transaction, Account signer, String recentBlockHash) throws
            RpcException {
        return sendTransaction(transaction, Collections.singletonList(signer), recentBlockHash);
    }

    public String sendTransaction(Transaction transaction, Account signer) throws RpcException {
        return sendTransaction(transaction, Collections.singletonList(signer), null);
    }

    public String sendTransaction(Transaction transaction, List<Account> signers, String recentBlockHash)
            throws RpcException {
        if (recentBlockHash == null) {
            recentBlockHash = getRecentBlockhash();
        }
        transaction.setRecentBlockHash(recentBlockHash);
        transaction.sign(signers);
        byte[] serializedTransaction = transaction.serialize();

        String base64Trx = Base64.getEncoder().encodeToString(serializedTransaction);

        List<Object> params = new ArrayList<Object>();

        params.add(base64Trx);
        params.add(new RpcSendTransactionConfig());

        return client.call("sendTransaction", params, String.class);
    }

    public void sendAndConfirmTransaction(Transaction transaction, List<Account> signers,
            NotificationEventListener listener) throws RpcException {
        String signature = sendTransaction(transaction, signers, null);

        SubscriptionWebSocketClient subClient = SubscriptionWebSocketClient.getInstance(client.getEndpoint());
        subClient.signatureSubscribe(signature, listener);
    }

    public long getBalance(PublicKey account) throws RpcException {
        return getBalance(account, null);
    }

    public long getBalance(PublicKey account, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());
        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getBalance", params, ValueLong.class).getValue();
    }

    public ConfirmedTransaction getConfirmedTransaction(String signature) throws RpcException {
        List<Object> params = new ArrayList<Object>();

        params.add(signature);
        // TODO jsonParsed, base58, base64
        // the default encoding is JSON
        // params.add("json");

        return client.call("getConfirmedTransaction", params, ConfirmedTransaction.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<SignatureInformation> getConfirmedSignaturesForAddress2(PublicKey account, int limit)
            throws RpcException {
        List<Object> params = new ArrayList<Object>();

        params.add(account.toString());
        params.add(new ConfirmedSignFAddr2(limit));

        List<AbstractMap> rawResult = client.call("getConfirmedSignaturesForAddress2", params, List.class);

        List<SignatureInformation> result = new ArrayList<SignatureInformation>();
        for (AbstractMap item : rawResult) {
            result.add(new SignatureInformation(item));
        }

        return result;
    }

    public List<ProgramAccount> getProgramAccounts(PublicKey account, long offset, String bytes) throws RpcException {
        List<Object> filters = new ArrayList<Object>();
        filters.add(new Filter(new Memcmp(offset, bytes)));

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(filters);
        return getProgramAccounts(account, programAccountConfig);
    }

    public List<ProgramAccount> getProgramAccounts(PublicKey account) throws RpcException {
        return getProgramAccounts(account, new ProgramAccountConfig(Encoding.base64));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ProgramAccount> getProgramAccounts(PublicKey account, ProgramAccountConfig programAccountConfig)
            throws RpcException {
        List<Object> params = new ArrayList<Object>();

        params.add(account.toString());

        if (programAccountConfig != null) {
            params.add(programAccountConfig);
        }

        List<AbstractMap> rawResult = client.call("getProgramAccounts", params, List.class);

        List<ProgramAccount> result = new ArrayList<ProgramAccount>();
        for (AbstractMap item : rawResult) {
            result.add(new ProgramAccount(item));
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ProgramAccount> getProgramAccounts(PublicKey account, List<Memcmp> memcmpList, int dataSize)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());

        List<Object> filters = new ArrayList<>();
        memcmpList.forEach(memcmp -> {
            filters.add(new Filter(memcmp));
        });

        filters.add(new DataSize(dataSize));

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(filters);
        params.add(programAccountConfig);

        List<AbstractMap> rawResult = client.call("getProgramAccounts", params, List.class);

        List<ProgramAccount> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ProgramAccount(item));
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ProgramAccount> getProgramAccounts(PublicKey account, List<Memcmp> memcmpList) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());

        List<Object> filters = new ArrayList<>();
        memcmpList.forEach(memcmp -> {
            filters.add(new Filter(memcmp));
        });

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(filters);
        params.add(programAccountConfig);

        List<AbstractMap> rawResult = client.call("getProgramAccounts", params, List.class);

        List<ProgramAccount> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ProgramAccount(item));
        }

        return result;
    }

    public AccountInfo getAccountInfo(PublicKey account) throws RpcException {
        return getAccountInfo(account, new LinkedHashMap<>());
    }

    public AccountInfo getAccountInfo(PublicKey account, Map<String, Object> additionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();

        Map<String, Object> parameterMap = new LinkedHashMap<>();

        parameterMap.put("encoding", additionalParams.getOrDefault("encoding", "base64"));

        if (additionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment) additionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }
        if (additionalParams.containsKey("dataSlice")) {
            parameterMap.put("dataSlice", additionalParams.get("dataSlice"));
        }

        params.add(account.toString());
        params.add(parameterMap);

        return client.call("getAccountInfo", params, AccountInfo.class);
    }

    /**
     * account type에 따른 account정보를 return
     * @param account account publickey - account, token, mint
     * @return account인 경우: AccountInfo, 
     *         token : SplAccountInfo,
     *         mint  : SplMintInfo
     * @throws Exception exception
     */
    public Object getAccountInfo2(PublicKey account) throws Exception {        
        List<Object> params = new ArrayList<>();
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("encoding", "jsonParsed");

        params.add(account.toString());
        params.add(parameterMap);
        String json = client.callApi("getAccountInfo", params);

        JsonAdapter<RpcResponse<Object>> resultAdapter = new Moshi.Builder().build()
        .adapter(Types.newParameterizedType(RpcResponse.class, Object.class));
        RpcResponse<Object> rpcResult = resultAdapter.fromJson(json);

        if (rpcResult.getError() != null) {
            throw new RpcException(rpcResult.getError().getMessage());
        }

        final Object accountObj = rpcResult.getResult();

        final String progID = (String) JsonUtils.getObjectFromMap("value.owner", accountObj);
        if (Objects.isNull(progID)) {
            return JsonUtils.castJson(accountObj,UnkownAccountInfo.class);
        }

        // SystemProgram인 경우 Native Account
        if(progID.equals(SystemProgram.PROGRAM_ID.toString())) {                
            return JsonUtils.castJson(accountObj,AccountInfo.class);
        }        
        else if(progID.equals(TokenProgram.PROGRAM_ID.toString())) {
            String type = (String)JsonUtils.getObjectFromMap(
                    "value.data.parsed.type", accountObj);
            if(type.equals("account")) {
                return JsonUtils.castJson(accountObj,SplAccountInfo.class);
            }
            else if(type.equals("mint")) {
                return JsonUtils.castJson(accountObj,SplMintInfo.class);
            }
            else {
                // throw new RuntimeException("Not supported account type");
                return JsonUtils.castJson(accountObj,UnkownAccountInfo.class);
            }
        }
        else if(progID.equals(MetaDataManager.PROGRAM_ID.toString())) {
            return JsonUtils.castJson(accountObj,MetaDataAccountInfo.class);
        }
        else {
            return JsonUtils.castJson(accountObj,UnkownAccountInfo.class);
        }
    }    
    public SplAccountInfo getSplAccountInfo(PublicKey account) throws RpcException {
        List<Object> params = new ArrayList<>();
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("encoding", "jsonParsed");

        params.add(account.toString());
        params.add(parameterMap);

        return client.call("getAccountInfo", params, SplAccountInfo.class);
    }
    public SplMintInfo getSplMintInfo(PublicKey account) throws RpcException {
        List<Object> params = new ArrayList<>();
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("encoding", "jsonParsed");

        params.add(account.toString());
        params.add(parameterMap);

        return client.call("getAccountInfo", params, SplMintInfo.class);
    }    

    public long getMinimumBalanceForRentExemption(long dataLength) throws RpcException {
        return getMinimumBalanceForRentExemption(dataLength, null);
    }

    public long getMinimumBalanceForRentExemption(long dataLength, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(dataLength);
        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getMinimumBalanceForRentExemption", params, Long.class);
    }

    public long getBlockTime(long block) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(block);

        return client.call("getBlockTime", params, Long.class);
    }

    /**
     * Seemingly deprecated on the official Solana API.
     *
     * @return block height
     * @throws RpcException rpc exception
     */
    public long getBlockHeight() throws RpcException {
        return getBlockHeight(null);
    }

    public long getBlockHeight(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }
        return client.call("getBlockHeight", params, Long.class);
    }

    public BlockProduction getBlockProduction() throws RpcException {
        return getBlockProduction(new LinkedHashMap<>());
    }

    // TODO - implement the parameters - currently takes in none
    public BlockProduction getBlockProduction(Map<String, Object> optionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();

        Map<String, Object> parameterMap = new LinkedHashMap<>();
        if (optionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment) optionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }
        params.add(parameterMap);

        return client.call("getBlockProduction", params, BlockProduction.class);
    }

    public Long minimumLedgerSlot() throws RpcException {
        return client.call("minimumLedgerSlot", new ArrayList<>(), Long.class);
    }

    public SolanaVersion getVersion() throws RpcException {
        return client.call("getVersion", new ArrayList<>(), SolanaVersion.class);
    }

    public String requestAirdrop(PublicKey address, long lamports) throws RpcException {
        return requestAirdrop(address, lamports, null);
    }

    public String requestAirdrop(PublicKey address, long lamports, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<Object>();

        params.add(address.toString());
        params.add(lamports);
        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("requestAirdrop", params, String.class);
    }

    public BlockCommitment getBlockCommitment(long block) throws RpcException {
        List<Object> params = new ArrayList<Object>();

        params.add(block);

        return client.call("getBlockCommitment", params, BlockCommitment.class);
    }

    public FeeCalculatorInfo getFeeCalculatorForBlockhash(String blockhash) throws RpcException {
        return getFeeCalculatorForBlockhash(blockhash, null);
    }

    public FeeCalculatorInfo getFeeCalculatorForBlockhash(String blockhash, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(blockhash);
        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getFeeCalculatorForBlockhash", params, FeeCalculatorInfo.class);
    }

    public FeeRateGovernorInfo getFeeRateGovernor() throws RpcException {
        return client.call("getFeeRateGovernor", new ArrayList<>(), FeeRateGovernorInfo.class);
    }

    public FeesInfo getFees() throws RpcException {
        return getFees(null);
    }

    public FeesInfo getFees(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getFees", params, FeesInfo.class);
    }

    public long getTransactionCount() throws RpcException {
        return getTransactionCount(null);
    }

    public long getTransactionCount(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getTransactionCount", params, Long.class);
    }

    public long getMaxRetransmitSlot() throws RpcException {
        return client.call("getMaxRetransmitSlot", new ArrayList<>(), Long.class);
    }

    public SimulatedTransaction simulateTransaction(String transaction, List<PublicKey> addresses) throws RpcException {
        SimulateTransactionConfig simulateTransactionConfig = new SimulateTransactionConfig(Encoding.base64);
        simulateTransactionConfig.setAccounts(
                Map.of(
                        "encoding",
                        Encoding.base64,
                        "addresses",
                        addresses.stream().map(PublicKey::toBase58).collect(Collectors.toList()))
        );
        simulateTransactionConfig.setReplaceRecentBlockhash(true);

        List<Object> params = new ArrayList<>();
        params.add(transaction);
        params.add(simulateTransactionConfig);

        SimulatedTransaction simulatedTransaction = client.call(
                "simulateTransaction",
                params,
                SimulatedTransaction.class
        );

        return simulatedTransaction;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ClusterNode> getClusterNodes() throws RpcException {
        List<Object> params = new ArrayList<Object>();

        // TODO - fix uncasted type stuff
        List<AbstractMap> rawResult = client.call("getClusterNodes", params, List.class);

        List<ClusterNode> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ClusterNode(item));
        }

        return result;
    }

    /**
     * Returns identity and transaction information about a confirmed block in the ledger
     * DEPRECATED: use getBlock instead     
     * @param slot block number
     * @return ConfirmedBlock
     * @throws RpcException rpc exception
     */
     @Deprecated     
    public ConfirmedBlock getConfirmedBlock(int slot) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(slot);
        params.add(new BlockConfig());

        return client.call("getConfirmedBlock", params, ConfirmedBlock.class);
    }

    /**
     * Returns identity and transaction information about a confirmed block in the ledger
     * @param slot block number
     * @return block information
     * @throws RpcException rpc exception
     */
    public Block getBlock(int slot) throws RpcException {
        return getBlock(slot, null);
    }

    public Block getBlock(int slot, Map<String, Object> optionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(slot);

        if (null != optionalParams) {
            BlockConfig blockConfig = new BlockConfig();
            if (optionalParams.containsKey("commitment")) {
                Commitment commitment = (Commitment) optionalParams.get("commitment");
                blockConfig.setCommitment(commitment.getValue());
            }
            params.add(blockConfig);
        }

        return client.call("getBlock", params, Block.class);
    }


    /**
     * Returns information about the current epoch
     * @return Epoch infomation
     * @throws RpcException rpc exception
     */
    public EpochInfo getEpochInfo() throws RpcException {
        return getEpochInfo(null);
    }

    public EpochInfo getEpochInfo(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getEpochInfo", params, EpochInfo.class);
    }

    public EpochSchedule getEpochSchedule() throws RpcException {
        List<Object> params = new ArrayList<Object>();

        return client.call("getEpochSchedule", params, EpochSchedule.class);
    }

    public PublicKey getTokenAccountsByOwner(PublicKey owner, PublicKey tokenMint) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(owner.toBase58());

        Map<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("mint", tokenMint.toBase58());
        params.add(parameterMap);

        @SuppressWarnings({"unchecked"})
        Map<String, Object> rawResult = client.call("getTokenAccountsByOwner", params, Map.class);

        PublicKey tokenAccountKey;

        try {
            @SuppressWarnings({"rawtypes"})
            String base58 = (String) ((Map) ((List) rawResult.get("value")).get(0)).get("pubkey");
            tokenAccountKey = new PublicKey(base58);

        } catch (Exception ex) {
            throw new RpcException("unable to get token account by owner");
        }

        return tokenAccountKey;
    }
    
    /**
     *  owner가 소유한 모든 토큰(NFT포함) account를 리턴
     * @param owner owner 지갑 주소     
     * @return TokenAccountInfo
     * @throws RpcException rpc exception      
     */
    public TokenAccountInfo getTokenAccountsByOwnerWithProgramID(PublicKey owner) throws RpcException {
        try {            
            return client.getApi().getTokenAccountsByOwner(owner,Map.of("programId",TokenProgram.PROGRAM_ID.toBase58()),new HashMap<>());
            
        } catch(Exception e) {
            throw new RpcException(String.format("unable to get token account by owner : %s",e.toString()));
        }
    }
    public TokenAccountInfo getTokenAccountsByOwnerWithMint(PublicKey owner, PublicKey mint) throws RpcException {
        try {
            return client.getApi().getTokenAccountsByOwner(owner,Map.of("mint",mint.toBase58()),new HashMap<>());
            
        } catch(Exception e) {
            throw new RpcException(String.format("unable to get token account by owner : %s",e.toString()));
        }
    }    
    public InflationRate getInflationRate() throws RpcException {
        return client.call("getInflationRate", new ArrayList<>(), InflationRate.class);
    }

    public InflationGovernor getInflationGovernor() throws RpcException {
        return getInflationGovernor(null);
    }

    public InflationGovernor getInflationGovernor(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getInflationGovernor", params, InflationGovernor.class);
    }

    public List<InflationReward> getInflationReward(List<PublicKey> addresses) throws RpcException {
        return getInflationReward(addresses, null, null);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<InflationReward> getInflationReward(List<PublicKey> addresses, Long epoch, Commitment commitment)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(addresses.stream().map(PublicKey::toString).collect(Collectors.toList()));

        RpcEpochConfig rpcEpochConfig = new RpcEpochConfig();
        if (null != epoch) {
            rpcEpochConfig.setEpoch(epoch);
        }
        if (null != commitment) {
            rpcEpochConfig.setCommitment(commitment.getValue());
        }
        params.add(rpcEpochConfig);

        List<AbstractMap> rawResult = client.call("getInflationReward", params, List.class);

        List<InflationReward> result = new ArrayList<>();        
        for (AbstractMap item : rawResult) {
            result.add(new InflationReward(item));
        }

        return result;
    }

    public long getSlot() throws RpcException {
        return getSlot(null);
    }

    public long getSlot(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getSlot", params, Long.class);
    }

    public PublicKey getSlotLeader() throws RpcException {
        return getSlotLeader(null);
    }

    public PublicKey getSlotLeader(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return new PublicKey(client.call("getSlotLeader", params, String.class));
    }

    public List<PublicKey> getSlotLeaders(long startSlot, long limit) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(startSlot);
        params.add(limit);

        @SuppressWarnings({"unchecked"})
        List<String> rawResult = client.call("getSlotLeaders", params, List.class);

        List<PublicKey> result = new ArrayList<>();
        for (String item : rawResult) {
            result.add(new PublicKey(item));
        }

        return result;
    }

    public long getSnapshotSlot() throws RpcException {
        return client.call("getSnapshotSlot", new ArrayList<>(), Long.class);
    }

    public long getMaxShredInsertSlot() throws RpcException {
        return client.call("getMaxShredInsertSlot", new ArrayList<>(), Long.class);
    }

    public PublicKey getIdentity() throws RpcException {
        @SuppressWarnings({"unchecked"})
        Map<String, Object> rawResult = client.call("getIdentity", new ArrayList<>(), Map.class);

        PublicKey identity;
        try {
            String base58 = (String) rawResult.get("identity");
            identity = new PublicKey(base58);

        } catch (Exception ex) {
            throw new RpcException("unable to get identity");
        }

        return identity;
    }

    public Supply getSupply() throws RpcException {
        return getSupply(null);
    }

    public Supply getSupply(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getSupply", params, Supply.class);
    }

    public long getFirstAvailableBlock() throws RpcException {
        return client.call("getFirstAvailableBlock", new ArrayList<>(), Long.class);
    }

    public String getGenesisHash() throws RpcException {
        return client.call("getGenesisHash", new ArrayList<>(), String.class);
    }

    /**
     * Returns a list of confirmed blocks between two slots
     * DEPRECATED: use getBlocks instead
     * @param start start slot, as u64 integer
     * @param end (optional) end slot, as u64 integer
     * @return  list confirmed blocks between start slot and either end slot
     * @throws RpcException rpc exception
     */
    @SuppressWarnings({ "unchecked"})
    @Deprecated
    public List<Double> getConfirmedBlocks(Integer start, Integer end) throws RpcException {
        List<Object> params;
        params = (end == null ? Arrays.asList(start) : Arrays.asList(start, end));
        return this.client.call("getConfirmedBlocks", params, List.class);
    }
    /**
     * Returns a list of confirmed blocks between two slots
     * DEPRECATED: use getBlocks instead
     * @param start start solot
     * @return comfirmed block numbers form start slot rto current
     * @throws RpcException rpc exception
     */
    @Deprecated
    public List<Double> getConfirmedBlocks(Integer start) throws RpcException {
        return this.getConfirmedBlocks(start, null);
    }

    public TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount) throws RpcException {
        return getTokenAccountBalance(tokenAccount, null);
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(tokenAccount.toString());

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }
        Map<String, Object> rawResult = client.call("getTokenAccountBalance", params, Map.class);

        return new TokenAmountInfo((AbstractMap) rawResult.get("value"));
    }

    public TokenAmountInfo getTokenSupply(PublicKey tokenMint) throws RpcException {
        return getTokenSupply(tokenMint, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TokenAmountInfo getTokenSupply(PublicKey tokenMint, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(tokenMint.toString());

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }
        Map<String, Object> rawResult =  client.call("getTokenSupply", params, Map.class);

        return new TokenAmountInfo((AbstractMap) rawResult.get("value"));
    }

    public List<TokenAccount> getTokenLargestAccounts(PublicKey tokenMint) throws RpcException {
        return getTokenLargestAccounts(tokenMint, null);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public List<TokenAccount> getTokenLargestAccounts(PublicKey tokenMint, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(tokenMint.toString());

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }
        
        
        Map<String, Object> rawResult = client.call("getTokenLargestAccounts", params, Map.class);
        
        List<TokenAccount> result = new ArrayList<>();
        for (AbstractMap item : (List<AbstractMap>) rawResult.get("value")) {
            result.add(new TokenAccount(item));
        }

        return result;
    }

    public TokenAccountInfo getTokenAccountsByOwner(PublicKey accountOwner, Map<String, Object> requiredParams,
            Map<String, Object> optionalParams) throws RpcException {
        return getTokenAccount(accountOwner, requiredParams, optionalParams, "getTokenAccountsByOwner");
    }

    public TokenAccountInfo getTokenAccountsByDelegate(PublicKey accountDelegate, Map<String, Object> requiredParams,
            Map<String, Object> optionalParams) throws RpcException {
        return getTokenAccount(accountDelegate, requiredParams, optionalParams, "getTokenAccountsByDelegate");
    }

    private TokenAccountInfo getTokenAccount(PublicKey account, Map<String, Object> requiredParams,
            Map<String, Object> optionalParams, String method) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(account.toString());

        // Either mint or programId is required
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        if (requiredParams.containsKey("mint")) {
            parameterMap.put("mint", requiredParams.get("mint").toString());
        } else if (requiredParams.containsKey("programId")) {
            parameterMap.put("programId", requiredParams.get("programId").toString());
        } else {
            throw new RpcException("mint or programId are mandatory parameters");
        }
        params.add(parameterMap);

        if (null != optionalParams) {
            parameterMap = new LinkedHashMap<>();
            parameterMap.put("encoding", optionalParams.getOrDefault("encoding", "jsonParsed"));
            if (optionalParams.containsKey("commitment")) {
                Commitment commitment = (Commitment) optionalParams.get("commitment");
                parameterMap.put("commitment", commitment.getValue());
            }
            if (optionalParams.containsKey("dataSlice")) {
                parameterMap.put("dataSlice", optionalParams.get("dataSlice"));
            }
            params.add(parameterMap);
        }

        return client.call(method, params, TokenAccountInfo.class);
    }

    public VoteAccounts getVoteAccounts() throws RpcException {
        return getVoteAccounts(null, null);
    }

    public VoteAccounts getVoteAccounts(PublicKey votePubkey, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        VoteAccountConfig voteAccountConfig = new VoteAccountConfig();
        if (votePubkey != null) {
            voteAccountConfig.setVotePubkey(votePubkey.toBase58());
        }
        if (commitment != null) {
            voteAccountConfig.setCommitment(commitment.getValue());
        }
        params.add(voteAccountConfig);

        return client.call("getVoteAccounts", params, VoteAccounts.class);
    }

    public StakeActivation getStakeActivation(PublicKey publicKey) throws RpcException {
        return getStakeActivation(publicKey, null, null);
    }

    public StakeActivation getStakeActivation(PublicKey publicKey, Long epoch, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(publicKey.toBase58());

        RpcEpochConfig rpcEpochConfig = new RpcEpochConfig();
        if (null != epoch) {
            rpcEpochConfig.setEpoch(epoch);
        }
        if (null != commitment) {
            rpcEpochConfig.setCommitment(commitment.getValue());
        }
        params.add(rpcEpochConfig);

        return client.call("getStakeActivation", params, StakeActivation.class);
    }

    public SignatureStatuses getSignatureStatuses(List<String> signatures, boolean searchTransactionHistory)
            throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(signatures);
        params.add(new SignatureStatusConfig(searchTransactionHistory));

        return client.call("getSignatureStatuses", params, SignatureStatuses.class);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<PerformanceSample> getRecentPerformanceSamples() throws RpcException {
        List<Object> params = new ArrayList<>();

        List<AbstractMap> rawResult = client.call("getRecentPerformanceSamples", params, List.class);

        List<PerformanceSample> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new PerformanceSample(item));
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<PerformanceSample> getRecentPerformanceSamples(int limit) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(limit);

        List<AbstractMap> rawResult = client.call("getRecentPerformanceSamples", params, List.class);

        List<PerformanceSample> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new PerformanceSample(item));
        }

        return result;
    }

    // Throws an exception if not healthy
    public boolean getHealth() throws RpcException {
        List<Object> params = new ArrayList<>();
        String result = client.call("getHealth", params, String.class);
        return result.equals("ok");
    }

    public List<LargeAccount> getLargestAccounts() throws RpcException {
        return getLargestAccounts(null, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<LargeAccount> getLargestAccounts(String filter, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        LargestAccountConfig largestAccountConfig = new LargestAccountConfig();
        if (null != filter) {
            largestAccountConfig.setFilter(filter);
        }
        if (null != commitment) {
            largestAccountConfig.setCommitment(commitment.getValue());
        }
        params.add(largestAccountConfig);

        Map<String, Object> rawResult = client.call("getLargestAccounts", params, Map.class);

        List<LargeAccount> result = new ArrayList<>();
        for (AbstractMap item : (List<AbstractMap>) rawResult.get("value")) {
            result.add(new LargeAccount(item));
        }

        return result;
    }

    public List<LeaderSchedule> getLeaderSchedule() throws RpcException {
        return getLeaderSchedule(null, null, null);
    }

    @SuppressWarnings({ "unchecked"})
    public List<LeaderSchedule> getLeaderSchedule(Long epoch, String identity, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != epoch) {
            params.add(epoch);
        }

        LeaderScheduleConfig leaderScheduleConfig = new LeaderScheduleConfig();
        if (null != identity) {
            leaderScheduleConfig.setIdentity(identity);
        }
        if (null != commitment) {
            leaderScheduleConfig.setCommitment(commitment.getValue());
        }
        params.add(leaderScheduleConfig);

        Map<String, Object> rawResult = client.call("getLeaderSchedule", params, Map.class);

        List<LeaderSchedule> result = new ArrayList<>();
        rawResult.forEach((key, value) -> {
            result.add(new LeaderSchedule(key, (List<Double>) value));
        });

        return result;
    }

    public List<AccountInfo.Value> getMultipleAccounts(List<PublicKey> publicKeys) throws RpcException {
        return getMultipleAccounts(publicKeys, new LinkedHashMap<>());
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<AccountInfo.Value> getMultipleAccounts(List<PublicKey> publicKeys, Map<String, Object> additionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(publicKeys.stream().map(PublicKey::toBase58).collect(Collectors.toList()));

        Map<String, Object> parameterMap = new LinkedHashMap<>();

        parameterMap.put("encoding", additionalParams.getOrDefault("encoding", "base64"));

        if (additionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment) additionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }
        if (additionalParams.containsKey("dataSlice")) {
            parameterMap.put("dataSlice", additionalParams.get("dataSlice"));
        }

        params.add(parameterMap);

        Map<String, Object> rawResult = client.call("getMultipleAccounts", params, Map.class);
        List<AccountInfo.Value> result = new ArrayList<>();

        for (AbstractMap item : (List<AbstractMap>) rawResult.get("value")) {
            if (item != null) {
                result.add(new AccountInfo.Value(item));
            }
        }

        return result;
    }

}
