package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.Signatures;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Block;
import de.neozo.jblockchain.common.domain.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BlockServiceTests {

    private final static byte[] fixedSignature = new byte[]{48, 44, 2, 20, 89, 48, -114, -49, 36, 65, 116, -5, 88, 6, -38, -110, -30, -73, 59, -53, 19, -49, 122, 90, 2, 20, 111, 38, 55, -120, -125, 17, -66, -8, -121, 85, 31, -82, -80, -31, -33, 116, 121, -90, 123, -113};

    @Autowired
    private BlockService blockService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;

    private Address address;
    private byte[] privateKey;

    @BeforeEach
    public void setUp() throws Exception {
        var classLoader = Thread.currentThread().getContextClassLoader();
        privateKey = classLoader.getResourceAsStream("key.priv").readAllBytes();
        var publicKey = classLoader.getResourceAsStream("key.pub").readAllBytes();
        address = new Address("Max Mustermann", publicKey);
        addressService.add(address);
    }

    @Test
    public void addBlock_validHash() {
        var block = new Block(null, List.of(generateStableTransaction()), 3, 20854567, 42);
        assertTrue(blockService.append(block));
    }

    @Test
    public void addBlock_invalidHash() throws Exception {
        var block = new Block(null, generateTransactions(1), 3, 42, System.currentTimeMillis());
        assertFalse(blockService.append(block));
    }

    @Test
    public void addBlock_invalidLimitExceeded() throws Exception {
        var block = new Block(null, generateTransactions(6), 3, 42, System.currentTimeMillis());
        assertFalse(blockService.append(block));
    }

    @Disabled
    @Test
    public void addBlock_generateBlock() {
        var transactions = List.of(generateStableTransaction());
        var success = false;
        var nonce = 0;
        while (!success) {
            var block = new Block(null, transactions,3, nonce, 42);
            success = blockService.append(block);
            nonce++;
        }
        System.out.println("nonce=" + (nonce - 1));
    }

    private List<Transaction> generateTransactions(int count) throws Exception {
        var transactions = new ArrayList<Transaction>();
        for (var i = 0; i < count; i++) {
            var text = "Hello %d".formatted(i);
            var signature = Signatures.sign(text.getBytes(), privateKey);
            var transaction = new Transaction(text, address.getHash(), signature, System.currentTimeMillis());

            transactionService.add(transaction);
            transactions.add(transaction);
        }
        return transactions;
    }

    private Transaction generateStableTransaction() {
        var transaction = new Transaction(
                "Hello 0",
                address.getHash(),
                fixedSignature,
                42);

        transactionService.add(transaction);
        return transaction;
    }

}
