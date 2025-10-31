# 19. Monitoring and Observability

## 19.1. Observability Pillars

```
┌────────────────────────────────────────┐
│          Observability Stack            │
├────────────────────────────────────────┤
│                                         │
│  Logs        Metrics        Traces     │
│    ↓            ↓              ↓        │
│  Events      KPIs          Spans       │
│    ↓            ↓              ↓        │
│         Application Insights           │
│                  ↓                      │
│            Log Analytics                │
│                  ↓                      │
│          Azure Monitor Alerts           │
│                                         │
└────────────────────────────────────────┘
```

---

## 19.2. Azure Application Insights Integration

### **19.2.1. Backend Configuration**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
    <version>3.4.18</version>
</dependency>
```

```yaml
# application-prod.yml
azure:
  application-insights:
    instrumentation-key: ${APPINSIGHTS_INSTRUMENTATIONKEY}
    enabled: true
    web:
      enable-W3C: true
    quick-pulse:
      enabled: true
    sampling:
      percentage: 100  # 100% em produção inicial, reduzir se volume alto
```

### **19.2.2. Custom Telemetry**

```java
@Service
@Slf4j
public class TelemetryService {
    private final TelemetryClient telemetryClient;

    // Track custom events
    public void trackEvent(String eventName, Map<String, String> properties, Map<String, Double> metrics) {
        var event = new EventTelemetry(eventName);
        event.getProperties().putAll(properties);
        event.getMetrics().putAll(metrics);
        telemetryClient.trackEvent(event);
    }

    // Track business metrics
    public void trackVendaFinalizada(Venda venda) {
        var properties = Map.of(
            "vendaId", venda.getId().value().toString(),
            "tipo", venda.getTipo().toString(),
            "formaPagamento", venda.getFormaPagamento().toString(),
            "tenantId", TenantContext.getTenant().toString()
        );

        var metrics = Map.of(
            "totalCentavos", (double) venda.getTotal().valor(),
            "quantidadeItens", (double) venda.getItens().size()
        );

        trackEvent("VendaFinalizada", properties, metrics);
    }

    // Track dependencies (external APIs)
    public void trackDependency(
        String dependencyName,
        String commandName,
        Duration duration,
        boolean success
    ) {
        var dependency = new RemoteDependencyTelemetry(
            dependencyName,
            commandName,
            duration,
            success
        );
        telemetryClient.trackDependency(dependency);
    }
}
```

### **19.2.3. Automatic Instrumentation**

```java
// Spring Boot auto-configura automaticamente:
// - HTTP requests (latência, status codes)
// - Database queries (via JDBC)
// - External HTTP calls (RestTemplate, WebClient)
// - Exceptions não tratadas
// - JVM metrics (memory, GC, threads)
```

---

## 19.3. Logging Strategy

### **19.3.1. Structured Logging**

```java
@Slf4j
@Service
public class VendaService {
    public void finalizarVenda(VendaId vendaId, FormaPagamento formaPagamento) {
        log.info("Finalizando venda: vendaId={}, formaPagamento={}",
            vendaId, formaPagamento);

        var venda = vendaRepository.findById(vendaId)
            .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

        venda.finalizar(formaPagamento);
        vendaRepository.save(venda);

        log.info("Venda finalizada com sucesso: vendaId={}, total={}, itens={}",
            vendaId,
            venda.getTotal(),
            venda.getItens().size()
        );

        telemetryService.trackVendaFinalizada(venda);
    }
}
```

### **19.3.2. Log Levels by Environment**

```yaml
# application-dev.yml
logging:
  level:
    root: INFO
    com.estoquecentral: DEBUG
    org.springframework.web: DEBUG
    org.springframework.jdbc: DEBUG

# application-prod.yml
logging:
  level:
    root: WARN
    com.estoquecentral: INFO
    org.springframework.web: WARN
```

### **19.3.3. Log Aggregation Queries (KQL)**

```kusto
// Top 10 endpoints mais lentos
requests
| where timestamp > ago(1h)
| summarize
    p50 = percentile(duration, 50),
    p95 = percentile(duration, 95),
    p99 = percentile(duration, 99),
    count = count()
  by operation_Name
| where p95 > 200  // > 200ms
| top 10 by p95 desc

// Erros por tenant
exceptions
| where timestamp > ago(24h)
| extend tenantId = tostring(customDimensions.tenantId)
| summarize errorCount = count() by tenantId, type
| order by errorCount desc

