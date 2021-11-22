package de.neozo.jblockchain.client;


import de.neozo.jblockchain.common.Signatures;
import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Transaction;
import org.apache.commons.cli.*;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Simple class to help building REST calls for jBlockchain.
 * Just run it in command line for instructions on how to use it.
 * <p>
 * Functions include:
 * - Generate Private/Public-Key
 * - Publish a new Address
 * - Publish a new Transaction
 */
@SpringBootApplication
public class BlockchainClient implements CommandLineRunner {

    public static void main(String... args) {
        new SpringApplicationBuilder(BlockchainClient.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }

    private final RestTemplate restTemplate;

    public BlockchainClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        var parser = new DefaultParser();
        var options = getOptions();
        try {
            var line = parser.parse(options, args);
            executeCommand(line);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(getClass().getSimpleName(), options, true);
        }
    }

    private void executeCommand(CommandLine line) throws Exception {
        if (line.hasOption("keypair")) {
            generateKeyPair();
        } else if (line.hasOption("address")) {
            var node = line.getOptionValue("node");
            var name = line.getOptionValue("name");
            var publickey = line.getOptionValue("publickey");
            if (node == null || name == null || publickey == null) {
                throw new ParseException("node, name and publickey is required");
            }
            publishAddress(new URL(node), Paths.get(publickey), name);

        } else if (line.hasOption("transaction")) {
            var node = line.getOptionValue("node");
            var message = line.getOptionValue("message");
            var sender = line.getOptionValue("sender");
            var privatekey = line.getOptionValue("privatekey");
            if (node == null || message == null || sender == null || privatekey == null) {
                throw new ParseException("node, message, sender and privatekey is required");
            }
            publishTransaction(new URL(node), Paths.get(privatekey), message, Base64.getDecoder().decode(sender));
        }
    }

    private Options getOptions() {
        var actions = new OptionGroup()
                .addOption(new Option("k", "keypair", false, "generate private/public key pair"))
                .addOption(new Option("a", "address", false, "publish new address"))
                .addOption(new Option("t", "transaction", false, "publish new transaction"));
        actions.setRequired(true);

        var options = new Options();
        options.addOptionGroup(actions);
        options.addOption(Option.builder("o")
                .longOpt("node")
                .hasArg()
                .argName("Node URL")
                .desc("needed for address and transaction publishing")
                .build());
        options.addOption(Option.builder("n")
                .longOpt("name")
                .hasArg()
                .argName("name for new address")
                .desc("needed for address publishing")
                .build());
        options.addOption(Option.builder("p")
                .longOpt("publickey")
                .hasArg()
                .argName("path to key file")
                .desc("needed for address publishing")
                .build());
        options.addOption(Option.builder("v")
                .longOpt("privatekey")
                .hasArg()
                .argName("path to key file")
                .desc("needed for transaction publishing")
                .build());
        options.addOption(Option.builder("m")
                .longOpt("message")
                .hasArg()
                .argName("message to post")
                .desc("needed for transaction publishing")
                .build());
        options.addOption(Option.builder("s")
                .longOpt("sender")
                .hasArg()
                .argName("address hash (Base64)")
                .desc("needed for transaction publishing")
                .build());

        return options;
    }

    private void generateKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException, IOException {
        var keyPair = Signatures.generateKeyPair();
        Files.write(Paths.get("key.priv"), keyPair.getPrivate().getEncoded());
        Files.write(Paths.get("key.pub"), keyPair.getPublic().getEncoded());
    }

    private void publishAddress(URL node, Path publicKey, String name) throws IOException, URISyntaxException {
        var address = new Address(name, Files.readAllBytes(publicKey));
        restTemplate.put(new URL(node, "address?publish=true").toURI(), address);
        System.out.println("Hash of new address: " + Base64.getEncoder().encodeToString(address.getHash()));
    }

    private void publishTransaction(URL node, Path privateKey, String text, byte[] senderHash) throws IOException, URISyntaxException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        var signature = Signatures.sign(text.getBytes(), Files.readAllBytes(privateKey));
        var transaction = new Transaction(
                text.getBytes(UTF_8),
                senderHash,
                signature,
                System.currentTimeMillis()
        );
        restTemplate.put(new URL(node, "transaction?publish=true").toURI(), transaction);
        System.out.println("Hash of new transaction: " + Base64.getEncoder().encodeToString(transaction.getHash()));
    }

}
