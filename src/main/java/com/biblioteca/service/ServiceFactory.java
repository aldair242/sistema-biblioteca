package com.biblioteca.service;

import com.biblioteca.dao.*;
import com.biblioteca.dao.impl.*;

/**
 * Fábrica centralizada de servicios (ServiceFactory).
 * Instancia e inyecta los DAOs correspondientes en los servicios de negocio (SOLID - Dependency Inversion).
 */
public final class ServiceFactory {

    private static ServiceFactory instancia;

    private final UsuarioDAO usuarioDAO;
    private final ClienteDAO clienteDAO;
    private final LibroDAO libroDAO;
    private final EjemplarDAO ejemplarDAO;
    private final PrestamoDAO prestamoDAO;
    private final ReservaDAO reservaDAO;
    private final MultaDAO multaDAO;
    private final ConfiguracionDAO configuracionDAO;

    private final AuthService authService;
    private final ConfiguracionService configuracionService;
    private final ClienteService clienteService;
    private final CatalogoService catalogoService;
    private final MultaService multaService;
    private final ReservaService reservaService;
    private final PrestamoService prestamoService;
    private final ReporteService reporteService;
    private final NotificacionService notificacionService;

    private ServiceFactory() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.libroDAO = new LibroDAOImpl();
        this.ejemplarDAO = new EjemplarDAOImpl();
        this.prestamoDAO = new PrestamoDAOImpl();
        this.reservaDAO = new ReservaDAOImpl();
        this.multaDAO = new MultaDAOImpl();
        this.configuracionDAO = new ConfiguracionDAOImpl();

        this.authService = new AuthService(usuarioDAO);
        this.configuracionService = new ConfiguracionService(configuracionDAO);
        this.clienteService = new ClienteService(clienteDAO, usuarioDAO, prestamoDAO, multaDAO, reservaDAO);
        this.catalogoService = new CatalogoService(libroDAO, ejemplarDAO, prestamoDAO, reservaDAO);
        this.multaService = new MultaService(multaDAO, configuracionService);
        this.reservaService = new ReservaService(reservaDAO, ejemplarDAO, configuracionService);
        this.prestamoService = new PrestamoService(prestamoDAO, ejemplarDAO, clienteDAO, multaDAO, reservaDAO,
                multaService, reservaService, configuracionService);
        this.reporteService = new ReporteService(prestamoDAO, clienteDAO, libroDAO, ejemplarDAO, multaDAO);
        this.notificacionService = new NotificacionService(clienteDAO, ejemplarDAO, libroDAO, configuracionService);
    }

    public static synchronized ServiceFactory getInstancia() {
        if (instancia == null) {
            instancia = new ServiceFactory();
        }
        return instancia;
    }

    public UsuarioDAO getUsuarioDAO() {
        return usuarioDAO;
    }

    public ClienteDAO getClienteDAO() {
        return clienteDAO;
    }

    public LibroDAO getLibroDAO() {
        return libroDAO;
    }

    public EjemplarDAO getEjemplarDAO() {
        return ejemplarDAO;
    }

    public PrestamoDAO getPrestamoDAO() {
        return prestamoDAO;
    }

    public ReservaDAO getReservaDAO() {
        return reservaDAO;
    }

    public MultaDAO getMultaDAO() {
        return multaDAO;
    }

    public ConfiguracionDAO getConfiguracionDAO() {
        return configuracionDAO;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ConfiguracionService getConfiguracionService() {
        return configuracionService;
    }

    public ClienteService getClienteService() {
        return clienteService;
    }

    public CatalogoService getCatalogoService() {
        return catalogoService;
    }

    public MultaService getMultaService() {
        return multaService;
    }

    public ReservaService getReservaService() {
        return reservaService;
    }

    public PrestamoService getPrestamoService() {
        return prestamoService;
    }

    public ReporteService getReporteService() {
        return reporteService;
    }

    public NotificacionService getNotificacionService() {
        return notificacionService;
    }
}

