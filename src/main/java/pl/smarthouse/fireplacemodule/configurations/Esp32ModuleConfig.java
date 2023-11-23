package pl.smarthouse.fireplacemodule.configurations;

import static pl.smarthouse.fireplacemodule.properties.Ds18b20SensorsProperties.*;
import static pl.smarthouse.fireplacemodule.properties.Esp32ModuleProperties.*;
import static pl.smarthouse.fireplacemodule.properties.PumpProperties.*;
import static pl.smarthouse.fireplacemodule.properties.ThrottleProperties.*;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.smartmodule.model.actors.actor.ActorMap;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20CompFactor;
import pl.smarthouse.smartmodule.model.actors.type.pin.Pin;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinMode;
import pl.smarthouse.smartmodule.model.actors.type.pwm.Pwm;
import pl.smarthouse.smartmodule.services.ManagerService;
import pl.smarthouse.smartmodule.services.ModuleService;

@Configuration
@RequiredArgsConstructor
@Getter
public class Esp32ModuleConfig {
  private final ModuleService moduleService;
  private final ManagerService managerService;

  // Module specific
  private pl.smarthouse.smartmodule.model.configuration.Configuration configuration;

  @PostConstruct
  public void postConstruct() {
    configuration =
        new pl.smarthouse.smartmodule.model.configuration.Configuration(
            MODULE_TYPE, FIRMWARE, VERSION, MAC_ADDRESS, createActors());
    moduleService.setConfiguration(configuration);
    managerService.setConfiguration(configuration);
  }

  private ActorMap createActors() {
    final ActorMap actorMap = new ActorMap();
    // Exchanger
    final Ds18b20 ds18b20 = new Ds18b20(THERMOMETERS, THERMOMETERS_DS18B20_PIN);
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            THERMOMETER_WATER_IN,
            Ds18b20CompFactor.builder()
                .gradient(THERMOMETERS_WATER_IN_GRADIENT)
                .intercept(THERMOMETERS_WATER_IN_INTERCEPT)
                .build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            THERMOMETERS_WATER_OUT,
            Ds18b20CompFactor.builder()
                .gradient(THERMOMETERS_WATER_OUT_GRADIENT)
                .intercept(THERMOMETERS_WATER_OUT_INTERCEPT)
                .build());
    ds18b20
        .getDs18b20CompFactorMap()
        .put(
            THERMOMETERS_CHIMNEY,
            Ds18b20CompFactor.builder()
                .gradient(THERMOMETERS_CHIMNEY_GRADIENT)
                .intercept(THERMOMETERS_CHIMNEY_INTERCEPT)
                .build());
    actorMap.putActor(ds18b20);

    // Circuit pump
    actorMap.putActor(
        new Pin(PUMP, PUMP_PIN, PinMode.OUTPUT, PUMP_DEFAULT_STATE, PUMP_DEFAULT_ENABLED));
    // Throttle
    actorMap.putActor(
        new Pwm(
            THROTTLE,
            THROTTLE_CHANNEL,
            THROTTLE_FREQUENCY,
            THROTTLE_RESOLUTION,
            THROTTLE_PIN,
            THROTTLE_DEFAULT_DUTY_CYCLE,
            THROTTLE_DEFAULT_ENABLED));
    return actorMap;
  }
}
