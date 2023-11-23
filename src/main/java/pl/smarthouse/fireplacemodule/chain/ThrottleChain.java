package pl.smarthouse.fireplacemodule.chain;

import static pl.smarthouse.fireplacemodule.properties.ThrottleProperties.*;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.fireplacemodule.service.DutyCycleService;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleParamsService;
import pl.smarthouse.fireplacemodule.service.FireplaceModuleService;
import pl.smarthouse.sharedobjects.dto.fireplace.core.Throttle;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pin.PinCommandType;
import pl.smarthouse.smartmodule.model.actors.type.pwm.Pwm;
import pl.smarthouse.smartmodule.model.actors.type.pwm.PwmCommandType;

@Service
public class ThrottleChain {
  private final FireplaceModuleService fireplaceModuleService;
  private final FireplaceModuleParamsService fireplaceModuleParamsService;
  private final DutyCycleService dutyCycleService;
  private final Pwm throttleActor;

  public ThrottleChain(
      @Autowired final FireplaceModuleService fireplaceModuleService,
      @Autowired final FireplaceModuleParamsService fireplaceModuleParamsService,
      @Autowired final DutyCycleService dutyCycleService,
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
    this.fireplaceModuleService = fireplaceModuleService;
    this.fireplaceModuleParamsService = fireplaceModuleParamsService;
    this.dutyCycleService = dutyCycleService;
    throttleActor = (Pwm) esp32ModuleConfig.getConfiguration().getActorMap().getActor(THROTTLE);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Throttle");
    // Wait 1 minute or cooling forced and set throttle position accordingly
    chain.addStep(createStep1());
    // Send new goal value if needed
    chain.addStep(createStep2());
    // Wait until response and set NO_ACTION
    chain.addStep(createStep3());
    return chain;
  }

  private Step createStep1() {

    return Step.builder()
        .stepDescription("Calculate goal position")
        .conditionDescription("Waiting 60 seconds")
        .condition(PredicateUtils.delaySeconds(60).or(step -> isForceCloseThrottle()))
        .action(createActionStep1())
        .build();
  }

  private Runnable createActionStep1() {
    return () -> {
      int goalPosition = fireplaceModuleService.getThrottle().getCurrentPosition();
      final double requiredTemp = fireplaceModuleParamsService.getParams().getWorkingTemperature();
      final double currentTemp = fireplaceModuleService.getWaterOutSensor().getTemp();

      if (currentTemp < requiredTemp) {
        goalPosition += 10;
      }

      if (currentTemp > requiredTemp) {
        goalPosition -= 10;
      }

      if (goalPosition > 100) {
        goalPosition = 100;
      }

      if (goalPosition < 0) {
        goalPosition = 0;
      }
      fireplaceModuleService.getThrottle().setGoalPosition(goalPosition);
    };
  }

  private Step createStep2() {
    return Step.builder()
        .stepDescription("Send new goal value if needed")
        .conditionDescription("No condition")
        .condition((step -> true))
        .action(createActionStep2())
        .build();
  }

  private Runnable createActionStep2() {
    return () -> {
      final Throttle throttle = fireplaceModuleService.getThrottle();
      if (throttle.getCurrentPosition() != throttle.getGoalPosition()) {
        throttleActor.getCommandSet().setCommandType(PwmCommandType.DUTY_CYCLE);
        throttleActor
            .getCommandSet()
            .setValue(
                String.valueOf(
                    dutyCycleService.recalculateByGoalPosition(throttle.getGoalPosition())));
      }
    };
  }

  private Step createStep3() {
    return Step.builder()
        .stepDescription("Set NO_ACTION")
        .conditionDescription("Wait until response updated")
        .condition(PredicateUtils.isResponseUpdated(throttleActor))
        .action(createActionStep3())
        .build();
  }

  private Runnable createActionStep3() {
    return () -> {
      throttleActor.getCommandSet().setCommandType(PinCommandType.NO_ACTION);
    };
  }

  private boolean isForceCloseThrottle() {
    final List throttleCloseModes = List.of(Mode.ERROR, Mode.OFF);
    return throttleCloseModes.contains(fireplaceModuleService.getMode())
        && fireplaceModuleService.getThrottle().getCurrentPosition() != 0;
  }
}
