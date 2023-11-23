package pl.smarthouse.fireplacemodule.service;

import static pl.smarthouse.fireplacemodule.properties.ThrottleProperties.THROTTLE_DUTY_CYCLE_0;
import static pl.smarthouse.fireplacemodule.properties.ThrottleProperties.THROTTLE_DUTY_CYCLE_100;

import org.springframework.stereotype.Service;

@Service
public class DutyCycleService {

  public int recalculateByGoalPosition(final int goalPosition) {
    final double dutyCyclePerPercent = (THROTTLE_DUTY_CYCLE_100 - THROTTLE_DUTY_CYCLE_0) / 100.00;

    return 0;
  }
}
