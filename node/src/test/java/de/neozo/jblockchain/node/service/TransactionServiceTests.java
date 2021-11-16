package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.Signatures;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        keyPair = Signatures.generateKeyPair();
        address = new Address("Max Mustermann", keyPair.getPublic().getEncoded());
        addressService.add(address);
    }

    @Test
    public void addTransaction_valid() throws Exception {
        var text = "Lorem Ipsum";
        var signature = Signatures.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
        var transaction = new Transaction(text, address.getHash(), signature, Clock.systemUTC());

        assertTrue(transactionService.add(transaction));
    }

    @Test
    public void addTransaction_invalidText() throws Exception {
        var text = "Lorem Ipsum";
        var signature = Signatures.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
        var transaction = new Transaction("Fake text!!!", address.getHash(), signature, Clock.systemUTC());

        assertFalse(transactionService.add(transaction));
    }

    @Test
    public void addTransaction_invalidSender() throws Exception {
        var addressPresident = new Address("Mr. President", Signatures.generateKeyPair().getPublic().getEncoded());
        addressService.add(addressPresident);

        var text = "Lorem Ipsum";
        var signature = Signatures.sign(text.getBytes(), keyPair.getPrivate().getEncoded());
        var transaction = new Transaction(text, addressPresident.getHash(), signature, Clock.systemUTC());

        assertFalse(transactionService.add(transaction));
    }
}
