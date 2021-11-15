package de.neozo.jblockchain.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;


@SpringBootApplication
public class BlockchainNode {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainNode.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
