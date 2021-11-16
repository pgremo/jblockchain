package de.neozo.jblockchain.node;

import de.neozo.jblockchain.common.domain.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.URL;
import java.time.Clock;


@SpringBootApplication
public class BlockchainNode {

    @Value("${master-node-address}")
    public URL masterAddress;

    public static void main(String[] args) {
        SpringApplication.run(BlockchainNode.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Node masterNode() {
        return new Node(masterAddress);
    }
}
