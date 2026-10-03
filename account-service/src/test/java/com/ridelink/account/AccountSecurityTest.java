package com.ridelink.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=test-signing-key-that-is-long-enough-for-hmac-sha",
        "spring.data.mongodb.uri=mongodb://localhost:27017/account_test_db"
})
class AccountSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedProfileRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/accounts/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passengerCannotChangeRole() throws Exception {
        var passenger = new UsernamePasswordAuthenticationToken(
                "account-1", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")));

        mockMvc.perform(patch("/api/accounts/account-2/role")
                        .with(authentication(passenger))
                        .contentType("application/json")
                        .content("{\"role\":\"DRIVER\"}"))
                .andExpect(status().isForbidden());
    }
}
