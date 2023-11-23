package pl.smarthouse.fireplacemodule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;

@RequiredArgsConstructor
public class ModeHandlerService {
  private final FireplaceModuleService fireplaceModuleService;
  private final FireplaceModuleParamsService fireplaceModuleParamsService;

  @Scheduled(fixedDelay = 10000)
  void handleMode() {
    if (fireplaceModuleService.getWaterInSensor().isError()
        || fireplaceModuleService.getWaterOutSensor().isError()) {
      fireplaceModuleService.setMode(Mode.ERROR);
      return;
    } else {
      fireplaceModuleService.setMode(Mode.OFF);
    }

    if (fireplaceModuleService.isOn()) {
      setModeWhenFireplaceIsOn();
    } else {
      setModeWhenFireplaceIsOff();
    }
  }

  private void setModeWhenFireplaceIsOn() {
    final Mode currentMode = fireplaceModuleService.getMode();
    final double waterOutTemperature = fireplaceModuleService.getWaterOutSensor().getTemp();

    if (currentMode.equals(Mode.OFF)) {
      fireplaceModuleService.setMode(Mode.STANDBY);
    }

    if (waterOutTemperature >= fireplaceModuleParamsService.getParams().getWorkingTemperature()) {
      fireplaceModuleService.setMode(Mode.HEATING);
    }
  }

  private void setModeWhenFireplaceIsOff() {
    final double absDeltaTemp =
        Math.abs(
            fireplaceModuleService.getWaterInSensor().getTemp()
                - fireplaceModuleService.getWaterOutSensor().getTemp());

    if (absDeltaTemp > 1.0) {
      fireplaceModuleService.setMode(Mode.COOLING);
    } else {
      fireplaceModuleService.setMode(Mode.OFF);
    }
  }
}
