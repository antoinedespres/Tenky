package fr.dutapp.tenky;

public class DailyWeather {
    private String dayName;
    private int temperature;
    private String minMaxTemp;
    private String weatherIcon;

    public DailyWeather(String dayName, int temperature, String minMaxTemp, String weatherIcon) {
        this.dayName = dayName;
        this.temperature = temperature;
        this.minMaxTemp = minMaxTemp;
        this.weatherIcon = weatherIcon;
    }

    public String getDayName() {
        return dayName;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getMinMaxTemp() {
        return minMaxTemp;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }
}
