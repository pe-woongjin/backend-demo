package com.bespin.demo.controller;

import com.bespin.demo.model.Product;
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {

    @Autowired
    private MemcachedClient memcachedClient;

    @RequestMapping(value = "/v1/product", method = RequestMethod.GET)
    public Object list() {
        System.out.println("===getStats===");
        System.out.println(memcachedClient.getStats());

        System.out.println("===flush===");
        System.out.println(memcachedClient.flush());
        return "";
    }

    @RequestMapping(value = "/v1/product/{id}", method = RequestMethod.GET)
    public Object get(@PathVariable("id") String identifier) {
        if (memcachedClient.get(identifier) == null) {
            memcachedClient.add(identifier, 10, new Product(identifier, "", 0));
        }
        return memcachedClient.get(identifier);
    }

    @RequestMapping(value = "/v1/product/add", method = RequestMethod.POST)
    public Object add(@RequestParam String identifier, @RequestParam String name, @RequestParam int price) {
        memcachedClient.add(identifier, 10, new Product(identifier, name, price));
        return memcachedClient.get(identifier);
    }
}
