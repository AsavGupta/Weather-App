import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlIJTheme;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WeatherScreen extends JPanel {
    private static Panel weatherDisplayPanel;
    private static JTextField locationField;
    private static JButton fetchButton;
    private static Font concertOne;
    private static ArrayList<JLabel> labelArrayList = new ArrayList<>();
    private static LocalTime now;
    private static Color wordColor;
    WeatherScreen(){
        try {
            UIManager.setLookAndFeel(new FlatLightOwlIJTheme());

            concertOne = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("user.dir") + "\\Concert_One\\ConcertOne-Regular.ttf")).deriveFont(24f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(concertOne); // Register the font
            this.setFont(concertOne);
        } catch (Exception e){
            e.printStackTrace();
        }
        this.setBackground(new Color(78, 180, 210));
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setPreferredSize(new Dimension(Main.frameWidth,Main.frameHeight));

        locationField = new JTextField(15);
        locationField.setText("Enter City Name");
        locationField.setFont(concertOne);

        weatherDisplayPanel = new Panel();
        weatherDisplayPanel.setLayout(new FlowLayout(FlowLayout.CENTER, (int) (Main.frameWidth/1.5), Main.frameHeight/25));
        weatherDisplayPanel.setPreferredSize(new Dimension((int) (Main.frameWidth/1.2), (int) (Main.frameHeight/1.2)));

        JLabel locationLabel = new JLabel("");
        labelArrayList.add(locationLabel);
        weatherDisplayPanel.add(locationLabel);

        JLabel imageLabel = new JLabel();
        labelArrayList.add(imageLabel);
        weatherDisplayPanel.add(imageLabel);

        JLabel currentTemperatureLabel = new JLabel("");
        labelArrayList.add(currentTemperatureLabel);
        weatherDisplayPanel.add(currentTemperatureLabel);

        JLabel descriptionLabel = new JLabel("");
        labelArrayList.add(descriptionLabel);
        weatherDisplayPanel.add(descriptionLabel);

        JLabel temperatureRangeLabel = new JLabel("");
        labelArrayList.add(temperatureRangeLabel);
        weatherDisplayPanel.add(temperatureRangeLabel);

        JLabel temperatureFeelsLikeLabel = new JLabel("");
        labelArrayList.add(temperatureFeelsLikeLabel);
        weatherDisplayPanel.add(temperatureFeelsLikeLabel);

        fetchButton = new JButton();
        fetchButton.setIcon(imageResizer(System.getProperty("user.dir") + "\\Images\\Search.png", 20,20));
        fetchButton.addActionListener(e -> {
            String city = locationField.getText();
            for (int i = 0; i < city.length(); i++){
                String c = String.valueOf(city.charAt(i));
                if(c.equals(" ")){
                    city = city.replace(c, "%20");
                }
            }
            String[] weatherInfo = fetchWeatherData(city);
            if(weatherInfo[0] != "Error") {
                locationLabel.setText(locationField.getText());
                imageLabel.setIcon(weatherDescription(weatherInfo[0]));
                currentTemperatureLabel.setText(weatherInfo[1]+"°");
                descriptionLabel.setText("Condition: " + weatherInfo[0]);
                temperatureRangeLabel.setText("Temperature: " + weatherInfo[2] + "°" + " / " + weatherInfo[3] + "°");
                temperatureFeelsLikeLabel.setText("Feels Like: " + weatherInfo[4] + "°");
            }else{
                locationLabel.setText("Error: Please Try Another City");
            }
        }
        );

        this.add(locationField);
        this.add(fetchButton);
        this.add(weatherDisplayPanel);
        // Define the start and end colors
        Color dayColor = new Color(122, 189, 255);
        Color nightColor = new Color(0, 40, 90);
        Color dayWordColor = new Color(0, 0, 0);
        Color nightWordColor = new Color(210, 210, 210);

        // Create a timer to update the color
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    // Get the current time
                    now = LocalTime.now();

                    // Define time intervals
                    LocalTime morningStart = LocalTime.of(6, 0);
                    LocalTime nightStart = LocalTime.of(23, 0);

                    // Calculate the color based on the time
                    Color currentBgColor;
                    if (!now.isBefore(morningStart) && now.isBefore(nightStart)) {
                        // Daytime transition: 6 AM to 11 PM
                        currentBgColor = interpolateColor(dayColor, nightColor,
                                getFraction(morningStart, nightStart, now));
                        wordColor = interpolateColor(dayWordColor, nightWordColor,
                                getFraction(morningStart, nightStart, now));
                    } else {
                        // Nighttime transition: 11 PM to 6 AM
                        LocalTime nextMorningStart = LocalTime.of(6, 0);
                        if (now.isBefore(morningStart)) {
                            nextMorningStart = morningStart.minusHours(24); // Handle pre-6 AM transition
                        }
                        currentBgColor = interpolateColor(nightColor, dayColor,
                                getFraction(nightStart, nextMorningStart, now));
                        wordColor = interpolateColor(nightWordColor, dayWordColor,
                                getFraction(nightStart, nextMorningStart, now));
                    }

                    // Set the panel's background color
                    setBackground(currentBgColor);
                    for (JLabel label: labelArrayList) {
                        label.setFont(concertOne);
                        label.setForeground(wordColor);
                    }
                });
            }
        }, 0, 1000); // Update every second
    }

    // Helper method to interpolate between two colors
    private static Color interpolateColor(Color startColor, Color endColor, float fraction) {
        int red = (int) (startColor.getRed() + fraction * (endColor.getRed() - startColor.getRed()));
        int green = (int) (startColor.getGreen() + fraction * (endColor.getGreen() - startColor.getGreen()));
        int blue = (int) (startColor.getBlue() + fraction * (endColor.getBlue() - startColor.getBlue()));
        return new Color(red, green, blue);
    }

    // Helper method to calculate the fraction of time passed between two LocalTime values
    private static float getFraction(LocalTime start, LocalTime end, LocalTime current) {
        long totalSeconds = end.toSecondOfDay() - start.toSecondOfDay();
        if (totalSeconds < 0) {
            totalSeconds += 24 * 60 * 60; // Handle day rollover
        }
        long elapsedSeconds = current.toSecondOfDay() - start.toSecondOfDay();
        if (elapsedSeconds < 0) {
            elapsedSeconds += 24 * 60 * 60; // Handle day rollover
        }
        return (float) elapsedSeconds / totalSeconds;
    }

    private String[] fetchWeatherData(String city){
        try {
            String apiKey = "7ea609038dc1b8bbe6e9b12d6ddc1467";
            URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey);
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = "";
            String line;
            while ((line = reader.readLine()) != null){
                response += line;
            }
            reader.close();

            JSONObject jsonObject = (JSONObject) JSONValue.parse(response.toString());
            JSONObject mainObj = (JSONObject) jsonObject.get("main");

            double temperatureKelvin = (double)mainObj.get("temp");
            //Conversion
            double temperatureFahrenheit = (double) Math.round((temperatureKelvin - 273.15) * ((double) 9 / 5) + 32);
            double maxTemperatureFahrenheit = temperatureFahrenheit + 1;
            double minTemperatureFahrenheit = temperatureFahrenheit - 1;
            double feelsLikeTemperature = temperatureFahrenheit;
            try{
                double minTemperatureKelvin = (double)mainObj.get("temp_min");
                double maxTemperatureKelvin = (double)mainObj.get("temp_max");
                feelsLikeTemperature = (double)mainObj.get("feels_like");
                minTemperatureFahrenheit = (double) Math.round((minTemperatureKelvin - 273.15) * ((double) 9 / 5) + 32);
                maxTemperatureFahrenheit = (double) Math.round((maxTemperatureKelvin - 273.15) * ((double) 9 / 5) + 32);
                feelsLikeTemperature = (double) Math.round((feelsLikeTemperature - 273.15) * ((double) 9 / 5) + 32);
            } catch (Exception e){}
            //Weather Description
            JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
            JSONObject weather = (JSONObject) weatherArray.get(0);
            String description = (String) weather.get("main");

            return new String[]{description, String.valueOf(temperatureFahrenheit), String.valueOf(minTemperatureFahrenheit),
                    String.valueOf(maxTemperatureFahrenheit), String.valueOf(feelsLikeTemperature)};
        } catch (Exception e){
            return new String[]{"Error"};
        }
    }

    private ImageIcon weatherDescription(String description){
        final int imageSize = 110;
        if(Objects.equals(description, "Clear") || Objects.equals(description, "Sunny")){
            return imageResizer(System.getProperty("user.dir") + "\\Images\\Clear.png", imageSize, imageSize);
        }
        if(Objects.equals(description, "Fog")){
            return imageResizer(System.getProperty("user.dir") + "\\Images\\Foggy.png", imageSize, imageSize);
        }
        if(Objects.equals(description, "Hail")){
            return imageResizer(System.getProperty("user.dir") + "\\Images\\Hail.png", imageSize, imageSize);
        }
        if(Objects.equals(description, "Rainy") || Objects.equals(description, "Mist")){
            return imageResizer(System.getProperty("user.dir") + "\\Images\\Rainy.png", imageSize, imageSize);
        }
        if(Objects.equals(description, "Snowy")){
            return imageResizer(System.getProperty("user.dir") + "\\Images\\Snowy.png", imageSize, imageSize);
        }
        if(Objects.equals(description, "Thunder")){
            return imageResizer(System.getProperty("user.dir") + "\\Images\\Hail.png", imageSize, imageSize);
        }
        return imageResizer(System.getProperty("user.dir") + "\\Images\\Cloudy.png", imageSize, imageSize);
    }
    public static ImageIcon imageResizer(String imageUrl, int width, int height){
        ImageIcon icon = new ImageIcon(imageUrl);
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        icon = new ImageIcon(newimg);
        return icon;
    }
}
