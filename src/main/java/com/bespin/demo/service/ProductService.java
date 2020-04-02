package com.bespin.demo.service;

import com.bespin.demo.model.Product;
import net.sf.ehcache.Ehcache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String CACHE_NAME = "productCache";

    private CacheManager cacheManager;

    public ProductService(@Autowired CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @CachePut(cacheNames = CACHE_NAME, key = "#product.id")
    public Product addProduct(final Product product) {
        return product;
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#id")
    public Product getProduct(final String id) {
        return new Product();
    }

    public List<Product> getProducts() {
        final List<Product> list = new ArrayList<>();
        final Ehcache ehcache = (Ehcache) cacheManager.getCache(CACHE_NAME).getNativeCache();

        final List<?> keys = ehcache.getKeys();
        if (keys == null) {
            return list;
        }
        return keys.stream().limit(10).map(v -> (String) v).map(v -> (Product) ehcache.get(v).getObjectValue()).collect(Collectors.toList());
    }
}