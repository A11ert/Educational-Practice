package org.example.educationalpractice.basket;

import org.example.educationalpractice.item.Item;
import org.example.educationalpractice.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class BasketApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ItemRepository itemRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void addItemReturnsBasketWithItemAndTotal() throws Exception {
        Long id = persistItem("Headphones", "30.00").getId();

        mockMvc.perform(post("/api/basket/items/{itemId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount", is(1)))
                .andExpect(jsonPath("$.totalPrice", is(30.00)))
                .andExpect(jsonPath("$.items[0].id", is(id.intValue())));
    }

    @Test
    void addUnknownItemReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/basket/items/{itemId}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBasketReflectsCurrentContents() throws Exception {
        Long first = persistItem("Mouse", "20.00").getId();
        Long second = persistItem("Keyboard", "40.00").getId();
        mockMvc.perform(post("/api/basket/items/{itemId}", first)).andExpect(status().isOk());
        mockMvc.perform(post("/api/basket/items/{itemId}", second)).andExpect(status().isOk());

        mockMvc.perform(get("/api/basket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount", is(2)))
                .andExpect(jsonPath("$.totalPrice", is(60.00)));
    }

    @Test
    void removeItemTakesItOutOfBasket() throws Exception {
        Long id = persistItem("Cable", "5.00").getId();
        mockMvc.perform(post("/api/basket/items/{itemId}", id)).andExpect(status().isOk());

        mockMvc.perform(delete("/api/basket/items/{itemId}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount", is(0)));
    }

    @Test
    void deletedItemDisappearsFromBasketView() throws Exception {
        Item item = persistItem("Charger", "15.00");
        mockMvc.perform(post("/api/basket/items/{itemId}", item.getId())).andExpect(status().isOk());

        itemRepository.delete(item);

        mockMvc.perform(get("/api/basket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount", is(0)));
    }

    private Item persistItem(String title, String price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("test item");
        item.setPrice(new BigDecimal(price));
        return itemRepository.save(item);
    }
}
