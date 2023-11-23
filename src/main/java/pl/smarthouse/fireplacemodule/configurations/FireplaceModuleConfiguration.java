package pl.smarthouse.fireplacemodule.configurations;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.fireplacemodule.model.dao.FireplaceModuleDao;
import pl.smarthouse.fireplacemodule.properties.Esp32ModuleProperties;
import pl.smarthouse.sharedobjects.dto.fireplace.core.Throttle;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;
import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
import pl.smarthouse.smartmonitoring.model.EnumCompareProperties;
import pl.smarthouse.smartmonitoring.model.NumberCompareProperties;
import pl.smarthouse.smartmonitoring.properties.defaults.Ds18b20DefaultProperties;
import pl.smarthouse.smartmonitoring.service.CompareProcessor;
import pl.smarthouse.smartmonitoring.service.MonitoringService;

@Configuration
@RequiredArgsConstructor
@Getter
@Slf4j
public class FireplaceModuleConfiguration {
  private final CompareProcessor compareProcessor;
  private final MonitoringService monitoringService;
  private final Esp32ModuleConfig esp32ModuleConfig;
  private final Esp32ModuleProperties esp32ModuleProperties;
  private FireplaceModuleDao fireplaceModuleDao;

  @PostConstruct
  void postConstruct() {
    final Throttle throttle = new Throttle();
    throttle.setGoalPosition(0);
    throttle.setCurrentPosition(1);
    fireplaceModuleDao =
        FireplaceModuleDao.builder()
            .moduleName(Esp32ModuleProperties.MODULE_TYPE)
            .mode(Mode.STANDBY)
            .state(State.OFF)
            .waterIn(new Ds18b20Result())
            .waterOut(new Ds18b20Result())
            .chimney(new Ds18b20Result())
            .pump(State.OFF)
            .throttle(throttle)
            .build();
    monitoringService.setModuleDaoObject(fireplaceModuleDao);
    setCompareProperties();
  }

  private void setCompareProperties() {
    compareProcessor.addMap("error", BooleanCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "errorPendingAcknowledge", BooleanCompareProperties.builder().saveEnabled(true).build());
    Ds18b20DefaultProperties.setDefaultProperties(compareProcessor, "waterIn");
    Ds18b20DefaultProperties.setDefaultProperties(compareProcessor, "waterOut");
    Ds18b20DefaultProperties.setDefaultProperties(compareProcessor, "chimney");
    compareProcessor.addMap("mode", EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap("pump", EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap("state", EnumCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "throttle.currentPosition",
        NumberCompareProperties.builder().saveTolerance(1).saveEnabled(true).build());
    compareProcessor.addMap(
        "throttle.goalPosition",
        NumberCompareProperties.builder().saveTolerance(1).saveEnabled(true).build());
  }
}
