package com.saasstarterkit.user.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class TracingService {

    private static final Logger logger = LoggerFactory.getLogger(TracingService.class);
    
    private final Tracer tracer;
    private final TextMapPropagator propagator;
    
    @Autowired
    public TracingService(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("com.saasstarterkit.user");
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
    }
    
    /**
     * Create a new span for a business operation
     */
    public Span createSpan(String operationName, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operationName)
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("operation.type", "business")
            .setAttribute("operation.id", UUID.randomUUID().toString())
            .startSpan();
        
        // Add custom attributes
        if (attributes != null) {
            attributes.forEach((key, value) -> span.setAttribute(key, value));
        }
        
        return span;
    }
    
    /**
     * Create a span for HTTP operations
     */
    public Span createHttpSpan(String operationName, String method, String url, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operationName)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute("http.method", method)
            .setAttribute("http.url", url)
            .setAttribute("operation.type", "http")
            .setAttribute("operation.id", UUID.randomUUID().toString())
            .startSpan();
        
        // Add custom attributes
        if (attributes != null) {
            attributes.forEach((key, value) -> span.setAttribute(key, value));
        }
        
        return span;
    }
    
    /**
     * Create a span for database operations
     */
    public Span createDatabaseSpan(String operationName, String database, String table, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operationName)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute("db.system", database)
            .setAttribute("db.name", table)
            .setAttribute("operation.type", "database")
            .setAttribute("operation.id", UUID.randomUUID().toString())
            .startSpan();
        
        // Add custom attributes
        if (attributes != null) {
            attributes.forEach((key, value) -> span.setAttribute(key, value));
        }
        
        return span;
    }
    
    /**
     * Execute a function within a span context
     */
    public <T> T executeWithSpan(String operationName, Map<String, String> attributes, Supplier<T> operation) {
        Span span = createSpan(operationName, attributes);
        
        try (var scope = span.makeCurrent()) {
            T result = operation.get();
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Execute an async function within a span context
     */
    public <T> CompletableFuture<T> executeAsyncWithSpan(String operationName, Map<String, String> attributes, Supplier<CompletableFuture<T>> operation) {
        Span span = createSpan(operationName, attributes);
        
        return operation.get()
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, throwable.getMessage());
                    span.recordException(throwable);
                } else {
                    span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
                }
                span.end();
            });
    }
    
    /**
     * Add event to current span
     */
    public void addEvent(String eventName, Map<String, String> attributes) {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.isRecording()) {
            // Add attributes one by one to avoid complex type inference issues
            if (attributes != null) {
                attributes.forEach((key, value) -> {
                    if (value != null) {
                        currentSpan.setAttribute(key, value);
                    }
                });
            }
            currentSpan.addEvent(eventName);
        }
    }
    
    /**
     * Add error to current span
     */
    public void addError(Throwable error) {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.isRecording()) {
            currentSpan.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, error.getMessage());
            currentSpan.recordException(error);
        }
    }
    
    /**
     * Get current trace ID
     */
    public String getCurrentTraceId() {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
            return currentSpan.getSpanContext().getTraceId();
        }
        return "unknown";
    }
    
    /**
     * Get current span ID
     */
    public String getCurrentSpanId() {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.getSpanContext().isValid()) {
            return currentSpan.getSpanContext().getSpanId();
        }
        return "unknown";
    }
    
    /**
     * Inject trace context into headers
     */
    public void injectTraceContext(Map<String, String> headers) {
        Context context = Context.current();
        propagator.inject(context, headers, (carrier, key, value) -> carrier.put(key, value));
    }
    
    /**
     * Extract trace context from headers
     */
    public Context extractTraceContext(Map<String, String> headers) {
        // Simplified implementation - just return current context
        // In a real implementation, you would extract trace context from headers
        return Context.current();
    }
    
    /**
     * Create a child span for nested operations
     */
    public Span createChildSpan(String operationName, Map<String, String> attributes) {
        Span parentSpan = Span.current();
        Span span = tracer.spanBuilder(operationName)
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("operation.type", "child")
            .setAttribute("operation.id", UUID.randomUUID().toString())
            .setAttribute("parent.span.id", parentSpan.getSpanContext().getSpanId())
            .setAttribute("parent.trace.id", parentSpan.getSpanContext().getTraceId())
            .startSpan();
        
        // Add custom attributes
        if (attributes != null) {
            attributes.forEach((key, value) -> span.setAttribute(key, value));
        }
        
        return span;
    }
    
    /**
     * Add business context to current span
     */
    public void addBusinessContext(String tenantId, String userId, String operation, Map<String, Object> businessData) {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.isRecording()) {
            currentSpan.setAttribute("business.tenant.id", tenantId);
            currentSpan.setAttribute("business.user.id", userId);
            currentSpan.setAttribute("business.operation", operation);
            
            if (businessData != null) {
                businessData.forEach((key, value) -> {
                    if (value != null) {
                        currentSpan.setAttribute("business.data." + key, value.toString());
                    }
                });
            }
        }
    }
    
    /**
     * Add performance metrics to current span
     */
    public void addPerformanceMetrics(String operation, long durationMs, String status) {
        Span currentSpan = Span.current();
        if (currentSpan != null && currentSpan.isRecording()) {
            currentSpan.setAttribute("performance.operation", operation);
            currentSpan.setAttribute("performance.duration_ms", durationMs);
            currentSpan.setAttribute("performance.status", status);
            currentSpan.setAttribute("performance.timestamp", System.currentTimeMillis());
        }
    }
    
    /**
     * Create a span for external service calls
     */
    public Span createExternalServiceSpan(String serviceName, String operation, String endpoint, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operation)
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute("service.name", serviceName)
            .setAttribute("service.endpoint", endpoint)
            .setAttribute("operation.type", "external")
            .setAttribute("operation.id", UUID.randomUUID().toString())
            .startSpan();
        
        // Add custom attributes
        if (attributes != null) {
            attributes.forEach((key, value) -> span.setAttribute(key, value));
        }
        
        return span;
    }
}
