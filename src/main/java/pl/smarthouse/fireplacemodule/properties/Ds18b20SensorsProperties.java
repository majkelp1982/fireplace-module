package pl.smarthouse.fireplacemodule.properties;

public class Ds18b20SensorsProperties {
  // Exchanger
  public static final String THERMOMETERS = "thermometers";
  public static final int THERMOMETERS_DS18B20_PIN = 33;
  public static final String THERMOMETER_WATER_IN = "40-255-157-23-3-23-4-170"; // DS18b20
  public static final float THERMOMETERS_WATER_IN_GRADIENT = 1f;
  public static final float THERMOMETERS_WATER_IN_INTERCEPT = 1f;
  public static final String THERMOMETERS_WATER_OUT = "40-255-150-10-3-23-4-127"; // DS18b20
  public static final float THERMOMETERS_WATER_OUT_GRADIENT = 1f;
  public static final float THERMOMETERS_WATER_OUT_INTERCEPT = 1.25f;
  public static final String THERMOMETERS_CHIMNEY = "40-255-145-33-2-23-5-63"; // DS18b20
  public static final float THERMOMETERS_CHIMNEY_GRADIENT = 1f;
  public static final float THERMOMETERS_CHIMNEY_INTERCEPT = 0.75f;
}
