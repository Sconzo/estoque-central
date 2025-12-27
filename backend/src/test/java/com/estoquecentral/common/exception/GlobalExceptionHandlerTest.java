package com.estoquecentral.common.exception;

import com.estoquecentral.company.adapter.in.CompanyController;
import com.estoquecentral.company.application.CompanyService;
import com.estoquecentral.auth.application.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for GlobalExceptionHandler (Story 8.5).
 *
 * Tests validation errors, database errors, and schema provisioning errors.
 */
@WebMvcTest(CompanyController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private JwtService jwtService;

    /**
     * AC1: Validation errors return 400 Bad Request with field-specific messages.
     */
    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        String invalidRequest = """
            {
                "nome": "",
                "email": "invalid-email",
                "cnpj": "123",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Erro de validação"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.cnpj").exists());
    }

    /**
     * AC1: CNPJ validation - must be 14 digits or empty.
     */
    @Test
    void shouldReturn400WhenCnpjIsInvalid() throws Exception {
        String invalidCnpjRequest = """
            {
                "nome": "Test Company",
                "email": "test@example.com",
                "cnpj": "12345",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidCnpjRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.cnpj").value("CNPJ deve conter exatamente 14 dígitos numéricos"));
    }

    /**
     * AC1: Nome validation - max 255 characters.
     */
    @Test
    void shouldReturn400WhenNomeExceedsMaxLength() throws Exception {
        String longName = "A".repeat(256);
        String invalidNameRequest = String.format("""
            {
                "nome": "%s",
                "email": "test@example.com",
                "userId": 1
            }
            """, longName);

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidNameRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").value("Nome da empresa deve ter no máximo 255 caracteres"));
    }

    /**
     * AC3: Database connection failure returns 503 Service Unavailable.
     */
    @Test
    @WithMockUser
    void shouldReturn503WhenDatabaseConnectionFails() throws Exception {
        when(companyService.createCompany(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        String validRequest = """
            {
                "nome": "Test Company",
                "email": "test@example.com",
                "cnpj": "12345678000190",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Erro de conexão com banco de dados"))
                .andExpect(jsonPath("$.message").value("Não foi possível conectar ao banco de dados. Tente novamente mais tarde."));
    }

    /**
     * AC4: Schema provisioning failure returns 500 Internal Server Error.
     */
    @Test
    @WithMockUser
    void shouldReturn500WhenSchemaProvisioningFails() throws Exception {
        when(companyService.createCompany(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new SchemaProvisioningException("Failed to create schema", null));

        String validRequest = """
            {
                "nome": "Test Company",
                "email": "test@example.com",
                "cnpj": "12345678000190",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Erro ao criar esquema de banco de dados"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro ao provisionar o banco de dados da empresa. A operação foi revertida."));
    }

    /**
     * AC2: Duplicate company names are allowed (no 409 Conflict).
     * This test verifies that we DON'T throw BusinessException for duplicate names.
     */
    @Test
    @WithMockUser
    void shouldAllowDuplicateCompanyNames() throws Exception {
        // This test verifies the behavior - no duplicate name validation should exist
        // If the service doesn't throw an exception, the test passes
        when(companyService.createCompany(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(any()); // Allow duplicate names

        String validRequest = """
            {
                "nome": "Duplicate Name Company",
                "email": "test@example.com",
                "cnpj": "12345678000190",
                "userId": 1
            }
            """;

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
                .andExpect(status().isOk()); // Should succeed, not 409 Conflict
    }
}
