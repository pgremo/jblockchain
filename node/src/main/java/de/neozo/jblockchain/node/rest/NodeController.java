package de.neozo.jblockchain.node.rest;


import de.neozo.jblockchain.common.domain.Node;
import de.neozo.jblockchain.node.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;


@RestController
@RequestMapping("node")
public class NodeController {

    private final static Logger LOG = LoggerFactory.getLogger(NodeController.class);

    private final NodeService nodeService;

    @Autowired
    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Get all Nodes this node knows
     * @return JSON list of addresses
     */
    @GetMapping()
    Set<Node> getNodes() {
        return nodeService.getKnownNodes();
    }

    /**
     * Add a new Node
     * @param node the Node to add
     */
    @PutMapping
    void addNode(@RequestBody Node node) {
        LOG.info("Add node " + node.address());
        nodeService.add(node);
    }

    /**
     * Remove a Node
     * @param node the Node to remove
     */
    @DeleteMapping()
    void removeNode(@RequestBody Node node) {
        LOG.info("Remove node " + node.address());
        nodeService.remove(node);
    }

    /**
     * Helper to determine the external address for new Nodes.
     * @param request HttpServletRequest
     * @return the remote address
     */
    @RequestMapping(path = "ip")
    String getIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

}
