package pl.smarthouse.fireplacemodule.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.configurations.FireplaceModuleConfiguration;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;

@Service
@RequiredArgsConstructor
public class FireplaceModuleService {
  private final FireplaceModuleConfiguration fireplaceModuleConfiguration;
  private final ModelMapper modelMapper = new ModelMapper();

  public FireplaceModuleDto getFireplaceModule() {
    return modelMapper.map(
        fireplaceModuleConfiguration.getFireplaceModuleDao(), FireplaceModuleDto.class);
  }

  public Ds18b20Result getWaterInSensor() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getWaterIn();
  }

  public Ds18b20Result getWaterOutSensor() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getWaterOut();
  }

  public Ds18b20Result getChimneySensor() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getChimney();
  }
}
