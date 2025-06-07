package ru.fox.orion.config.http_logger;

import brave.Tracing;
import brave.baggage.BaggageFields;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.SpanHandler;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {

    @Bean
    public Tracer tracing() {
        brave.propagation.CurrentTraceContext braveCurrentTraceContext = ThreadLocalCurrentTraceContext.newBuilder()
                .addScopeDecorator(customPropagationKeys())
                .build();

        io.micrometer.tracing.CurrentTraceContext bridgeContext = new BraveCurrentTraceContext(braveCurrentTraceContext);
        Tracing tracing = Tracing.newBuilder()
                .currentTraceContext(braveCurrentTraceContext)
                .supportsJoin(false)
                .traceId128Bit(true)
                .sampler(Sampler.NEVER_SAMPLE)
                .addSpanHandler(SpanHandler.NOOP)
                .build();

        brave.Tracer braveTracer = tracing.tracer();
        return new BraveTracer(braveTracer, bridgeContext);
    }

    private CurrentTraceContext.ScopeDecorator customPropagationKeys() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(BaggageFields.TRACE_ID)
                        .name("trace.id").flushOnUpdate().build())
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(BaggageFields.SPAN_ID)
                        .flushOnUpdate()
                        .name("span.id").build())
                .build();
    }
}
