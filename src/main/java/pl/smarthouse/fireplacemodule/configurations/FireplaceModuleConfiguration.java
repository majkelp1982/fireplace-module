package pl.smarthouse.fireplacemodule.configurations;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import pl.smarthouse.fireplacemodule.model.dao.FireplaceModuleDao;
import pl.smarthouse.fireplacemodule.properties.Esp32ModuleProperties;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;
import pl.smarthouse.smartmonitoring.model.BooleanCompareProperties;
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
    fireplaceModuleDao =
        FireplaceModuleDao.builder()
            .moduleName(Esp32ModuleProperties.MODULE_TYPE)
            .waterIn(new Ds18b20Result())
            .waterOut(new Ds18b20Result())
            .chimney(new Ds18b20Result())
            .pump(State.OFF)
            .build();
    monitoringService.setModuleDaoObject(fireplaceModuleDao);
    setCompareProperties();
  }

  private void setCompareProperties() {
    compareProcessor.addMap("error", BooleanCompareProperties.builder().saveEnabled(true).build());
    compareProcessor.addMap(
        "errorPendingAcknowledge", BooleanCompareProperties.builder().saveEnabled(true).build());
    Ds18b20DefaultProperties.setDefaultProperties(compareProcessor, "waterIn");
    Ds18b20DefaultProperties.setDefaultProperties(compareProcessor, "waterIn");
    Ds18b20DefaultProperties.setDefaultProperties(compareProcessor, "chimney");
  }
}