// Taxa de sucesso NFCe por hora
customEvents
| where name == "NFCeEmitida" or name == "NFCeFalha"
| where timestamp > ago(24h)
| summarize
    total = count(),
    sucessos = countif(name == "NFCeEmitida"),
    falhas = countif(name == "NFCeFalha")
  by bin(timestamp, 1h)
| extend taxaSucesso = todouble(sucessos) / todouble(total) * 100
| project timestamp, taxaSucesso, total, sucessos, falhas
```

---

## 19.4. Key Performance Indicators (KPIs)

### **19.4.1. Application Metrics**

| Métrica | Target | Alerta | Query KQL |
|---------|--------|--------|-----------|
| **Response Time (p95)** | < 200ms | > 500ms | `requests \| summarize p95=percentile(duration, 95)` |
| **Error Rate** | < 0.1% | > 1% | `requests \| summarize errorRate=countif(success==false)*100.0/count()` |
| **Availability** | > 99.5% | < 99% | `availabilityResults \| summarize avg(success)` |
| **Database Latency (p95)** | < 50ms | > 100ms | `dependencies \| where type=="SQL" \| summarize p95=percentile(duration, 95)` |

### **19.4.2. Business Metrics**

```java
@Service
public class MetricsCollector {
    private final MeterRegistry meterRegistry;

    public void recordVenda(Venda venda) {
        // Counter: Total de vendas
        meterRegistry.counter("vendas.total",
            "tipo", venda.getTipo().toString(),
            "tenant", TenantContext.getTenant().toString()
        ).increment();

        // Gauge: Valor da venda
        meterRegistry.gauge("vendas.valor",
            Tags.of("tipo", venda.getTipo().toString()),
            venda.getTotal().valor()
        );

        // Timer: Tempo de processamento
        Timer.builder("vendas.processamento")
            .tag("tipo", venda.getTipo().toString())
            .register(meterRegistry);
    }

    public void recordEstoque(ProdutoId produtoId, int quantidade) {
        meterRegistry.gauge("estoque.quantidade",
            Tags.of("produtoId", produtoId.value().toString()),
            quantidade
        );
    }
}
```

### **19.4.3. Dashboard KPIs**

```kusto
// Dashboard: Vendas em tempo real (últimas 24h)
customEvents
| where name == "VendaFinalizada"
| where timestamp > ago(24h)
| extend
    totalCentavos = todouble(customMeasurements.totalCentavos),
    tipo = tostring(customDimensions.tipo)
| summarize
    totalVendas = count(),
    valorTotal = sum(totalCentavos) / 100,  // Converter para reais
    ticketMedio = avg(totalCentavos) / 100
  by bin(timestamp, 1h), tipo
| render timechart

// Dashboard: NFCe - Taxa de sucesso
customEvents
| where name in ("NFCeAutorizada", "NFCeFalha", "NFCeFalhaPermanente")
| where timestamp > ago(24h)
| summarize
    total = count(),
    autorizadas = countif(name == "NFCeAutorizada"),
    falhas = countif(name == "NFCeFalha"),
    falhasPermanentes = countif(name == "NFCeFalhaPermanente")
| extend
    taxaSucesso = (todouble(autorizadas) / todouble(total)) * 100,
    taxaFalhaPermanente = (todouble(falhasPermanentes) / todouble(total)) * 100
| project taxaSucesso, taxaFalhaPermanente, total, autorizadas, falhas, falhasPermanentes
```

---

## 19.5. Distributed Tracing

### **19.5.1. W3C Trace Context**

```java
// Automaticamente habilitado via Application Insights
// Headers propagados:
// - traceparent: 00-trace-id-span-id-01
// - tracestate: key1=value1,key2=value2

@Service
public class VendaService {
    private final EstoqueService estoqueService;
    private final FiscalService fiscalService;

    @Transactional
    public void finalizarVenda(VendaId vendaId) {
        // Trace ID propagado automaticamente para:
        // 1. Chamada ao EstoqueService
        // 2. Chamada ao FiscalService
        // 3. Queries ao banco de dados
        // 4. Chamadas HTTP externas

        estoqueService.baixar(vendaId);  // Span 1
        fiscalService.emitirNFCe(vendaId); // Span 2
    }
}
```

### **19.5.2. Custom Spans**

```java
@Service
public class ProdutoService {
    private final TelemetryClient telemetryClient;

