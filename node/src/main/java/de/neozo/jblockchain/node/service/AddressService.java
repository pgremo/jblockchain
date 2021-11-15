package de.neozo.jblockchain.node.service;


import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.common.domain.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;


@Service
public class AddressService {

    private final static Logger LOG = LoggerFactory.getLogger(AddressService.class);

    /**
     * Mapping of Address hash -> Address object
     */
    private final Map<String, Address> addresses = new HashMap<>();

    /**
     * Get a specific Address
     * @param hash hash of Address
     * @return Matching Address for hash
     */
    public Address getByHash(byte[] hash) {
        return addresses.get(Base64.getEncoder().encodeToString(hash));
    }

    /**
     * Return all Addresses from map
     * @return Collection of Addresses
     */
    public Collection<Address> getAll() {
        return addresses.values();
    }

    /**
     * Add a new Address to the map
     * @param address Address to add
     */
    public synchronized void add(Address address) {
        addresses.put(Base64.getEncoder().encodeToString(address.getHash()), address);
    }

    /**
     * Download Addresses from other Node and them to the map
     * @param node Node to query
     * @param restTemplate RestTemplate to use
     */
    public void retrieveAddresses(Node node, RestTemplate restTemplate) {
        var addresses = restTemplate.getForObject(node.getAddress() + "/address", Address[].class);
        if (addresses == null) addresses = new Address[0];
        asList(addresses).forEach(this::add);
        LOG.info("Retrieved " + addresses.length + " addresses from node " + node.getAddress());
    }
}
