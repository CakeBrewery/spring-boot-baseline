package com.samueln.spring_boot_baseline.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@Testcontainers
class FavoriteIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FavoriteStockRepository favoriteStockRepository;

    private ObjectMapper objectMapper;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        favoriteStockRepository.deleteAll();
        userRepository.deleteAll();

        testUser = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        userRepository.save(testUser);
    }

    @Test
    void shouldAddAndGetFavorite() throws Exception {
        AddFavoriteRequest request = new AddFavoriteRequest("AAPL");

        mockMvc.perform(post("/api/users/" + testUser.getId() + "/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/" + testUser.getId() + "/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].symbol", is("AAPL")));
    }

    @Test
    void shouldNotAddDuplicateFavorite() throws Exception {
        AddFavoriteRequest request = new AddFavoriteRequest("AAPL");

        mockMvc.perform(post("/api/users/" + testUser.getId() + "/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/" + testUser.getId() + "/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRemoveFavorite() throws Exception {
        AddFavoriteRequest request = new AddFavoriteRequest("AAPL");
        mockMvc.perform(post("/api/users/" + testUser.getId() + "/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/users/" + testUser.getId() + "/favorites/AAPL"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + testUser.getId() + "/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
         mockMvc.perform(get("/api/users/" + UUID.randomUUID() + "/favorites"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void shouldReturn404WhenRemovingNonExistentFavorite() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId() + "/favorites/GOOGL"))
                .andExpect(status().isNotFound());
    }
}
