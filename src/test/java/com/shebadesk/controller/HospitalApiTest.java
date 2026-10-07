package com.shebadesk.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HospitalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void patients_createGetDuplicate() throws Exception {
        String email = "it" + System.nanoTime() + "@test.com";
        String body = """
                {"name":"IT Patient","email":"%s","location":"Dhaka","dateOfBirth":"1990-01-01","registeredDate":"%s"}
                """.formatted(email, LocalDate.now());

        String id = mockMvc.perform(post("/patients").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/patients/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(post("/patients").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail", containsString(email)));
    }

    @Test
    void patients_validationAndNotFound() throws Exception {
        mockMvc.perform(post("/patients").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"bad","location":"Dhaka","dateOfBirth":"1990-01-01","registeredDate":"%s"}
                                """.formatted(LocalDate.now())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists());

        mockMvc.perform(get("/patients/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void appointments_bookAndRejectDoubleBooking() throws Exception {
        String doctorBody = """
                {"name":"Dr IT","specialty":"QA","email":"drit%s@test.com"}
                """.formatted(System.nanoTime());
        String doctorId = mockMvc.perform(post("/doctors").contentType(MediaType.APPLICATION_JSON).content(doctorBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        String patientBody = """
                {"name":"IT P2","email":"itp2%s@test.com","location":"Dhaka","dateOfBirth":"1991-02-02","registeredDate":"%s"}
                """.formatted(System.nanoTime(), LocalDate.now());
        String patientId = mockMvc.perform(post("/patients").contentType(MediaType.APPLICATION_JSON).content(patientBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        String date = LocalDate.now().plusDays(30).toString();
        String booking = """
                {"doctorId":%s,"patientId":"%s","appointmentDate":"%s","notes":"checkup"}
                """.formatted(doctorId, patientId, date);

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(booking))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctorId").value(doctorId));

        mockMvc.perform(post("/appointments").contentType(MediaType.APPLICATION_JSON).content(booking))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void doctors_deleteMissing_returns404() throws Exception {
        mockMvc.perform(delete("/doctors/999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
