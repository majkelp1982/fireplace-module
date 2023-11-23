package pl.smarthouse.fireplacemodule.model.dao;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import pl.smarthouse.sharedobjects.dao.ModuleDao;
import pl.smarthouse.sharedobjects.dto.fireplace.core.Throttle;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.Mode;
import pl.smarthouse.sharedobjects.dto.fireplace.enums.State;
import pl.smarthouse.smartmodule.model.actors.type.ds18b20.Ds18b20Result;

@Data
@SuperBuilder
public class FireplaceModuleDao extends ModuleDao {
  private Mode mode;
  private State state;
  private Ds18b20Result waterIn;
  private Ds18b20Result waterOut;
  private Ds18b20Result chimney;
  private State pump;
  private Throttle throttle;
}