    public void sincronizarComML(ProdutoId produtoId) {
        var operation = telemetryClient.startOperation("SyncML", "sync-" + produtoId);

        try {
            // Custom span 1: Buscar produto
            var spanBuscar = telemetryClient.getContext().getOperation().getId();
            telemetryClient.trackEvent("ML_BuscarProduto", Map.of("produtoId", produtoId.toString()));

            var produto = produtoRepository.findById(produtoId).orElseThrow();

            // Custom span 2: Enviar para ML
            telemetryClient.trackEvent("ML_EnviarProduto", Map.of("produtoId", produtoId.toString()));
            mlClient.syncProduct(produto);

            operation.setSuccess(true);

        } catch (Exception e) {
            operation.setSuccess(false);
            telemetryClient.trackException(e);
            throw e;

        } finally {
            telemetryClient.stopOperation(operation);
        }
    }
}
```

---

## 19.6. Health Checks

### **19.6.1. Spring Boot Actuator**

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

### **19.6.2. Custom Health Indicators**

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    @Override
    public Health health() {
        try (var conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
        return Health.down().build();
    }
}

@Component
public class RedisHealthIndicator implements HealthIndicator {
    private final RedisConnectionFactory connectionFactory;

    @Override
    public Health health() {
        try {
            var connection = connectionFactory.getConnection();
            connection.ping();
            return Health.up()
                .withDetail("redis", "Connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withException(e)
                .build();
        }
    }
}

@Component
public class MercadoLivreHealthIndicator implements HealthIndicator {
    private final MercadoLivreClient mlClient;

    @Override
    public Health health() {
        try {
            mlClient.healthCheck();
            return Health.up()
                .withDetail("mercadolivre", "API accessible")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("mercadolivre", "API unavailable")
                .withException(e)
                .build();
        }
    }
}
```

### **19.6.3. Health Check Response**

```json
// GET /actuator/health
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "redis": "Connected"
      }
    },
    "mercadolivre": {
      "status": "UP",
      "details": {
        "mercadolivre": "API accessible"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 536870912000,
        "free": 429496729600,
        "threshold": 10485760
      }
    }
  }
}
```

---

## 19.7. Alerting

### **19.7.1. Alert Rules**

```yaml
# Azure Monitor Alert Rules (via Bicep)
resource highErrorRateAlert 'Microsoft.Insights/metricAlerts@2018-03-01' = {
  name: 'high-error-rate'
  location: 'global'
  properties: {
    description: 'Alert quando taxa de erro > 1%'
    severity: 2
    enabled: true
    scopes: [
      containerApp.id
    ]
    evaluationFrequency: 'PT1M'
    windowSize: 'PT5M'
    criteria: {
      allOf: [
        {
          threshold: 1
          name: 'ErrorRate'
          metricNamespace: 'Microsoft.App/containerApps'
          metricName: 'Requests'
          operator: 'GreaterThan'
          timeAggregation: 'Average'
          criterionType: 'StaticThresholdCriterion'
        }
      ]
    }
    actions: [
      {
        actionGroupId: actionGroup.id
      }
    ]
  }
}
```

### **19.7.2. Alert Categories**

| Categoria | Severity | Notification | Example |
|-----------|----------|--------------|---------|
| **Critical** | 0 | SMS + Email + PagerDuty | Serviço down, database inacessível |
| **Error** | 1 | Email + Slack | Error rate > 5%, latência p95 > 1s |
| **Warning** | 2 | Slack | Error rate > 1%, latência p95 > 500ms |
| **Info** | 3 | Slack (low priority) | Deployment concluído, backup executado |

### **19.7.3. KQL Alert Queries**

```kusto
// Alerta: Taxa de erro alta
requests
| where timestamp > ago(5m)
| summarize
    total = count(),
    erros = countif(success == false)
| extend errorRate = (todouble(erros) / todouble(total)) * 100
| where errorRate > 1.0

// Alerta: Latência alta
requests
| where timestamp > ago(5m)
| summarize p95 = percentile(duration, 95)
| where p95 > 500

// Alerta: NFCe - Taxa de falha permanente alta
customEvents
| where name == "NFCeFalhaPermanente"
| where timestamp > ago(1h)
| summarize count = count()
| where count > 10

// Alerta: Estoque crítico
customEvents
| where name == "EstoqueBaixo"
| where timestamp > ago(5m)
| extend quantidade = toint(customDimensions.quantidade)
| where quantidade < 5
| summarize produtosEmEstoqueBaixo = dcount(tostring(customDimensions.produtoId))
| where produtosEmEstoqueBaixo > 20
```

