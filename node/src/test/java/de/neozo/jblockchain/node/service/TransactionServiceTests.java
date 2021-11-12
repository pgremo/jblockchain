package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.SignatureUtils;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;

@SpringBootTest
public class TransactionServiceTests {

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
    public void addTransaction_valid() throws Exception {
        String text = "Lorem Ipsum";
        byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
        Transaction transaction = new Transaction(text, address.getHash(), signature);

        boolean success = transactionService.add(transaction);
        Assertions.assertTrue(success);
    }

    @Test
    public void addTransaction_invalidText() throws Exception {
        String text = "Lorem Ipsum";
        byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
        Transaction transaction = new Transaction("Fake text!!!", address.getHash(), signature);

        boolean success = transactionService.add(transaction);
        Assertions.assertFalse(success);
    }

    @Test
    public void addTransaction_invalidSender() throws Exception {
        Address addressPresident = new Address("Mr. President", SignatureUtils.generateKeyPair().getPublic().getEncoded());
        addressService.add(addressPresident);

        String text = "Lorem Ipsum";
        byte[] signature = SignatureUtils.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
        Transaction transaction = new Transaction(text, addressPresident.getHash(), signature);

        boolean success = transactionService.add(transaction);
        Assertions.assertFalse(success);
    }
}
