package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.SignatureUtils;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.time.Clock;

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
        keyPair = SignatureUtils.generateKeyPair();
        address = new Address("Max Mustermann", keyPair.getPublic().getEncoded());
        addressService.add(address);
    }

    @Test
    public void startStopMiner() throws Exception {
        final var initialTransactions = 100;
        addTransactions(initialTransactions);

        miningService.startMiner();

        while (transactionService.getTransactionPool().size() == initialTransactions) {
            Thread.sleep(1000);
        }

        miningService.stopMiner();
    }

    private void addTransactions(int count) throws Exception {
        for (var i = 0; i < count; i++) {
            var text = "Demo Transaction %d".formatted(i);
            var signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
            var transaction = new Transaction(text, address.getHash(), signature, Clock.systemUTC());

            transactionService.add(transaction);
        }
    }

}
