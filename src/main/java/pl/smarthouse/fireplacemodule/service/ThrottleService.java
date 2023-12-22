package pl.smarthouse.fireplacemodule.service;

import static pl.smarthouse.fireplacemodule.properties.ThrottleProperties.*;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class ThrottleService {
  private final FireplaceModuleService fireplaceModuleService;
  private final FireplaceModuleParamsService fireplaceModuleParamsService;

  @Scheduled(fixedDelay = 30000)
  void recalculateGoalPosition() {
    if (isForceClose()) {
      fireplaceModuleService.getThrottle().setGoalPosition(THROTTLE_GOAL_POSITION_CLOSED);
      return;
    }
    int goalPosition = fireplaceModuleService.getThrottle().getGoalPosition();
    final double requiredTemp = fireplaceModuleParamsService.getParams().getWorkingTemperature();
    final double currentTemp = fireplaceModuleService.getWaterOutSensor().getTemp();

    if (currentTemp < requiredTemp) {
      goalPosition += THROTTLE_GOAL_POSITION_STEP;
    }

    if (currentTemp > requiredTemp) {
      goalPosition = THROTTLE_GOAL_POSITION_CLOSED;
    }

    goalPosition = validateGoalPosition(goalPosition);

    fireplaceModuleService.getThrottle().setGoalPosition(goalPosition);
  }

  private int validateGoalPosition(final int resultGoalPosition) {
    if (resultGoalPosition > THROTTLE_GOAL_POSITION_FULL_OPEN) {
      return THROTTLE_GOAL_POSITION_FULL_OPEN;
    }

    if (resultGoalPosition < THROTTLE_GOAL_POSITION_CLOSED) {
      return THROTTLE_GOAL_POSITION_CLOSED;
    }

    return resultGoalPosition;
  }

  public boolean isPositionCorrect() {
    return fireplaceModuleService.getThrottle().getGoalPosition()
        == fireplaceModuleService.getThrottle().getCurrentPosition();
  }

  public int getCalculatedThrottleDutyCycle() {
    final double dutyCyclePerPercent = (THROTTLE_DUTY_CYCLE_100 - THROTTLE_DUTY_CYCLE_0) / 100.00;

    return (int)
        (THROTTLE_DUTY_CYCLE_0
            + fireplaceModuleService.getThrottle().getGoalPosition() * dutyCyclePerPercent);
  }

  public void setGoalPositionAsCurrent() {
    fireplaceModuleService
        .getThrottle()
        .setCurrentPosition(fireplaceModuleService.getThrottle().getGoalPosition());
  }

  private boolean isForceClose() {
    return fireplaceModuleService.getWaterOutSensor().isError() || !fireplaceModuleService.isOn();
  }
}
