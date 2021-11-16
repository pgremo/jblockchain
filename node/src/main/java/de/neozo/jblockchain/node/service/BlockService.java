package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.domain.Block;
import de.neozo.jblockchain.common.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.addAll;


@Service
public class BlockService {

    private final static Logger LOG = LoggerFactory.getLogger(BlockService.class);

    @Value("${difficulty}")
    public int difficulty;

    @Value("${max-transactions-per-block}")
    public int maxTransactionsPerBlock;

    private final TransactionService transactionService;

    private final List<Block> blockchain = new ArrayList<>();

    @Autowired
    public BlockService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public List<Block> getBlockchain() {
        return blockchain;
    }

    /**
     * Determine the last added Block
     *
     * @return Last Block in chain
     */
    public Block getLastBlock() {
        if (blockchain.isEmpty()) {
            return null;
        }
        return blockchain.get(blockchain.size() - 1);
    }

    /**
     * Append a new Block at the end of chain
     *
     * @param block Block to append
     * @return true if verifcation succeeds and Block was appended
     */
    public synchronized boolean append(Block block) {
        if (!verify(block)) return false;

        blockchain.add(block);

        // remove transactions from pool
        block.getTransactions().forEach(transactionService::remove);
        return true;
    }

    /**
     * Download Blocks from other Node and them to the blockchain
     *
     * @param node         Node to query
     * @param restTemplate RestTemplate to use
     */
    public void retrieveBlockchain(Node node, RestTemplate restTemplate) {
        var blocks = restTemplate.getForObject(node.address() + "/block", Block[].class);
        if (blocks == null) blocks = new Block[0];
        addAll(blockchain, blocks);
        LOG.info("Retrieved " + blocks.length + " blocks from node " + node.address());
    }


    private boolean verify(Block block) {
        // references last block in chain
        if (blockchain.size() > 0) {
            var lastBlockInChainHash = getLastBlock().getHash();
            if (!Arrays.equals(block.getPreviousHash(), lastBlockInChainHash)) {
                return false;
            }
        } else {
            if (block.getPreviousHash() != null) {
                return false;
            }
        }

        // correct hashes
        if (!Arrays.equals(block.getMerkleRoot(), block.calculateMerkleRoot())) {
            return false;
        }
        if (!Arrays.equals(block.getHash(), block.calculateHash())) {
            return false;
        }

        // transaction limit
        if (block.getTransactions().size() > maxTransactionsPerBlock) {
            return false;
        }

        // all transactions in pool
        if (!transactionService.containsAll(block.getTransactions())) {
            return false;
        }

        // considered difficulty
        return block.getLeadingZerosCount() >= difficulty;
    }
}
