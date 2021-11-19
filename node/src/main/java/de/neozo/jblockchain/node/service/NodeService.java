package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.addAll;


@Service
public class NodeService implements ApplicationListener<ServletWebServerInitializedEvent> {

    private final static Logger LOG = LoggerFactory.getLogger(NodeService.class);

    private final BlockService blockService;
    private final TransactionService transactionService;
    private final AddressService addressService;

    private Node self;
    private final Node masterNode;

    private final Set<Node> knownNodes = new HashSet<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public NodeService(BlockService blockService, TransactionService transactionService, AddressService addressService, Node masterNode) {
        this.blockService = blockService;
        this.transactionService = transactionService;
        this.addressService = addressService;
        this.masterNode = masterNode;
    }

    /**
     * Initial setup, query master Node for
     * - Other Nodes
     * - All Addresses
     * - Current Blockchain
     * - Transactions in pool
     * and publish self on all other Nodes
     *
     * @param event serverletContainer for port retrieval
     */
    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        // construct self node
        var host = retrieveSelfExternalHost(masterNode, restTemplate);
        var port = event.getWebServer().getPort();

        self = getSelfNode(host, port);
        LOG.info("Self address: {}", self.address());

        // download data if necessary
        if (self.equals(masterNode)) {
            LOG.info("Running as master node, nothing to init");
        } else {
            knownNodes.add(masterNode);

            // retrieve data
            retrieveKnownNodes(masterNode, restTemplate);
            addressService.retrieveAddresses(masterNode, restTemplate);
            blockService.retrieveBlockchain(masterNode, restTemplate);
            transactionService.retrieveTransactions(masterNode, restTemplate);

            // publish self
            broadcastPut("node", self);
        }
    }

    /**
     * Logout from every other Node before shutdown
     */
    @PreDestroy
    public void shutdown() {
        LOG.info("Shutting down");
        broadcastPost("node/remove", self);
        LOG.info("{} informed", knownNodes.size());
    }


    public Stream<Node> getKnownNodes() {
        return knownNodes.stream();
    }

    public synchronized void add(Node node) {
        knownNodes.add(node);
    }

    public synchronized void remove(Node node) {
        knownNodes.remove(node);
    }

    /**
     * Invoke a PUT request on all other Nodes
     *
     * @param endpoint the endpoint for this request
     * @param data     the data to send
     */
    public void broadcastPut(String endpoint, Object data) {
        knownNodes.parallelStream().forEach(node -> {
            try {
                restTemplate.put(new URL(node.address(), endpoint).toURI(), data);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Invoke a POST request on all other Nodes
     *
     * @param endpoint the endpoint for this request
     * @param data     the data to send
     */
    public void broadcastPost(String endpoint, Object data) {
        knownNodes.parallelStream().forEach(node -> {
            try {
                restTemplate.postForLocation(new URL(node.address(), endpoint).toURI(), data);
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Download Nodes from other Node and add them to known Nodes
     *
     * @param node         Node to query
     * @param restTemplate RestTemplate to use
     */
    public void retrieveKnownNodes(Node node, RestTemplate restTemplate) {
        try {
            var nodes = restTemplate.getForObject(new URL(node.address(), "node").toURI(), Node[].class);
            if (nodes == null) nodes = new Node[0];
            addAll(knownNodes, nodes);
            LOG.info("Retrieved {} nodes from node {}" , nodes.length,  node.address());
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String retrieveSelfExternalHost(Node node, RestTemplate restTemplate) {
        try {
            return restTemplate.getForObject(new URL(node.address(), "node/ip").toURI(), String.class);
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Node getSelfNode(String host, int port) {
        try {
            return new Node(new URL("http", host, port, ""));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
