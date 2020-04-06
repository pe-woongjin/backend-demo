package com.bespin.demo.controller;

import com.bespin.demo.model.Product;
import com.bespin.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void ut_add() throws Exception {
        // given
        final Product product = new Product("ming", "dev", "it", 1000, System.currentTimeMillis());

        // when
        when(this.productService.addProduct(product)).thenReturn(product);

        // then
        final ResultActions action = this.mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(product))
                .accept(MediaType.APPLICATION_JSON));
        action.andExpect(status().isCreated());
    }

    @Test
    void ut_get() throws Exception {
        // given
        final String id = "hello";
        final long dtm = System.currentTimeMillis();
        final Product product = new Product(id, "world", "programming", 100, dtm);

        // when
        when(this.productService.getProduct("hello")).thenReturn(product);

        // then
        final ResultActions action = this.mockMvc.perform(get("/api/v1/products/{id}", id).accept(MediaType.APPLICATION_JSON));
        action.andExpect(status().isOk());
        action.andExpect(jsonPath("$.name").value("world"));
        action.andExpect(jsonPath("$.regdtm").value(dtm));
    }

    @Test
    void ut_list() throws Exception {
        // given
        final List<Product> list = new ArrayList<>();
        list.add(new Product("hello1", "world1", "programming1", 100, System.currentTimeMillis()));
        list.add(new Product("hello2", "world2", "programming2", 100, System.currentTimeMillis()));

        // when
        when(this.productService.getProducts()).thenReturn(list);

        // then
        final ResultActions action = this.mockMvc.perform(get("/api/v1/products").accept(MediaType.APPLICATION_JSON));
        action.andExpect(status().isOk());
        action.andExpect(jsonPath("$.[0].name").value("world1"));
        action.andExpect(jsonPath("$.[1].name").value("world2"));
    }

    public static String toJson(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}