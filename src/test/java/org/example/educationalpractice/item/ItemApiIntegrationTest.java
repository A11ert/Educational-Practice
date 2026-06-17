package org.example.educationalpractice.item;

import org.example.educationalpractice.item.dto.ItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class ItemApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void createItemReturnsCreatedWithBody() throws Exception {
        ItemRequest request = new ItemRequest("Road bike", "Lightweight", new BigDecimal("499.99"), Set.of("sport"));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is("Road bike")))
                .andExpect(jsonPath("$.price", is(499.99)))
                .andExpect(jsonPath("$.favorite", is(false)));
    }

    @Test
    void createItemReturnsBadRequestWhenTitleBlank() throws Exception {
        ItemRequest request = new ItemRequest("  ", "No title", new BigDecimal("10.00"), Set.of());

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("title")));
    }

    @Test
    void getItemByIdReturnsItem() throws Exception {
        Long id = persistItem("Lamp", "Desk lamp", "25.00", Set.of("home")).getId();

        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Lamp")));
    }

    @Test
    void getItemByIdReturnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/api/items/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void updateItemReplacesFields() throws Exception {
        Long id = persistItem("Old", "Old desc", "10.00", Set.of("a")).getId();
        ItemRequest request = new ItemRequest("New", "New desc", new BigDecimal("20.00"), Set.of("b"));

        mockMvc.perform(put("/api/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("New")))
                .andExpect(jsonPath("$.price", is(20.00)));
    }

    @Test
    void deleteItemReturnsNoContent() throws Exception {
        Long id = persistItem("Delete me", "desc", "5.00", Set.of()).getId();

        mockMvc.perform(delete("/api/items/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/items/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listItemsReturnsPagedResults() throws Exception {
        persistItem("Item A", "desc", "10.00", Set.of());
        persistItem("Item B", "desc", "20.00", Set.of());

        mockMvc.perform(get("/api/items").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.size", is(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void searchByKeywordMatchesTitleOrDescription() throws Exception {
        persistItem("Mountain bike", "great trails", "300.00", Set.of());
        persistItem("Office chair", "comfortable", "120.00", Set.of());

        mockMvc.perform(get("/api/items").param("keyword", "bike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Mountain bike")));
    }

    @Test
    void searchByPriceRangeFiltersItems() throws Exception {
        persistItem("Cheap", "desc", "10.00", Set.of());
        persistItem("Mid", "desc", "50.00", Set.of());
        persistItem("Expensive", "desc", "200.00", Set.of());

        mockMvc.perform(get("/api/items")
                        .param("minPrice", "20")
                        .param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Mid")));
    }

    @Test
    void searchByTagReturnsMatchingItems() throws Exception {
        persistItem("Tagged", "desc", "10.00", Set.of("electronics"));
        persistItem("Untagged", "desc", "10.00", Set.of("home"));

        mockMvc.perform(get("/api/items").param("tags", "electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("Tagged")));
    }

    @Test
    void searchWithMinPriceAboveMaxPriceReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/items")
                        .param("minPrice", "100")
                        .param("maxPrice", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void favoriteFlowMarksListsAndUnmarks() throws Exception {
        Long id = persistItem("Favourite", "desc", "10.00", Set.of()).getId();

        mockMvc.perform(put("/api/items/{id}/favorite", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite", is(true)));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(id.intValue())));

        mockMvc.perform(delete("/api/items/{id}/favorite", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite", is(false)));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    private Item persistItem(String title, String description, String price, Set<String> tags) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setPrice(new BigDecimal(price));
        item.setTags(new LinkedHashSet<>(tags));
        return itemRepository.save(item);
    }
}
