package com.biblioteca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * Gestor de transacciones basado en ThreadLocal.
 * Permite ejecutar bloques transaccionales garantizando atomicidad (commit / rollback)
 * y suministra a los DAOs la conexión apropiada (activa compartida o independiente).
 */
public final class GestorTransaccion {

    private static final Logger logger = LoggerFactory.getLogger(GestorTransaccion.class);
    private static final ThreadLocal<Connection> conexionActual = new ThreadLocal<>();

    private GestorTransaccion() {
    }

    /**
     * Ejecuta una operación con valor de retorno dentro de una transacción.
     * Si ocurre un error, realiza rollback y propaga la excepción.
     *
     * @param accion Operación a ejecutar
     * @param <T>    Tipo de retorno
     * @return Resultado de la operación
     */
    public static <T> T ejecutar(Supplier<T> accion) {
        if (conexionActual.get() != null) {
            // Ya existe una transacción activa en este hilo
            return accion.get();
        }

        Connection conn = null;
        try {
            conn = ConexionBD.obtenerConexion();
            conn.setAutoCommit(false);
            conexionActual.set(conn);
            logger.debug("Transacción iniciada en hilo {}", Thread.currentThread().getName());

            T resultado = accion.get();

            conn.commit();
            logger.debug("Transacción confirmada (commit) con éxito");
            return resultado;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Transacción revertida (rollback) debido a: {}", e.getMessage());
                } catch (SQLException rollbackEx) {
                    logger.error("Error al ejecutar rollback", rollbackEx);
                }
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Fallo en la ejecución de la transacción", e);

        } finally {
            conexionActual.remove();
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.error("Error al cerrar conexión transaccional", closeEx);
                }
            }
        }
    }

    /**
     * Ejecuta una operación sin valor de retorno dentro de una transacción.
     *
     * @param accion Acción a ejecutar
     */
    public static void ejecutar(Runnable accion) {
        ejecutar(() -> {
            accion.run();
            return null;
        });
    }

    /**
     * Indica si hay una transacción activa en el hilo actual.
     *
     * @return true si hay transacción activa, false en caso contrario
     */
    public static boolean hayTransaccionActiva() {
        Connection conn = conexionActual.get();
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Inicia una transacción manual vinculada al hilo actual (utilizado principalmente para pruebas con rollback).
     *
     * @throws SQLException Si ocurre un error al abrir la conexión
     */
    public static void iniciarTransaccionManual() throws SQLException {
        if (conexionActual.get() != null) {
            return;
        }
        Connection conn = ConexionBD.obtenerConexion();
        conn.setAutoCommit(false);
        conexionActual.set(conn);
        logger.debug("Transacción manual iniciada para hilo {}", Thread.currentThread().getName());
    }

    /**
     * Revierte y cierra la transacción manual activa en el hilo actual.
     */
    public static void revertirTransaccionManual() {
        Connection conn = conexionActual.get();
        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
                logger.debug("Transacción manual revertida exitosamente");
            } catch (SQLException e) {
                logger.error("Error al revertir transacción manual", e);
            } finally {
                conexionActual.remove();
            }
        }
    }

    /**
     * Obtiene una conexión para realizar operaciones en los DAO.
     * Si hay una transacción activa en el hilo actual, retorna un envoltorio (proxy)
     * cuya invocación a close() no cerrará la conexión compartida.
     * Si no hay transacción activa, retorna una nueva conexión directa que debe ser cerrada.
     *
     * @return Connection JDBC
     * @throws SQLException Si falla la obtención de la conexión
     */
    public static Connection obtenerConexion() throws SQLException {
        Connection trans = conexionActual.get();
        if (trans != null && !trans.isClosed()) {
            return envolverParaTransaccion(trans);
        }
        return ConexionBD.obtenerConexion();
    }

    /**
     * Envuelve la conexión transaccional para evitar que bloques try-with-resources en los DAO la cierren.
     */
    private static Connection envolverParaTransaccion(Connection conexionReal) {
        return (Connection) Proxy.newProxyInstance(
                GestorTransaccion.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("close".equals(method.getName())) {
                            // Ignorar close(): la conexión se cerrará al finalizar el bloque transaccional
                            logger.trace("Llamada close() ignorada en conexión transaccional compartida");
                            return null;
                        }
                        if ("isClosed".equals(method.getName())) {
                            return conexionReal.isClosed();
                        }
                        return method.invoke(conexionReal, args);
                    }
                }
        );
    }
}

