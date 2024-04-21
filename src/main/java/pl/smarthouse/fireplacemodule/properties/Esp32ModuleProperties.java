package pl.smarthouse.fireplacemodule.properties;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Esp32ModuleProperties {

  // Module specific
  public static final String FIRMWARE = "20240421.15";
  public static final String VERSION = "20240421.15";
  public static final String MAC_ADDRESS = "3C:71:BF:4D:61:CC";
  public static final String MODULE_TYPE = "FIREPLACE";
}
