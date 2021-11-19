package de.neozo.jblockchain.node.rest;


import de.neozo.jblockchain.common.domain.Block;
import de.neozo.jblockchain.node.service.BlockService;
import de.neozo.jblockchain.node.service.MiningService;
import de.neozo.jblockchain.node.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;


@RestController
@RequestMapping("block")
public class BlockController {

    private static final Logger LOG = LoggerFactory.getLogger(BlockController.class);

    private final BlockService blockService;
    private final NodeService nodeService;
    private final MiningService miningService;

    @Autowired
    public BlockController(BlockService blockService, NodeService nodeService, MiningService miningService) {
        this.blockService = blockService;
        this.nodeService = nodeService;
        this.miningService = miningService;
    }

    /**
     * Retrieve all Blocks in order of mine date, also known as Blockchain
     *
     * @return JSON list of Blocks
     */
    @GetMapping
    Iterable<Block> getBlockchain() {
        return blockService.getBlockchain()::iterator;
    }

    /**
     * Add a new Block at the end of the Blockchain.
     * It is expected that the Block is valid, see BlockService.verify(Block) for details.
     *
     * @param block    the Block to add
     * @param publish  if true, this Node is going to inform all other Nodes about the new Block
     * @param response Status Code 202 if Block accepted, 406 if verification fails
     */
    @PutMapping
    void addBlock(@RequestBody Block block, @RequestParam(required = false) Boolean publish, HttpServletResponse response) {
        LOG.info("Add block " + Base64.getEncoder().encodeToString(block.getHash()));
        var success = blockService.append(block);

        if (success) {
            response.setStatus(SC_ACCEPTED);

            if (publish != null && publish) {
                nodeService.broadcastPut("block", block);
            }
        } else {
            response.setStatus(SC_NOT_ACCEPTABLE);
        }
    }

    /**
     * Start mining of Blocks on this Node in a Thread
     */
    @RequestMapping(path = "start-miner")
    public void startMiner() {
        miningService.startMiner();
    }

    /**
     * Stop mining of Blocks on this Node
     */
    @RequestMapping(path = "stop-miner")
    public void stopMiner() {
        miningService.stopMiner();
    }

}
