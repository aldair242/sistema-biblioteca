package com.biblioteca.dao;

import com.biblioteca.dao.impl.ConfiguracionDAOImpl;
import com.biblioteca.model.Configuracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionDAOTest extends BaseDAOTest {

    private final ConfiguracionDAO configuracionDAO = new ConfiguracionDAOImpl();

    @Test
    @DisplayName("CRUD y operaciones de Configuración en transacción")
    void testConfiguracionOperaciones() {
        // 1. Guardar o actualizar parámetro de prueba
        String clavePrueba = "PARAMETRO_TEST_TDD";
        configuracionDAO.guardar(clavePrueba, "99.99");

        // 2. Obtener parámetro
        Optional<String> valor = configuracionDAO.obtener(clavePrueba);
        assertTrue(valor.isPresent());
        assertEquals("99.99", valor.get());

        // 3. Listar como lista
        List<Configuracion> lista = configuracionDAO.listar();
        assertFalse(lista.isEmpty());

        // 4. Listar como mapa
        Map<String, String> mapa = configuracionDAO.listarComoMapa();
        assertTrue(mapa.containsKey(clavePrueba));
        assertEquals("99.99", mapa.get(clavePrueba));

        // 5. Modificar parámetro
        configuracionDAO.guardar(clavePrueba, "123.45");
        assertEquals("123.45", configuracionDAO.obtener(clavePrueba).orElseThrow());
    }
}

