package pl.smarthouse.fireplacemodule.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.configurations.FireplaceModuleConfiguration;
import pl.smarthouse.sharedobjects.dto.fireplace.FireplaceModuleDto;
import pl.smarthouse.sharedobjects.dto.fireplace.core.Throttle;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
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

  public State stateToggle() {
    fireplaceModuleConfiguration
        .getFireplaceModuleDao()
        .setState(
            State.ON.equals(fireplaceModuleConfiguration.getFireplaceModuleDao().getState())
                ? State.OFF
                : State.ON);
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getState();
  }

  public void setWaterIn(final Ds18b20Result ds18b20Result) {
    fireplaceModuleConfiguration.getFireplaceModuleDao().setWaterIn(ds18b20Result);
  }

  public void setWaterOut(final Ds18b20Result ds18b20Result) {
    fireplaceModuleConfiguration.getFireplaceModuleDao().setWaterOut(ds18b20Result);
  }

  public void setChimney(final Ds18b20Result ds18b20Result) {
    fireplaceModuleConfiguration.getFireplaceModuleDao().setChimney(ds18b20Result);
  }

  public String getModuleName() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getModuleName();
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

  public State getState() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getState();
  }

  public Mode getMode() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getMode();
  }

  public void setMode(final Mode mode) {
    fireplaceModuleConfiguration.getFireplaceModuleDao().setMode(mode);
  }

  public boolean isOn() {
    return getState().equals(State.ON);
  }

  public Throttle getThrottle() {
    return fireplaceModuleConfiguration.getFireplaceModuleDao().getThrottle();
  }
}
