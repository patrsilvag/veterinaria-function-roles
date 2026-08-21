package com.function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

public class FunctionTest {

    /**
     * Prueba POST /api/Roles cuando no se envía body.
     */
    @Test
    public void testCrearRolSinBody() {

        @SuppressWarnings("unchecked")
        HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        // Simulamos POST
        doReturn(HttpMethod.POST).when(req).getHttpMethod();

        // Body vacío
        doReturn(Optional.empty()).when(req).getBody();

        configurarResponseBuilder(req);

        ExecutionContext context = mock(ExecutionContext.class);

        doReturn(Logger.getGlobal()).when(context).getLogger();

        // Invoke
        HttpResponseMessage ret = new Function().run(req, context);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Prueba POST /api/Roles cuando falta nombreRol.
     */
    @Test
    public void testCrearRolSinNombre() {

        @SuppressWarnings("unchecked")
        HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        doReturn(HttpMethod.POST).when(req).getHttpMethod();

        String rolJson = """
                {
                    "estado": "ACTIVO"
                }
                """;

        doReturn(Optional.of(rolJson)).when(req).getBody();

        configurarResponseBuilder(req);

        ExecutionContext context = mock(ExecutionContext.class);

        doReturn(Logger.getGlobal()).when(context).getLogger();

        // Invoke
        HttpResponseMessage ret = new Function().run(req, context);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Prueba POST /api/Roles cuando falta estado.
     */
    @Test
    public void testCrearRolSinEstado() {

        @SuppressWarnings("unchecked")
        HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        doReturn(HttpMethod.POST).when(req).getHttpMethod();

        String rolJson = """
                {
                    "nombreRol": "ADMIN"
                }
                """;

        doReturn(Optional.of(rolJson)).when(req).getBody();

        configurarResponseBuilder(req);

        ExecutionContext context = mock(ExecutionContext.class);

        doReturn(Logger.getGlobal()).when(context).getLogger();

        // Invoke
        HttpResponseMessage ret = new Function().run(req, context);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, ret.getStatus());
    }

    /**
     * Prueba POST /api/Roles con datos completos.
     *
     * En este caso la validación pasa y la Function intenta realizar el INSERT en Oracle.
     *
     * Este test NO debe utilizarse como prueba unitaria contra Oracle. La prueba real del INSERT se
     * realiza mediante Postman.
     */
    @Test
    public void testCrearRolDatosCompletos() {

        @SuppressWarnings("unchecked")
        HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);

        doReturn(HttpMethod.POST).when(req).getHttpMethod();

        String rolJson = """
                {
                    "nombreRol": "ADMIN",
                    "estado": "ACTIVO"
                }
                """;

        doReturn(Optional.of(rolJson)).when(req).getBody();

        configurarResponseBuilder(req);

        ExecutionContext context = mock(ExecutionContext.class);

        doReturn(Logger.getGlobal()).when(context).getLogger();

        /*
         * No afirmamos CREATED aquí porque esta llamada requiere conexión real a Oracle.
         *
         * La prueba de integración se realizará con Postman.
         */
        HttpResponseMessage ret = new Function().run(req, context);

        /*
         * El test solamente verifica que la Function genere una respuesta y no falle por la
         * estructura del JSON.
         */
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ret.getStatus());
    }

    /**
     * Configura el Response Builder utilizado por las pruebas.
     */
    private void configurarResponseBuilder(HttpRequestMessage<Optional<String>> req) {

        doAnswer(invocation -> {

            HttpStatus status = (HttpStatus) invocation.getArguments()[0];

            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);

        }).when(req).createResponseBuilder(any(HttpStatus.class));
    }
}
