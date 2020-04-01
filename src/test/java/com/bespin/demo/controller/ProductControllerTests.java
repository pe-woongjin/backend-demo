package com.bespin.demo.controller;

import com.bespin.demo.model.Product;
import com.bespin.demo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProductController.class)
public class ProductControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProductService productService;

    @Test
    void ut_get() throws Exception {
        final String id = "hello";
        final Product product = new Product(id, "world", "programming", 100, System.currentTimeMillis());
        given(this.productService.getProduct("hello"))
                .willReturn(product);
        final ResultActions action = this.mvc.perform(get("/v1/products/{id}", id).accept(MediaType.APPLICATION_JSON));
        action.andExpect(status().isOk());
        action.andExpect(jsonPath("$.name").value(product.getName()));
    }

}
