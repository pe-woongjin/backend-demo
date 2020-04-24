package com.bespin.demo.controller;

import com.bespin.demo.model.Product;
import com.bespin.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class ProductController {

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private ProductService productService;

    public ProductController(@Autowired ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(value = "/health")
    public ResponseEntity health() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(value = "/api/v1/products")
    public ResponseEntity<?> add(@RequestBody Product product) throws Exception {
        product.setId(UUID.randomUUID().toString());

        productService.addProduct(product);

        logger.info("add : {}", new ObjectMapper().writeValueAsString(product));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "/api/v1/products/{id}")
    public ResponseEntity<Product> get(@PathVariable(name = "id", required = true) final String id) throws Exception {
        Product product = productService.getProduct(id);

        logger.info("get : {}", new ObjectMapper().writeValueAsString(product));

        return ResponseEntity.ok(product);
    }

    @GetMapping(value = "/api/v1/products")
    public ResponseEntity<List<Product>> list() throws Exception {
        List<Product> list = productService.getProducts();

        logger.info("list : {}", new ObjectMapper().writeValueAsString(list));

        return ResponseEntity.ok(list);
    }
}