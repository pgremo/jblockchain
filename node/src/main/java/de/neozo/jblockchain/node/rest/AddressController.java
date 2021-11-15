package de.neozo.jblockchain.node.rest;


import de.neozo.jblockchain.common.domain.Address;
import de.neozo.jblockchain.node.service.AddressService;
import de.neozo.jblockchain.node.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Collection;

import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;


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
    @RequestMapping
    Collection<Address> getAdresses() {
        return addressService.getAll();
    }


    /**
     * Add a new Address
     *
     * @param address  the Address to add
     * @param publish  if true, this Node is going to inform all other Nodes about the new Address
     * @param response Status Code 202 if Address was added, 406 if submitted hash is already present
     */
    @RequestMapping(method = RequestMethod.PUT)
    void addAddress(@RequestBody Address address, @RequestParam(required = false) Boolean publish, HttpServletResponse response) {
        LOG.info("Add address " + Base64.getEncoder().encodeToString(address.getHash()));

        if (addressService.getByHash(address.getHash()) == null) {
            addressService.add(address);

            if (publish != null && publish) {
                nodeService.broadcastPut("address", address);
            }
            response.setStatus(SC_ACCEPTED);
        } else {
            response.setStatus(SC_NOT_ACCEPTABLE);
        }
    }

}
