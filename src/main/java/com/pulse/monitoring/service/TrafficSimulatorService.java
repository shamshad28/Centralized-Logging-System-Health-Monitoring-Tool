package com.pulse.monitoring.service;

import com.pulse.monitoring.dto.LogIngestionRequest;
import com.pulse.monitoring.model.ServiceNode;
import com.pulse.monitoring.repository.ServiceNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class TrafficSimulatorService {

    @Autowired
    private LogIngestionService logIngestionService;

    @Autowired
    private ServiceNodeRepository serviceNodeRepository;

    private boolean simulatorEnabled = true;
    private final Random random = new Random();

    private static final String[] SERVICES = {"order-service", "payment-gateway", "auth-service", "inventory-api"};
    private static final String[] ENVIRONMENTS = {"PRODUCTION", "STAGING", "PRODUCTION", "PRODUCTION"};

    private static final String[] INFO_MESSAGES = {
        "User authenticated successfully via OAuth2 JWT Token",
        "Fetched user profile for user_id=%s",
        "Payment processed successfully for order_id=%s amount=$%.2f",
        "Inventory reserved for SKU-%05d (quantity: %d)",
        "Order #%s created and queued for dispatch",
        "HTTP GET /api/v1/catalog HTTP/1.1 200 OK (24ms)",
        "Health check probe passed - all database pools healthy",
        "Cache hit for key 'product_category_%d'"
    };

    private static final String[] WARN_MESSAGES = {
        "High DB connection pool usage (85%% capacity occupied)",
        "Slow query detected on table 'orders' execution time 1450ms",
        "Payment gateway response latency spiked to 3200ms",
        "Rate limit threshold (90%%) approached for IP %s",
        "Deprecated API endpoint /v1/checkout accessed by client %s"
    };

    private static final String[] ERROR_MESSAGES = {
        "DatabaseConnectionException: Connection pool exhausted trying to reach DB master node",
        "PaymentFailedException: Credit card charge declined by Stripe upstream gateway (Error 402)",
        "NullPointerException: Cannot invoke 'UserSession.getRoles()' because 'session' is null",
        "HikariPool-1 - Connection is not available, request timed out after 30000ms",
        "HttpClientErrorException: 502 Bad Gateway from auth.internal.company.com",
        "InventoryOutOfStockException: Item SKU-%05d is out of stock in warehouse-east"
    };

    private static final String SAMPLE_STACK_TRACE = 
        "java.lang.NullPointerException: Cannot read field \"userId\" because \"user\" is null\n" +
        "\tat com.pulse.service.OrderProcessor.processOrder(OrderProcessor.java:142)\n" +
        "\tat com.pulse.controller.OrderController.createOrder(OrderController.java:58)\n" +
        "\tat jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)\n" +
        "\tat java.lang.reflect.Method.invoke(Method.java:580)\n" +
        "\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:254)\n" +
        "\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:182)\n" +
        "\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)";

    @Scheduled(fixedRate = 2500) // Generate simulated logs every 2.5 seconds
    public void generateSimulatedTraffic() {
        if (!simulatorEnabled) return;

        int index = random.nextInt(SERVICES.length);
        String service = SERVICES[index];
        String env = ENVIRONMENTS[index];

        int roll = random.nextInt(100);
        String logLevel;
        String message;
        String stackTrace = null;

        if (roll < 70) {
            logLevel = "INFO";
            String tpl = INFO_MESSAGES[random.nextInt(INFO_MESSAGES.length)];
            message = String.format(tpl, UUID.randomUUID().toString().substring(0, 6), 
                                         random.nextDouble() * 150 + 10, 
                                         random.nextInt(9999), 
                                         random.nextInt(5) + 1, 
                                         random.nextInt(50));
        } else if (roll < 88) {
            logLevel = "WARN";
            String tpl = WARN_MESSAGES[random.nextInt(WARN_MESSAGES.length)];
            message = String.format(tpl, "192.168.1." + (random.nextInt(200) + 1));
        } else {
            logLevel = "ERROR";
            message = ERROR_MESSAGES[random.nextInt(ERROR_MESSAGES.length)];
            stackTrace = SAMPLE_STACK_TRACE;
        }

        LogIngestionRequest req = new LogIngestionRequest();
        req.setServiceName(service);
        req.setEnvironment(env);
        req.setLogLevel(logLevel);
        req.setMessage(message);
        req.setTraceId(UUID.randomUUID().toString().substring(0, 8));
        req.setSpanId(UUID.randomUUID().toString().substring(0, 4));
        req.setLoggerName("com.microservice." + service.replace("-", "") + ".Service");
        req.setThreadName("http-nio-8080-exec-" + (random.nextInt(10) + 1));
        req.setHostIp("10.0.4." + (random.nextInt(50) + 10));
        req.setExceptionStackTrace(stackTrace);
        req.setTimestamp(LocalDateTime.now());
        req.setTags("{\"cluster\":\"us-east-1\", \"attempt\":" + (random.nextInt(3) + 1) + "}");

        logIngestionService.ingestLog(req);
    }

    public boolean isSimulatorEnabled() {
        return simulatorEnabled;
    }

    public void setSimulatorEnabled(boolean enabled) {
        this.simulatorEnabled = enabled;
    }

    public void injectFault(String serviceName, String faultType) {
        if ("DOWN".equalsIgnoreCase(faultType)) {
            serviceNodeRepository.findByServiceName(serviceName).ifPresent(node -> {
                node.setStatus("DOWN");
                node.setResponseTimeMs(0L);
                node.setLastChecked(LocalDateTime.now());
                serviceNodeRepository.save(node);
            });
        }

        // Inject 5 burst error logs
        for (int i = 0; i < 6; i++) {
            LogIngestionRequest req = new LogIngestionRequest();
            req.setServiceName(serviceName);
            req.setEnvironment("PRODUCTION");
            req.setLogLevel("ERROR");
            req.setMessage("CRITICAL SYNTHETIC INJECTED FAULT [" + faultType + "]: Microservice outage detected!");
            req.setTraceId(UUID.randomUUID().toString().substring(0, 8));
            req.setExceptionStackTrace(SAMPLE_STACK_TRACE);
            req.setTimestamp(LocalDateTime.now());
            logIngestionService.ingestLog(req);
        }
    }
}
