import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { Resource } from '@opentelemetry/resources';
import { SemanticResourceAttributes } from '@opentelemetry/semantic-conventions';
import { BatchSpanProcessor } from '@opentelemetry/sdk-trace-web';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { DocumentLoadInstrumentation } from '@opentelemetry/instrumentation-document-load';
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch';
import { XMLHttpRequestInstrumentation } from '@opentelemetry/instrumentation-xml-http-request';
import { UserInteractionInstrumentation } from '@opentelemetry/instrumentation-user-interaction';

// OpenTelemetry configuration
const OTEL_COLLECTOR_URL = process.env.REACT_APP_OTEL_COLLECTOR_URL || 'http://localhost:4318';
const SERVICE_NAME = process.env.REACT_APP_SERVICE_NAME || 'frontend-app';
const SERVICE_VERSION = process.env.REACT_APP_SERVICE_VERSION || '1.0.0';
const ENVIRONMENT = process.env.NODE_ENV || 'development';

class OpenTelemetryService {
  private tracer: any;
  private isInitialized = false;

  constructor() {
    this.initializeOpenTelemetry();
  }

  private initializeOpenTelemetry() {
    try {
      // Create resource with service information
      const resource = new Resource({
        [SemanticResourceAttributes.SERVICE_NAME]: SERVICE_NAME,
        [SemanticResourceAttributes.SERVICE_VERSION]: SERVICE_VERSION,
        [SemanticResourceAttributes.DEPLOYMENT_ENVIRONMENT]: ENVIRONMENT,
        'platform': 'web',
        'framework': 'react',
        'data_governance': 'enabled',
        'tenant_isolation': 'enabled',
        'audit_trail': 'enabled'
      });

      // Create OTLP exporter
      const otlpExporter = new OTLPTraceExporter({
        url: `${OTEL_COLLECTOR_URL}/v1/traces`,
        headers: {
          'Content-Type': 'application/json',
        },
      });

      // Create tracer provider
      const provider = new WebTracerProvider({
        resource: resource,
      });

      // Add span processor
      provider.addSpanProcessor(new BatchSpanProcessor(otlpExporter));

      // Register the provider
      provider.register();

      // Get tracer
      this.tracer = provider.getTracer(SERVICE_NAME);

      // Register auto-instrumentations
      registerInstrumentations({
        instrumentations: [
          new DocumentLoadInstrumentation(),
          new FetchInstrumentation({
            ignoreUrls: [
              /localhost:3000\/sockjs-node/,
              /localhost:3000\/__webpack_dev_server__/,
              /localhost:3000\/hot-update.json/,
            ],
            propagateTraceHeaderCorsUrls: [
              /localhost:8081/,
              /localhost:8082/,
              /localhost:8083/,
            ],
          }),
          new XMLHttpRequestInstrumentation({
            ignoreUrls: [
              /localhost:3000\/sockjs-node/,
              /localhost:3000\/__webpack_dev_server__/,
              /localhost:3000\/hot-update.json/,
            ],
          }),
          new UserInteractionInstrumentation(),
        ],
      });

      this.isInitialized = true;
      console.log('OpenTelemetry initialized successfully');
    } catch (error) {
      console.error('Failed to initialize OpenTelemetry:', error);
      this.isInitialized = false;
    }
  }

  /**
   * Create a new span for business operations
   */
  createSpan(operationName: string, attributes: Record<string, string> = {}) {
    if (!this.isInitialized || !this.tracer) {
      return null;
    }

    try {
      const span = this.tracer.startSpan(operationName, {
        attributes: {
          'operation.type': 'business',
          'operation.id': this.generateOperationId(),
          'platform': 'web',
          'framework': 'react',
          ...attributes
        }
      });

      return span;
    } catch (error) {
      console.error('Failed to create span:', error);
      return null;
    }
  }

  /**
   * Create a span for API calls
   */
  createApiSpan(operationName: string, method: string, url: string, attributes: Record<string, string> = {}) {
    if (!this.isInitialized || !this.tracer) {
      return null;
    }

    try {
      const span = this.tracer.startSpan(operationName, {
        attributes: {
          'http.method': method,
          'http.url': url,
          'operation.type': 'api',
          'operation.id': this.generateOperationId(),
          'platform': 'web',
          'framework': 'react',
          ...attributes
        }
      });

      return span;
    } catch (error) {
      console.error('Failed to create API span:', error);
      return null;
    }
  }

