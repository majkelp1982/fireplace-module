package pl.smarthouse.fireplacemodule.properties;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Esp32ModuleProperties {

  // Module specific
  public static final String FIRMWARE = "20231116.19";
  public static final String VERSION = "20231116.22";
  public static final String MAC_ADDRESS = "3C:71:";
  public static final String MODULE_TYPE = "FIREPLACE";
}
