package com.juniverse.quarkus.crud.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Registra cada petición y respuesta HTTP (método, URI, cabeceras, cuerpo y duración). Enmascara las cabeceras sensibles. */
@Provider
@Priority(1000)
public class HttpLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(HttpLoggingFilter.class);
    private static final String REQUEST_START_TIME = "request.start.time";
    private static final String REQUEST_BODY = "request.body";
    private static final String MASK = "***";
    /** Cabeceras cuyo valor nunca debe quedar en los logs. */
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Context
    UriInfo uriInfo;

    @Context
    HttpHeaders headers;

    /**
     * Registra la petición entrante y guarda la hora de inicio.
     * @param requestContext petición
     * @throws IOException si falla la lectura del cuerpo
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        long startTime = System.currentTimeMillis();
        requestContext.setProperty(REQUEST_START_TIME, startTime);

        if (LOG.isInfoEnabled()) {
            String method = requestContext.getMethod();
            String uri = uriInfo.getRequestUri().toString();
            String contentType = requestContext.getHeaderString("Content-Type");
            
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("\n╔═══════════════════════════════════════════════════════════════════════════════════════════════════════");
            requestLog.append("\n║ HTTP REQUEST");
            requestLog.append("\n╠═══════════════════════════════════════════════════════════════════════════════════════════════════════");
            requestLog.append("\n║ Method: ").append(method);
            requestLog.append("\n║ URI: ").append(uri);
            requestLog.append("\n║ Content-Type: ").append(contentType != null ? contentType : "N/A");
            
            // Registra encabezados de petición todos.
            requestLog.append("\n║");
            requestLog.append("\n║ REQUEST HEADERS:");
            MultivaluedMap<String, String> requestHeaders = requestContext.getHeaders();
            for (Map.Entry<String, List<String>> headerEntry : requestHeaders.entrySet()) {
                requestLog.append("\n║   ").append(headerEntry.getKey()).append(": ")
                        .append(isSensitive(headerEntry.getKey()) ? MASK : String.join(", ", headerEntry.getValue()));
            }

            if (requestContext.hasEntity() && isLoggableContentType(contentType)) {
                try {
                    InputStream inputStream = requestContext.getEntityStream();
                    String requestBody = readInputStream(inputStream);
                    
                    if (!requestBody.isEmpty()) {
                        requestLog.append("\n║");
                        requestLog.append("\n║ REQUEST BODY (JSON):");
                        String formattedJson = formatJson(requestBody);
                        String[] lines = formattedJson.split("\n");
                        for (String line : lines) {
                            requestLog.append("\n║   ").append(line);
                        }
                        requestContext.setProperty(REQUEST_BODY, requestBody);
                    }
                    
                    requestContext.setEntityStream(new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    requestLog.append("\n║ Error reading request body: ").append(e.getMessage());
                }
            }
            
            requestLog.append("\n╚═══════════════════════════════════════════════════════════════════════════════════════════════════════");
            LOG.info(requestLog.toString());
        }
    }

    /**
     * Registra la respuesta y el tiempo que tardó.
     * @param requestContext petición
     * @param responseContext respuesta
     * @throws IOException si falla la escritura
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (LOG.isInfoEnabled()) {
            Long startTime = (Long) requestContext.getProperty(REQUEST_START_TIME);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

            String method = requestContext.getMethod();
            String uri = uriInfo.getRequestUri().toString();
            int status = responseContext.getStatus();
            String contentType = responseContext.getHeaderString("Content-Type");

            StringBuilder responseLog = new StringBuilder();
            responseLog.append("\n╔═══════════════════════════════════════════════════════════════════════════════════════════════════════");
            responseLog.append("\n║ HTTP RESPONSE");
            responseLog.append("\n╠═══════════════════════════════════════════════════════════════════════════════════════════════════════");
            responseLog.append("\n║ Method: ").append(method);
            responseLog.append("\n║ URI: ").append(uri);
            responseLog.append("\n║ Status: ").append(status);
            responseLog.append("\n║ Content-Type: ").append(contentType != null ? contentType : "N/A");
            responseLog.append("\n║ Duration: ").append(duration).append("ms");
            
            // Registra encabezados de respuesta todos.
            responseLog.append("\n║");
            responseLog.append("\n║ RESPONSE HEADERS:");
            MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
            for (Map.Entry<String, List<Object>> headerEntry : responseHeaders.entrySet()) {
                List<String> values = headerEntry.getValue().stream()
                    .map(Object::toString)
                    .toList();
                responseLog.append("\n║   ").append(headerEntry.getKey()).append(": ")
                        .append(isSensitive(headerEntry.getKey()) ? MASK : String.join(", ", values));
            }

            if (responseContext.hasEntity() && isLoggableContentType(contentType)) {
                try {
                    Object entity = responseContext.getEntity();
                    if (entity != null) {
                        String responseBody = entity.toString();
                        responseLog.append("\n║");
                        responseLog.append("\n║ RESPONSE BODY (JSON):");
                        String formattedJson = formatJson(responseBody);
                        String[] lines = formattedJson.split("\n");
                        for (String line : lines) {
                            responseLog.append("\n║   ").append(line);
                        }
                    }
                } catch (Exception e) {
                    responseLog.append("\n║ Error reading response body: ").append(e.getMessage());
                }
            }

            responseLog.append("\n╚═══════════════════════════════════════════════════════════════════════════════════════════════════════");
            LOG.info(responseLog.toString());
        }
    }

    /**
     * Indica si el valor de la cabecera es sensible (credenciales, cookies, claves de API).
     *
     * @param name nombre de la cabecera
     * @return {@code true} si hay que enmascararla en el log
     */
    private static boolean isSensitive(String name) {
        return name != null && SENSITIVE_HEADERS.contains(name.toLowerCase());
    }

    /** Lee todo un flujo como texto UTF-8. */
    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        
        return result.toString(StandardCharsets.UTF_8);
    }

    /** Da formato legible al JSON (si no es JSON válido lo devuelve igual). */
    private String formatJson(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            // Si no es JSON válido, se devuelve tal cual
            return jsonString;
        }
    }

    /** Indica si el tipo de contenido es texto que se puede mostrar en el log. */
    private boolean isLoggableContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        String lowerContentType = contentType.toLowerCase();
        return lowerContentType.contains("application/json") ||
               lowerContentType.contains("application/xml") ||
               lowerContentType.contains("text/") ||
               lowerContentType.contains("application/x-www-form-urlencoded");
    }
}