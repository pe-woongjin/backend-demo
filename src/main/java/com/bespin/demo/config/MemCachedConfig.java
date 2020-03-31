package com.bespin.demo.config;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.CacheConfiguration;
import com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl;
import com.google.code.ssm.spring.ExtendedSSMCacheManager;
import com.google.code.ssm.spring.SSMCache;
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

@Configuration
@EnableCaching
public class MemCachedConfig extends CachingConfigurerSupport {

    @Value("${memcached.port}")
    private Integer port;

    @Value("${memcached.ip}")
    private String ip;

    @Bean
    public MemcachedClient memcachedClient() {
        try {
            return new MemcachedClient(new InetSocketAddress(ip, port));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
/*
    @Bean
    public CacheManager cacheManager() {

        final String servers = "127.0.0.1:11211";
        final String operationTimeout = "10000";

        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setConsistentHashing(true);
        cacheConfiguration.setUseBinaryProtocol(true);
        cacheConfiguration.setOperationTimeout(Integer.getInteger(operationTimeout));
        // Cache Name Key Prefix
        cacheConfiguration.setUseNameAsKeyPrefix(true);
        cacheConfiguration.setKeyPrefixSeparator(":");

        MemcacheClientFactoryImpl cacheClientFactory = new MemcacheClientFactoryImpl();

        AddressProvider addressProvider = new DefaultAddressProvider(servers);

        CacheFactory cacheFactory = new CacheFactory();
        cacheFactory.setCacheName("ming");
        cacheFactory.setCacheClientFactory(cacheClientFactory);
        cacheFactory.setAddressProvider(addressProvider);
        cacheFactory.setConfiguration(cacheConfiguration);

        Cache object = null;
        try {
            object = cacheFactory.getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SSMCache ssmCache = new SSMCache(object, Integer.parseInt("3000"), true);
        ArrayList ssmCaches = new ArrayList<>();
        ssmCaches.add(0, ssmCache);
        ExtendedSSMCacheManager ssmCacheManager = new ExtendedSSMCacheManager();
        ssmCacheManager.setCaches(ssmCaches);
        return ssmCacheManager;
    }*/
}

/*

@Configuration
public class MemCachedConfig extends AbstractSSMConfiguration {

    @Bean
    public CacheFactory defaultMemcachedClient() {
        final CacheConfiguration conf = new CacheConfiguration();
        conf.setConsistentHashing(true);

        final CacheFactory cf = new CacheFactory();
        cf.setCacheClientFactory(new com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl());
        cf.setAddressProvider(new DefaultAddressProvider("127.0.0.1:11211"));
        cf.setConfiguration(conf);
        return cf;
    }
}*/
