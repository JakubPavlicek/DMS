package com.dms.integration.controller;

import com.dms.config.SecurityConfig;
import com.dms.controller.AuthController;
import com.dms.dto.UserLoginDTO;
import com.dms.service.AuthService;
import com.dms.service.UserService;
import com.dms.util.KeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, KeyManager.class})
@AutoConfigureMockMvc
class AuthControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    // needed for application context load
    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private UserLoginDTO userLogin;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .alwaysDo(print())
            .build();

        userLogin = new UserLoginDTO("james@gmail.com", "secret123!");
    }

    @Test
    void shouldReturnToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqYW1lc0BnbWFpbC5jb20iLCJleHAiOjE3MDg0MjgzMDQsImlhdCI6MTcwODQyNDcwNH0.jn7i31LH_syciaXwBcJG6D33vJ3vUQtPPeuSjUm4iLpP5R3SakkwZIZTR9ZAmMSlmrQYkWe-DQDhKLkzCWReLD3MRa66zFe1Bdd38QjYg1DRRsdZ5EpvluDOCXaZOD39pcbYYJtSTac6X22SKBYVrlLH_XlF9v8GlWNXAhhQdaEfpKm5frC3MU9mBSvz8GcOsIKro_Fa6eiSKWVWztA5CdLjgoO2RWwkQIihuBj4OjuiqwX8moUT_HQMXtarMrJNxkBlTZCPr49XiAHwGygIYz8vZ1eGT23JUc7qc6bjTTRjOq7BKRCX3PBW2zqqA4U-Wevcyo_enhqYLUSCt9uZ7w";

        when(authService.token(userLogin.getEmail(), userLogin.getPassword())).thenReturn(token);

        mvc.perform(post("/oauth2/token")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                            "password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.token").value(token)
           );
    }

    @Test
    void shouldNotReturnTokenWhenEmailIsNotProvided() throws Exception {
        mvc.perform(post("/oauth2/token")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("email: must not be null")
           );
    }

    @Test
    void shouldNotReturnTokenWhenPasswordIsNotProvided() throws Exception {
        mvc.perform(post("/oauth2/token")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "email": "james@gmail.com"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("password: must not be null")
           );
    }

    @Test
    void shouldNotReturnTokenWhenRequestLoginDoesNotContainAnyData() throws Exception {
        mvc.perform(post("/oauth2/token"))
           .andExpectAll(
               status().isUnsupportedMediaType(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("Request must contain data")
           );
    }

}