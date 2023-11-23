package pl.smarthouse.fireplacemodule.chain;

import static org.springframework.data.util.Predicates.negate;
import static pl.smarthouse.fireplacemodule.properties.Ds18b20SensorsProperties.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleService;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20CommandType;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Utils;

@Service
public class Ds18b20Chain {
  private final FireplaceModuleService fireplaceModuleService;
  private final Ds18b20 thermometers;

  public Ds18b20Chain(
      @Autowired final FireplaceModuleService fireplaceModuleService,
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
    this.fireplaceModuleService = fireplaceModuleService;
    thermometers =
        (Ds18b20) esp32ModuleConfig.getConfiguration().getActorMap().getActor(THERMOMETERS);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Read DS18B20 sensors");
    // Wait 10 seconds and read values from each sensor type DS18B20
    chain.addStep(createStep1());
    // Wait until command read successful and set command to NO_ACTION for all
    chain.addStep(createStep2());
    return chain;
  }

  private Step createStep1() {

    return Step.builder()
        .stepDescription("Read values from each sensor type DS18B20")
        .conditionDescription("Waiting 30 seconds")
        .condition(PredicateUtils.delaySeconds(30))
        .action(createActionStep1())
        .build();
  }

  private Runnable createActionStep1() {

    return () -> {
      thermometers.getCommandSet().setCommandType(Ds18b20CommandType.READ);
      thermometers
          .getCommandSet()
          .setValue(
              Ds18b20Utils.getDs18b20Command(
                  THERMOMETER_WATER_IN, THERMOMETERS_WATER_OUT, THERMOMETERS_CHIMNEY));
    };
  }

  private Step createStep2() {

    return Step.builder()
        .stepDescription("Set DS18B20 commands to NO_ACTION")
        .conditionDescription("Wait until command read successful")
        .condition(
            PredicateUtils.isResponseUpdated(thermometers)
                .and(negate(PredicateUtils.isErrorOnDs18b20Group(thermometers.getResponse()))))
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {

    return () -> {
      fireplaceModuleService.setWaterIn(
          thermometers.getResponse().getSensorResult(THERMOMETER_WATER_IN));
      fireplaceModuleService.setWaterOut(
          thermometers.getResponse().getSensorResult(THERMOMETERS_WATER_OUT));
      fireplaceModuleService.setChimney(
          thermometers.getResponse().getSensorResult(THERMOMETERS_CHIMNEY));
      thermometers.getCommandSet().setCommandType(Ds18b20CommandType.NO_ACTION);
    };
  }
}
