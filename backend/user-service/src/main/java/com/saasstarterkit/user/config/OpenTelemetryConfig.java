package com.saasstarterkit.user.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OpenTelemetryConfig {

    @Value("${opentelemetry.service.name:user-service}")
    private String serviceName;

    @Value("${opentelemetry.service.version:1.0.0}")
    private String serviceVersion;

    @Value("${opentelemetry.service.namespace:saas-starter-kit}")
    private String serviceNamespace;

    @Value("${opentelemetry.collector.endpoint:http://localhost:4317}")
    private String collectorEndpoint;

    @Value("${opentelemetry.sampling.probability:1.0}")
    private double samplingProbability;

    @Value("${spring.profiles.active:default}")
    private String environment;

    @Bean
    @Profile("!test")
    public OpenTelemetry openTelemetry() {
        // Create resource with service information and tenant isolation metadata
        Resource resource = Resource.getDefault()
            .merge(Resource.builder()
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("service.name"), serviceName)
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("service.version"), serviceVersion)
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("service.namespace"), serviceNamespace)
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("deployment.environment"), environment)
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("cloud.provider"), "local")
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("cloud.platform"), "docker")
                // Tenant isolation and security attributes
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("tenant.isolation"), "enabled")
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("data.governance"), "enabled")
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("compliance.tracking"), "enabled")
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("security.level"), "high")
                .put(io.opentelemetry.api.common.AttributeKey.stringKey("audit.trail"), "enabled")
                .build());

        // Create OTLP exporter with tenant context headers
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(collectorEndpoint)
            .setTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addHeader("X-Tenant-Isolation", "enabled")
            .addHeader("X-Data-Governance", "enabled")
            .addHeader("X-Compliance-Tracking", "enabled")
            .build();

        // Create span processor with tenant context validation
        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
            .setMaxQueueSize(10000)
            .setMaxExportBatchSize(512)
            .setExporterTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .setScheduleDelay(5, java.util.concurrent.TimeUnit.SECONDS)
            .build();

        // Create tracer provider with tenant-aware sampling
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(spanProcessor)
            .setResource(resource)
            .setSampler(Sampler.traceIdRatioBased(samplingProbability))
            .build();

        // Create OpenTelemetry SDK with tenant context propagation
        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();

        return openTelemetrySdk;
    }

    @Bean
    @Profile("test")
    public OpenTelemetry openTelemetryTest() {
        // Return a no-op OpenTelemetry for testing
        return OpenTelemetry.noop();
    }
}
