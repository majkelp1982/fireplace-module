package pl.smarthouse.fireplacemodule.error;

import static pl.smarthouse.fireplacemodule.properties.Ds18b20SensorsProperties.*;

import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleService;
import pl.smarthouse.smartmodule.utils.errorpredictions.Ds18b20ErrorPredictionsUtils;
import pl.smarthouse.smartmonitoring.service.ErrorHandlingService;

@Configuration
@RequiredArgsConstructor
public class Ds18b20ErrorPredictions {

  private final FireplaceModuleService fireplaceModuleService;
  private final ErrorHandlingService errorHandlingService;

  @PostConstruct
  public void postConstructor() {
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService, THERMOMETER_WATER_IN, fireplaceModuleService::getWaterInSensor);
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService, THERMOMETERS_WATER_OUT, fireplaceModuleService::getWaterOutSensor);
    Ds18b20ErrorPredictionsUtils.setDs180b20SensorsErrorPredictions(
        errorHandlingService, THERMOMETERS_CHIMNEY, fireplaceModuleService::getChimneySensor);
  }
}
