package pl.smarthouse.fireplacemodule.chain;

import static pl.smarthouse.fireplacemodule.properties.ThrottleProperties.*;

import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.smarthouse.fireplacemodule.configurations.Esp32ModuleConfig;
import pl.smarthouse.fireplacemodule.service.ThrottleService;
import pl.smarthouse.smartchain.model.core.Chain;
import pl.smarthouse.smartchain.model.core.Step;
import pl.smarthouse.smartchain.service.ChainService;
import pl.smarthouse.smartchain.utils.PredicateUtils;
import pl.smarthouse.smartmodule.model.actors.type.pca9685.Pca9685CommandType;
import pl.smarthouse.smartmodule.model.actors.type.pwm.Pwm;
import pl.smarthouse.smartmodule.model.actors.type.pwm.PwmCommandType;

@Service
public class ThrottleChain {
  private final ThrottleService throttleService;
  private final Pwm throttleActor;

  public ThrottleChain(
      @Autowired final ThrottleService throttleService,
      @Autowired final ChainService chainService,
      @Autowired final Esp32ModuleConfig esp32ModuleConfig) {
    this.throttleService = throttleService;
    throttleActor = (Pwm) esp32ModuleConfig.getConfiguration().getActorMap().getActor(THROTTLE);
    final Chain chain = createChain();
    chainService.addChain(chain);
  }

  private Chain createChain() {
    final Chain chain = new Chain("Throttle");
    // Wait for throttles goal position change or 120 seconds and drive to goal position
    chain.addStep(createStep1());
    // Wait for response and after set NO_ACTION
    chain.addStep(createStep2());
    // Wait 1s and release servo motor
    chain.addStep(createStep3());
    // Wait for response and set NO_ACTION
    chain.addStep(waitAndSetNoAction());
    return chain;
  }

  private Step createStep1() {
    return Step.builder()
        .conditionDescription("Wait for throttles goal position change or 120 seconds")
        .condition(createConditionStep1().or(PredicateUtils.delaySeconds(120)))
        .stepDescription("Drive throttle to goal position")
        .action(createActionStep1())
        .build();
  }

  private Predicate<Step> createConditionStep1() {
    return step -> !throttleService.isPositionCorrect();
  }

  private Runnable createActionStep1() {
    return () -> {
      throttleActor.getCommandSet().setCommandType(PwmCommandType.DUTY_CYCLE);
      throttleActor
          .getCommandSet()
          .setValue(String.valueOf(throttleService.getCalculatedThrottleDutyCycle()));
    };
  }

  private Step createStep2() {
    return Step.builder()
        .conditionDescription("Wait for response")
        .condition(createConditionStep2())
        .stepDescription("Set NO_ACTION")
        .action(setNoAction())
        .build();
  }

  private Predicate<Step> createConditionStep2() {
    return PredicateUtils.isResponseUpdated(throttleActor);
  }

  private Step createStep3() {
    return Step.builder()
        .conditionDescription("Wait 1 seconds")
        .condition(PredicateUtils.delaySeconds(1))
        .stepDescription("Write current position and release motor")
        .action(writeCurrentPositionAndReleaseAllServoMotors())
        .build();
  }

  private Runnable writeCurrentPositionAndReleaseAllServoMotors() {
    return () -> {
      throttleService.setGoalPositionAsCurrent();
      throttleActor.getCommandSet().setCommandType(PwmCommandType.DUTY_CYCLE);
      throttleActor.getCommandSet().setValue(Integer.toString(0));
    };
  }

  private Step waitAndSetNoAction() {
    return Step.builder()
        .conditionDescription("Wait for response")
        .condition(PredicateUtils.isResponseUpdated(throttleActor))
        .stepDescription("Set NO_ACTION")
        .action(setNoAction())
        .build();
  }

  private Runnable setNoAction() {
    return () -> throttleActor.getCommandSet().setCommandType(Pca9685CommandType.NO_ACTION);
  }
}