---

## 19.8. Monitoring Dashboards

### **19.8.1. Executive Dashboard**

```kusto
// Card: Total de vendas hoje
customEvents
| where name == "VendaFinalizada"
| where timestamp > startofday(now())
| summarize count()

// Card: Valor total vendido hoje
customEvents
| where name == "VendaFinalizada"
| where timestamp > startofday(now())
| extend valorCentavos = todouble(customMeasurements.totalCentavos)
| summarize sum(valorCentavos) / 100

// Chart: Vendas por hora (últimas 24h)
customEvents
| where name == "VendaFinalizada"
| where timestamp > ago(24h)
| summarize count() by bin(timestamp, 1h)
| render timechart

// Chart: Top 10 produtos mais vendidos
customEvents
| where name == "ItemVendido"
| where timestamp > ago(7d)
| extend produtoNome = tostring(customDimensions.produtoNome)
| summarize quantidade = sum(toint(customMeasurements.quantidade)) by produtoNome
| top 10 by quantidade desc
| render barchart
```

### **19.8.2. Operations Dashboard**

```kusto
// Response time por endpoint (p50, p95, p99)
requests
| where timestamp > ago(1h)
| summarize
    p50 = percentile(duration, 50),
    p95 = percentile(duration, 95),
    p99 = percentile(duration, 99)
  by operation_Name
| render barchart

// Dependências externas - latência
dependencies
| where timestamp > ago(1h)
| summarize avg(duration), p95 = percentile(duration, 95) by name
| render barchart

// Exceptions por tipo
exceptions
| where timestamp > ago(24h)
| summarize count() by type
| render piechart
```

### **19.8.3. Business Dashboard**

```kusto
// NFCe - Status distribution
customEvents
| where name startswith "NFCe"
| where timestamp > ago(24h)
| summarize count() by name
| render piechart

// Vendas por tipo (PDV, B2B, B2C, ML)
customEvents
| where name == "VendaFinalizada"
| where timestamp > ago(7d)
| extend tipo = tostring(customDimensions.tipo)
| summarize count() by tipo
| render piechart

// Forma de pagamento - Distribuição
customEvents
| where name == "VendaFinalizada"
| where timestamp > ago(7d)
| extend formaPagamento = tostring(customDimensions.formaPagamento)
| summarize count() by formaPagamento
| render piechart
```

---

## 19.9. SLIs, SLOs, and SLAs

### **Service Level Indicators (SLIs)**

| SLI | Measurement | Target |
|-----|-------------|--------|
| **Availability** | Successful requests / Total requests | 99.5% |
| **Latency** | p95 response time | < 200ms |
| **Error Rate** | Failed requests / Total requests | < 0.1% |
| **Throughput** | Requests per second | > 100 req/s |

### **Service Level Objectives (SLOs)**

```
Availability SLO: 99.5% uptime mensalmente
- Downtime permitido: ~3.6 horas/mês
- Medição: (successful_requests / total_requests) * 100

Latency SLO: 95% das requisições < 200ms
- Medição: percentile(request_duration, 95) < 200ms

Error Budget:
- 0.5% de requisições podem falhar
- 100,000 req/mês → 500 falhas permitidas
```

### **Service Level Agreements (SLAs)**

```
Acordo com clientes:
- 99.5% uptime mensal
- Créditos: 10% se < 99.5%, 25% se < 99%
- Suporte: Resposta em 4h para issues críticos
```

---

## 19.10. Monitoring Checklist

**Before Going to Production:**

- [ ] Application Insights configurado e testado
- [ ] Dashboards criados (Executive, Operations, Business)
- [ ] Alertas configurados (Critical, Error, Warning)
- [ ] Health checks implementados para todas dependências
- [ ] Log aggregation funcionando (KQL queries testadas)
- [ ] Distributed tracing funcionando end-to-end
- [ ] Custom metrics para KPIs de negócio
- [ ] Runbooks criados para incidentes comuns
- [ ] On-call rotation definida
- [ ] Incident response process documentado

**Continuous Monitoring:**

- [ ] Review dashboards diariamente
- [ ] Analisar trends semanalmente
- [ ] Ajustar thresholds de alertas mensalmente
- [ ] Review SLOs trimestralmente
- [ ] Post-mortem após incidentes
