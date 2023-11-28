package pl.smarthouse.fireplacemodule.chain;

import static pl.smarthouse.fireplacemodule.properties.PumpProperties.PUMP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleService;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pin.Pin;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinCommandType;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinState;

@Service
public class PumpChain {
  private final FireplaceModuleService fireplaceModuleService;
  private final Pin pump;

  public PumpChain(
      @Autowired final FireplaceModuleService fireplaceModuleService,
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
    this.fireplaceModuleService = fireplaceModuleService;
    pump = (Pin) esp32ModuleConfig.getConfiguration().getActorMap().getActor(PUMP);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Circuit pump");
    // Wait 10 seconds and set correct default state
    chain.addStep(createStep1());
    // Wait until correct default state and set pump accordingly
    chain.addStep(createStep2());
    // Wait until response updated and set NO_ACTION
    chain.addStep(createStep3());
    return chain;
  }

  private Step createStep1() {

    return Step.builder()
        .stepDescription("Set correct default state")
        .conditionDescription("Waiting 10 seconds")
        .condition(PredicateUtils.delaySeconds(10))
        .action(createActionStep1())
        .build();
  }

  private Runnable createActionStep1() {

    return () -> {
      if (!isCorrectDefaultState()) {
        pump.getCommandSet().setCommandType(PinCommandType.SET_DEFAULT_STATE);
        pump.getCommandSet().setValue(isOn() ? PinState.LOW.toString() : PinState.HIGH.toString());
      }
    };
  }

  private Step createStep2() {

    return Step.builder()
        .stepDescription("Set pump accordingly")
        .conditionDescription("Wait until correct default state")
        .condition(step -> isCorrectDefaultState())
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {

    return () -> {
      pump.getCommandSet().setCommandType(PinCommandType.SET);
      pump.getCommandSet().setValue(isOn() ? PinState.LOW.toString() : PinState.HIGH.toString());
    };
  }

  private Step createStep3() {
    return Step.builder()
        .stepDescription("Set NO_ACTION")
        .conditionDescription("Wait until response updated")
        .condition(PredicateUtils.isResponseUpdated(pump))
        .action(createActionStep3())
        .build();
  }

  private Runnable createActionStep3() {
    return () -> {
      fireplaceModuleService.setPump(
          pump.getResponse().getPinState().equals(PinState.HIGH) ? State.OFF : State.ON);
      pump.getCommandSet().setCommandType(PinCommandType.NO_ACTION);
    };
  }

  private boolean isOn() {
    return fireplaceModuleService.getState().equals(State.ON);
  }

  private boolean isCorrectDefaultState() {
    return pump.getResponse() != null
        && ((isOn() && PinState.LOW.equals(pump.getResponse().getPinDefaultState()))
            || (!isOn() && PinState.HIGH.equals(pump.getResponse().getPinDefaultState())));
  }
}