  /**
   * Create a span for user interactions
   */
  createUserInteractionSpan(interactionType: string, elementId: string, attributes: Record<string, string> = {}) {
    if (!this.isInitialized || !this.tracer) {
      return null;
    }

    try {
      const span = this.tracer.startSpan(`user.interaction.${interactionType}`, {
        attributes: {
          'interaction.type': interactionType,
          'element.id': elementId,
          'operation.type': 'user_interaction',
          'operation.id': this.generateOperationId(),
          'platform': 'web',
          'framework': 'react',
          ...attributes
        }
      });

      return span;
    } catch (error) {
      console.error('Failed to create user interaction span:', error);
      return null;
    }
  }

  /**
   * Add business context to current span
   */
  addBusinessContext(tenantId: string, userId: string, operation: string, businessData: Record<string, any> = {}) {
    if (!this.isInitialized || !this.tracer) {
      return;
    }

    try {
      const currentSpan = this.tracer.getCurrentSpan();
      if (currentSpan) {
        currentSpan.setAttributes({
          'business.tenant.id': tenantId,
          'business.user.id': userId,
          'business.operation': operation,
          'business.timestamp': new Date().toISOString()
        });

        // Add business data as attributes
        Object.entries(businessData).forEach(([key, value]) => {
          if (value != null) {
            currentSpan.setAttribute(`business.data.${key}`, String(value));
          }
        });
      }
    } catch (error) {
      console.error('Failed to add business context:', error);
    }
  }

  /**
   * Add performance metrics to current span
   */
  addPerformanceMetrics(operation: string, durationMs: number, status: string) {
    if (!this.isInitialized || !this.tracer) {
      return;
    }

    try {
      const currentSpan = this.tracer.getCurrentSpan();
      if (currentSpan) {
        currentSpan.setAttributes({
          'performance.operation': operation,
          'performance.duration_ms': durationMs,
          'performance.status': status,
          'performance.timestamp': new Date().toISOString()
        });
      }
    } catch (error) {
      console.error('Failed to add performance metrics:', error);
    }
  }

  /**
   * Add error to current span
   */
  addError(error: Error) {
    if (!this.isInitialized || !this.tracer) {
      return;
    }

    try {
      const currentSpan = this.tracer.getCurrentSpan();
      if (currentSpan) {
        currentSpan.setStatus({ code: 2, message: error.message });
        currentSpan.recordException(error);
      }
    } catch (spanError) {
      console.error('Failed to add error to span:', spanError);
    }
  }

  /**
   * Execute a function within a span context
   */
  executeWithSpan<T>(
    operationName: string, 
    attributes: Record<string, string> = {}, 
    operation: () => T
  ): T {
    const span = this.createSpan(operationName, attributes);
    
    if (!span) {
      return operation();
    }

    try {
      const result = operation();
      span.setStatus({ code: 1 }); // OK
      return result;
    } catch (error) {
      span.setStatus({ code: 2, message: error instanceof Error ? error.message : 'Unknown error' });
      if (error instanceof Error) {
        span.recordException(error);
      }
      throw error;
    } finally {
      span.end();
    }
  }

  /**
   * Execute an async function within a span context
   */
  async executeAsyncWithSpan<T>(
    operationName: string, 
    attributes: Record<string, string> = {}, 
    operation: () => Promise<T>
  ): Promise<T> {
    const span = this.createSpan(operationName, attributes);
    
    if (!span) {
      return operation();
    }

    try {
      const result = await operation();
      span.setStatus({ code: 1 }); // OK
      return result;
    } catch (error) {
      span.setStatus({ code: 2, message: error instanceof Error ? error.message : 'Unknown error' });
      if (error instanceof Error) {
        span.recordException(error);
      }
      throw error;
    } finally {
      span.end();
    }
  }

  /**
   * Get current trace ID
   */
  getCurrentTraceId(): string {
    if (!this.isInitialized || !this.tracer) {
      return 'unknown';
    }

    try {
      const currentSpan = this.tracer.getCurrentSpan();
      if (currentSpan && currentSpan.spanContext) {
        return currentSpan.spanContext().traceId;
      }
      return 'unknown';
    } catch (error) {
      console.error('Failed to get current trace ID:', error);
      return 'unknown';
    }
  }

  /**
   * Get current span ID
   */
  getCurrentSpanId(): string {
    if (!this.isInitialized || !this.tracer) {
      return 'unknown';
    }

    try {
      const currentSpan = this.tracer.getCurrentSpan();
      if (currentSpan && currentSpan.spanContext) {
        return currentSpan.spanContext().spanId;
      }
      return 'unknown';
    } catch (error) {
      console.error('Failed to get current span ID:', error);
      return 'unknown';
    }
  }

  /**
   * Check if OpenTelemetry is initialized
   */
  isReady(): boolean {
    return this.isInitialized;
  }

  /**
   * Generate unique operation ID
   */
  private generateOperationId(): string {
    return `op-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}

// Create singleton instance
export const openTelemetryService = new OpenTelemetryService();

// Export for use in components
export default openTelemetryService;
