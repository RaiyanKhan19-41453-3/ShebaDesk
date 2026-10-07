package com.shebadesk.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shebadesk.model.AppUser;
import com.shebadesk.model.Role;
import com.shebadesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HospitalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    private String field(String json, String name) throws Exception {
        return objectMapper.readTree(json).get(name).asText();
    }

    @BeforeEach
    void setUpAdmin() throws Exception {
        if (!userRepository.existsByUsername("testadmin")) {
            AppUser admin = new AppUser();
            admin.setUsername("testadmin");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
        String body = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testadmin","password":"Admin123!"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        adminToken = field(body, "accessToken");
    }

    private static RequestPostProcessor bearer(String token) {
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }

    @Test
    void patients_createGetDuplicate() throws Exception {
        String email = "it" + System.nanoTime() + "@test.com";
        String body = """
                {"name":"IT Patient","email":"%s","location":"Dhaka","dateOfBirth":"1990-01-01","registeredDate":"%s"}
                """.formatted(email, LocalDate.now());

        String idBody = mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn().getResponse().getContentAsString();
        String id = field(idBody, "id");

        mockMvc.perform(get("/patients/" + id).with(bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", containsString(email)));
    }

    @Test
    void patients_validationAndNotFound() throws Exception {
        mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"bad","location":"Dhaka","dateOfBirth":"1990-01-01","registeredDate":"%s"}
                                """.formatted(LocalDate.now())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists());

        mockMvc.perform(get("/patients/00000000-0000-0000-0000-000000000000").with(bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void appointments_bookAndRejectDoubleBooking() throws Exception {
        String doctorBody = """
                {"name":"Dr IT","specialty":"QA","email":"drit%s@test.com"}
                """.formatted(System.nanoTime());
        String doctorBodyOut = mockMvc.perform(post("/doctors").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(doctorBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String doctorId = field(doctorBodyOut, "id");

        String patientBody = """
                {"name":"IT P2","email":"itp2%s@test.com","location":"Dhaka","dateOfBirth":"1991-02-02","registeredDate":"%s"}
                """.formatted(System.nanoTime(), LocalDate.now());
        String patientBodyOut = mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(patientBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String patientId = field(patientBodyOut, "id");

        String date = LocalDate.now().plusDays(30).toString();
        String booking = """
                {"doctorId":%s,"patientId":"%s","appointmentDate":"%s","notes":"checkup"}
                """.formatted(doctorId, patientId, date);

        mockMvc.perform(post("/appointments").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(booking))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(doctorId));

        mockMvc.perform(post("/appointments").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(booking))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void doctors_deleteMissing_returns404() throws Exception {
        mockMvc.perform(delete("/doctors/999999999").with(bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void observability_prometheusExposedWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jvm_memory_used_bytes")));
    }

    @Test
    void httpSemantics_wrongMethodTypeAndPath() throws Exception {
        mockMvc.perform(post("/patients/123").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));

        mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.TEXT_PLAIN).content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));

        mockMvc.perform(get("/no-such-endpoint").with(bearer(adminToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void patients_softDelete_hidesAndBlocksEmailReuse() throws Exception {
        String email = "softdel" + System.nanoTime() + "@test.com";
        String body = """
                {"name":"Soft Del","email":"%s","location":"Dhaka","dateOfBirth":"1990-01-01","registeredDate":"%s"}
                """.formatted(email, LocalDate.now());

        String idBody = mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn().getResponse().getContentAsString();
        String id = field(idBody, "id");

        mockMvc.perform(delete("/patients/" + id).with(bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/patients/" + id).with(bearer(adminToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/patients").with(bearer(adminToken))
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(post("/patients").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void security_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"testadmin","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void security_receptionistCannotRegister_returns403() throws Exception {
        String recepUsername = "recep" + System.nanoTime();
        String receptionistBody = """
                {"username":"%s","password":"Recep123!","role":"RECEPTIONIST"}
                """.formatted(recepUsername);
        mockMvc.perform(post("/auth/register").with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(receptionistBody))
                .andExpect(status().isCreated());

        String tokenBody = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Recep123!"}
                                """.formatted(recepUsername)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String recepToken = field(tokenBody, "accessToken");

        mockMvc.perform(post("/auth/register").with(bearer(recepToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"another%s","password":"Another123!","role":"RECEPTIONIST"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/patients").with(bearer(recepToken)))
                .andExpect(status().isOk());
    }
}
