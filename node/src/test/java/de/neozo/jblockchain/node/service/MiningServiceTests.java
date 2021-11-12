package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.SignatureUtils;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        keyPair = SignatureUtils.generateKeyPair();
        address = new Address("Max Mustermann", keyPair.getPublic().getEncoded());
        addressService.add(address);
    }

    @Test
    @Disabled
    public void startStopMiner() throws Exception {
        final int initalTransactions = 100;
        addTransactions(initalTransactions);

        miningService.startMiner();

        while (transactionService.getTransactionPool().size() == initalTransactions) {
            Thread.sleep(1000);
        }

        miningService.stopMiner();
    }

    private void addTransactions(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            String text = "Demo Transaction " + i;
            byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
            Transaction transaction = new Transaction(text, address.getHash(), signature);

            transactionService.add(transaction);
        }
    }

}
