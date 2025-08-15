package com.saasstarterkit.user.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BravePropagator;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingSenderTracingObservationHandler;
import io.micrometer.tracing.handler.TracingObservationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public TracingObservationHandler tracingObservationHandler(Tracer tracer) {
        return new DefaultTracingObservationHandler(tracer);
    }

    @Bean
    public PropagatingSenderTracingObservationHandler propagatingSenderTracingObservationHandler(
            Tracer tracer, BravePropagator propagator) {
        return new PropagatingSenderTracingObservationHandler(tracer, propagator);
    }

    @Bean
    public PropagatingReceiverTracingObservationHandler propagatingReceiverTracingObservationHandler(
            Tracer tracer, BravePropagator propagator) {
        return new PropagatingReceiverTracingObservationHandler(tracer, propagator);
    }
}
