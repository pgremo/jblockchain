package de.neozo.jblockchain.node.rest;


import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.node.service.AddressService;
import de.neozo.jblockchain.node.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Collection;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@RestController()
@RequestMapping("address")
public class AddressController {

    private final static Logger LOG = LoggerFactory.getLogger(AddressController.class);

    private final AddressService addressService;
    private final NodeService nodeService;

    @Autowired
    public AddressController(AddressService addressService, NodeService nodeService) {
        this.addressService = addressService;
        this.nodeService = nodeService;
    }

    /**
     * Get all Addresses this node knows
     *
     * @return JSON list of Addresses
     */
    @GetMapping
    Collection<Address> getAdresses() {
        return addressService.getAll();
    }


    /**
     * Add a new Address
     *
     * @param address the Address to add
     * @param publish if true, this Node is going to inform all other Nodes about the new Address
     */
    @PutMapping
    void addAddress(@RequestBody Address address, @RequestParam(required = false) Boolean publish) {
        LOG.info("Add address " + Base64.getEncoder().encodeToString(address.getHash()));

        if (addressService.getByHash(address.getHash()) != null) {
            throw new ResponseStatusException(CONFLICT);
        }
        addressService.add(address);

        if (publish != null && publish) {
            nodeService.broadcastPut("address", address);
        }
    }

}
