package com.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class Function {

    /**
     * POST /api/Roles GET /api/Roles
     */
    @FunctionName("Roles")
    public HttpResponseMessage run(@HttpTrigger(name = "req",
            methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Function Roles ejecutada. Método: " + request.getHttpMethod());

        // GET - Consultar todos los roles
        if (request.getHttpMethod() == HttpMethod.GET) {
            return obtenerRoles(request, context);
        }

        // POST - Crear rol
        if (request.getHttpMethod() == HttpMethod.POST) {
            return crearRol(request, context);
        }

        return request.createResponseBuilder(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Método HTTP no permitido.").build();
    }

    /**
     * GET /api/Roles/{id} Obtiene un rol por su ID.
     */
    @FunctionName("RolPorId")
    public HttpResponseMessage obtenerPorId(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "Roles/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id, final ExecutionContext context) {

        context.getLogger().info("Function RolPorId ejecutada.");

        if (id == null || id.isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el ID del rol.").build();
        }

        long idRol;

        try {

            idRol = Long.parseLong(id);

            if (idRol <= 0) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El ID debe ser mayor que cero.").build();
            }

        } catch (NumberFormatException e) {

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El ID debe ser numérico.").build();
        }

        return obtenerRolPorId(request, context, idRol);
    }

    /**
     * PUT /api/Roles/{id} Actualiza un rol existente.
     */
    @FunctionName("ActualizarRol")
    public HttpResponseMessage actualizarRol(
            @HttpTrigger(name = "req", methods = {HttpMethod.PUT},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "Roles/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id, final ExecutionContext context) {

        context.getLogger().info("Function ActualizarRol ejecutada.");

        if (id == null || id.isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el ID del rol.").build();
        }

        long idRol;

        try {

            idRol = Long.parseLong(id);

            if (idRol <= 0) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El ID debe ser mayor que cero.").build();
            }

        } catch (NumberFormatException e) {

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El ID debe ser numérico.").build();
        }

        Optional<String> body = request.getBody();

        if (body.isEmpty() || body.get().isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Debe enviar los datos del rol.").build();
        }

        String json = body.get();

        try {

            String nombreRol = obtenerValor(json, "nombreRol");

            String estado = obtenerValor(json, "estado");

            if (nombreRol == null || nombreRol.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El campo nombreRol es obligatorio.").build();
            }

            if (estado == null || estado.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El campo estado es obligatorio.").build();
            }

            String sql = """
                    UPDATE ROLES
                    SET
                        NOMBRE_ROL = ?,
                        ESTADO = ?
                    WHERE ID_ROL = ?
                    """;

            try (Connection connection = OracleConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {

                connection.setAutoCommit(false);

                statement.setString(1, nombreRol);
                statement.setString(2, estado);
                statement.setLong(3, idRol);

                int filas = statement.executeUpdate();

                if (filas == 0) {

                    connection.rollback();

                    context.getLogger().info("Rol no encontrado. ID: " + idRol);

                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("No existe un rol con ID " + idRol).build();
                }

                connection.commit();

                String response = """
                        {
                            "mensaje": "Rol actualizado correctamente",
                            "idRol": %d,
                            "nombreRol": "%s",
                            "estado": "%s"
                        }
                        """.formatted(idRol, nombreRol, estado);

                context.getLogger().info("Rol actualizado correctamente. ID: " + idRol);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json").body(response).build();
            }

        } catch (Exception e) {

            context.getLogger().severe("Error actualizando rol: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No fue posible actualizar el rol.").build();
        }
    }

    /**
     * DELETE /api/Roles/{id} Elimina un rol existente.
     */
    @FunctionName("EliminarRol")
    public HttpResponseMessage eliminarRol(
            @HttpTrigger(name = "req", methods = {HttpMethod.DELETE},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "Roles/{id}") HttpRequestMessage<Optional<String>> request,
            @BindingName("id") String id, final ExecutionContext context) {

        context.getLogger().info("Function EliminarRol ejecutada.");

        if (id == null || id.isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Debe indicar el ID del rol.").build();
        }

        long idRol;

        try {

            idRol = Long.parseLong(id);

            if (idRol <= 0) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El ID debe ser mayor que cero.").build();
            }

        } catch (NumberFormatException e) {

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El ID debe ser numérico.").build();
        }

        String sql = """
                DELETE FROM ROLES
                WHERE ID_ROL = ?
                """;

        try (Connection connection = OracleConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            statement.setLong(1, idRol);

            int filas = statement.executeUpdate();

            if (filas == 0) {

                connection.rollback();

                context.getLogger().info("Rol no encontrado. ID: " + idRol);

                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                        .body("No existe un rol con ID " + idRol).build();
            }

            connection.commit();

            context.getLogger().info("Rol eliminado correctamente. ID: " + idRol);

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Rol eliminado correctamente. ID: " + idRol).build();

        } catch (Exception e) {

            context.getLogger().severe("Error eliminando rol: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No fue posible eliminar el rol.").build();
        }
    }

    /**
     * POST /api/Roles Crea un nuevo rol.
     */
    private HttpResponseMessage crearRol(HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        Optional<String> body = request.getBody();

        if (body.isEmpty() || body.get().isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Debe enviar los datos del rol.").build();
        }

        String json = body.get();

        try {

            String nombreRol = obtenerValor(json, "nombreRol");

            String estado = obtenerValor(json, "estado");

            if (nombreRol == null || nombreRol.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El campo nombreRol es obligatorio.").build();
            }

            if (estado == null || estado.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("El campo estado es obligatorio.").build();
            }

            String sql = """
                    INSERT INTO ROLES
                        (NOMBRE_ROL, ESTADO)
                    VALUES
                        (?, ?)
                    """;

            try (Connection connection = OracleConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {

                connection.setAutoCommit(false);

                statement.setString(1, nombreRol);
                statement.setString(2, estado);

                int filas = statement.executeUpdate();

                if (filas == 0) {

                    connection.rollback();

                    return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("No fue posible crear el rol.").build();
                }

                connection.commit();

                String response = """
                        {
                            "mensaje": "Rol creado correctamente",
                            "nombreRol": "%s",
                            "estado": "%s"
                        }
                        """.formatted(nombreRol, estado);

                context.getLogger().info("Rol creado correctamente.");

                return request.createResponseBuilder(HttpStatus.CREATED)
                        .header("Content-Type", "application/json").body(response).build();
            }

        } catch (Exception e) {

            context.getLogger().severe("Error creando rol: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No fue posible crear el rol.").build();
        }
    }

    /**
     * GET /api/Roles Obtiene todos los roles.
     */
    private HttpResponseMessage obtenerRoles(HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        String sql = """
                SELECT
                    ID_ROL,
                    NOMBRE_ROL,
                    ESTADO
                FROM ROLES
                ORDER BY ID_ROL
                """;

        StringBuilder response = new StringBuilder();
        response.append("[");

        try (Connection connection = OracleConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {

            boolean primero = true;

            while (resultSet.next()) {

                if (!primero) {
                    response.append(",");
                }

                response.append("""
                        {
                            "idRol": %d,
                            "nombreRol": "%s",
                            "estado": "%s"
                        }
                        """.formatted(resultSet.getLong("ID_ROL"),
                        resultSet.getString("NOMBRE_ROL"), resultSet.getString("ESTADO")));

                primero = false;
            }

            response.append("]");

            context.getLogger().info("Roles consultados correctamente.");

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json").body(response.toString()).build();

        } catch (Exception e) {

            context.getLogger().severe("Error consultando roles: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No fue posible consultar los roles.").build();
        }
    }

    /**
     * Consulta un rol por su ID.
     */
    private HttpResponseMessage obtenerRolPorId(HttpRequestMessage<Optional<String>> request,
            ExecutionContext context, long idRol) {

        String sql = """
                SELECT
                    ID_ROL,
                    NOMBRE_ROL,
                    ESTADO
                FROM ROLES
                WHERE ID_ROL = ?
                """;

        try (Connection connection = OracleConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idRol);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {

                    context.getLogger().info("Rol no encontrado. ID: " + idRol);

                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("No existe un rol con ID " + idRol).build();
                }

                String response = """
                        {
                            "idRol": %d,
                            "nombreRol": "%s",
                            "estado": "%s"
                        }
                        """.formatted(resultSet.getLong("ID_ROL"),
                        resultSet.getString("NOMBRE_ROL"), resultSet.getString("ESTADO"));

                context.getLogger().info("Rol encontrado. ID: " + idRol);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json").body(response).build();
            }

        } catch (Exception e) {

            context.getLogger().severe("Error consultando rol: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No fue posible consultar el rol.").build();
        }
    }

    /**
     * Extrae un valor sencillo desde el JSON recibido.
     */
    private String obtenerValor(String json, String campo) {

        String busqueda = "\"" + campo + "\"";

        int posicionCampo = json.indexOf(busqueda);

        if (posicionCampo == -1) {
            return null;
        }

        int inicio = json.indexOf(":", posicionCampo);

        if (inicio == -1) {
            return null;
        }

        inicio++;

        while (inicio < json.length() && Character.isWhitespace(json.charAt(inicio))) {
            inicio++;
        }

        if (inicio >= json.length() || json.charAt(inicio) != '"') {
            return null;
        }

        inicio++;

        int fin = json.indexOf("\"", inicio);

        if (fin == -1) {
            return null;
        }

        return json.substring(inicio, fin);
    }
}
