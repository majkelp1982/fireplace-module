package pl.smarthouse.fireplacemodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import pl.smarthouse.fireplacemodule.configurations.VentilationModuleConfiguration;
import pl.smarthouse.fireplacemodule.exceptions.VentilationModuleServiceResponseException;
import reactor.core.publisher.Mono;

@EnableScheduling
@Service
@RequiredArgsConstructor
@Slf4j
public class VentilationModuleService {
  private static final String VENTILATION_MODULE_TYPE = "VENTILATION";
  private final VentilationModuleConfiguration ventilationModuleConfiguration;
  private final ModuleManagerService moduleManagerService;
  private final FireplaceModuleService fireplaceModuleService;

  private final String SERVICE_ADDRESS_REGEX =
      "^(?:http:\\/\\/)?\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$";

  @Scheduled(fixedDelay = 10000)
  private void sendOverpressureForceCommandToVentIfNeeded() {
    if (fireplaceModuleService.isOn()) {
      sendVentOverpressureForceCommand().block();
    }
  }

  private Mono<String> retrieveVentilationServiceBaseUrl() {
    return Mono.justOrEmpty(ventilationModuleConfiguration.getBaseUrl())
        .switchIfEmpty(
            Mono.defer(() -> moduleManagerService.getServiceAddress(VENTILATION_MODULE_TYPE)))
        .flatMap(
            baseUrl -> {
              if (!baseUrl.matches(SERVICE_ADDRESS_REGEX)) {
                Mono.error(
                    new IllegalArgumentException(
                        String.format(
                            "Base url have to contain http address. Current: %s", baseUrl)));
              }
              ventilationModuleConfiguration.setBaseUrl(baseUrl);
              return Mono.just(baseUrl);
            });
  }

  private Mono<Void> sendVentOverpressureForceCommand() {
    return retrieveVentilationServiceBaseUrl()
        .flatMap(signal -> Mono.just(ventilationModuleConfiguration.getWebClient()))
        .flatMap(
            webClient ->
                webClient
                    .post()
                    .uri(uriBuilder -> uriBuilder.path("/vent/overpressure/force").build())
                    .exchangeToMono(this::processResponse))
        .doOnError(
            throwable -> {
              ventilationModuleConfiguration.resetBaseUrl();
              log.error(
                  "Error occurred on sendVentOverpressureForceCommand. Reason: {}",
                  throwable.getMessage(),
                  throwable);
            });
  }

  private Mono<Void> processResponse(final ClientResponse clientResponse) {
    if (clientResponse.statusCode().is2xxSuccessful()) {
      return clientResponse.bodyToMono(Void.class);
    } else {
      return clientResponse
          .bodyToMono(String.class)
          .flatMap(
              response ->
                  Mono.error(
                      new VentilationModuleServiceResponseException(
                          clientResponse.statusCode(), response)));
    }
  }
}
