package pl.smarthouse.fireplacemodule.model.dao;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FireplaceModuleParamsDao {
  private double workingTemperature;
  private double warningTemperature;
  private double alarmTemperature;
}
