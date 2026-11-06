package com.example.demo;

import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.services.dto.ListaInputDTO;
import com.example.demo.services.dto.LoginInputDTO;
import com.example.demo.services.dto.RegistroInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthAndListaIntegrationIT extends AbstractIntegrationTest {

    private static final String PASSWORD = "password123";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ---------- Helpers ----------

    private String emailUnico() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private void registrar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegistroInputDTO(email, PASSWORD))))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginInputDTO(email, PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("token").asString();
    }

    private String registrarYLogin() throws Exception {
        String email = emailUnico();
        registrar(email);
        return login(email);
    }

    private long crearLista(String token, String nombre) throws Exception {
        String body = mockMvc.perform(post("/api/listas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ListaInputDTO(nombre))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("id").asLong();
    }

    private String crearAdminYLogin() throws Exception {
        String email = emailUnico();
        usuarioRepository.guardar(new Usuario(null, email, passwordEncoder.encode(PASSWORD), Rol.ADMIN));
        return login(email);
    }

    // ---------- Escenarios de la consigna 5 ----------

    @Test
    void login_credencialesValidas_devuelve200ConToken() throws Exception {
        String email = emailUnico();
        registrar(email);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginInputDTO(email, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_credencialesInvalidas_devuelve401() throws Exception {
        String email = emailUnico();
        registrar(email);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginInputDTO(email, "contraseña-incorrecta"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerLista_listaPropia_devuelve200() throws Exception {
        String token = registrarYLogin();
        long listaId = crearLista(token, "Mis favoritos");

        mockMvc.perform(get("/api/listas/" + listaId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerLista_listaAjena_devuelve403() throws Exception {
        String tokenDueno = registrarYLogin();
        String tokenOtro = registrarYLogin();
        long listaId = crearLista(tokenDueno, "Lista privada");

        mockMvc.perform(get("/api/listas/" + listaId)
                .header("Authorization", "Bearer " + tokenOtro))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarListas_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerLista_adminSobreListaAjena_devuelve200() throws Exception {
        String tokenDueno = registrarYLogin();
        String tokenAdmin = crearAdminYLogin();
        long listaId = crearLista(tokenDueno, "Lista de otro");

        assertFalse(tokenAdmin.isBlank());
        mockMvc.perform(get("/api/listas/" + listaId)
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    // ---------- Escenarios adicionales (consignas 2 y 4) ----------

    @Test
    void registro_emailDuplicado_devuelve409() throws Exception {
        String email = emailUnico();
        registrar(email);

        mockMvc.perform(post("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegistroInputDTO(email, PASSWORD))))
                .andExpect(status().isConflict());
    }

    @Test
    void listarListas_usuarioComun_devuelveSoloLasPropias() throws Exception {
        String tokenA = registrarYLogin();
        String tokenB = registrarYLogin();
        crearLista(tokenA, "A1");
        crearLista(tokenA, "A2");
        crearLista(tokenB, "B1");

        mockMvc.perform(get("/api/listas")
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarFavoritosDeLista_listaAjena_devuelve403() throws Exception {
        String tokenDueno = registrarYLogin();
        String tokenOtro = registrarYLogin();
        long listaId = crearLista(tokenDueno, "Lista privada");

        mockMvc.perform(get("/api/listas/" + listaId + "/favoritos")
                .header("Authorization", "Bearer " + tokenOtro))
                .andExpect(status().isForbidden());
    }

    @Test
    void moverFavoritos_origenAjeno_devuelve403() throws Exception {
        String tokenDueno = registrarYLogin();
        String tokenAtacante = registrarYLogin();
        long origenAjeno = crearLista(tokenDueno, "Origen de otro");
        long destinoPropio = crearLista(tokenAtacante, "Mi destino");

        mockMvc.perform(post("/api/listas/" + origenAjeno + "/mover-favoritos")
                .header("Authorization", "Bearer " + tokenAtacante)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"listaDestinoId\":" + destinoPropio + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearFavorito_enListaAjena_devuelve403() throws Exception {
        String tokenDueno = registrarYLogin();
        String tokenAtacante = registrarYLogin();
        long listaAjena = crearLista(tokenDueno, "Lista de otro");

        mockMvc.perform(post("/api/favoritos")
                .header("Authorization", "Bearer " + tokenAtacante)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"notaPersonal\":\"nota\",\"listaId\":" + listaAjena + "}"))
                .andExpect(status().isForbidden());
    }
}