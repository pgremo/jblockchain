package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.Signatures;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

@SpringBootTest
public class MiningServiceTests {

    @Autowired
    private MiningService miningService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;

    private Address address;
    private KeyPair keyPair;

    @BeforeEach
    public void setUp() throws Exception {
        keyPair = Signatures.generateKeyPair();
        address = new Address("Max Mustermann", keyPair.getPublic().getEncoded());
        addressService.add(address);
    }

    @Disabled
    @Test
    public void startStopMiner() throws Exception {
        final var initialTransactions = 100;
        addTransactions(initialTransactions);

        miningService.startMiner();

        while (transactionService.getTransactionPool().count() == initialTransactions) {
            Thread.sleep(1000);
        }

        miningService.stopMiner();
    }

    private void addTransactions(int count) throws Exception {
        for (var i = 0; i < count; i++) {
            var text = "Demo Transaction %d".formatted(i);
            var signature = Signatures.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
            var transaction = new Transaction(
                    text.getBytes(StandardCharsets.UTF_8),
                    address.getHash(),
                    signature,
                    System.currentTimeMillis()
            );

            transactionService.add(transaction);
        }
    }

}
