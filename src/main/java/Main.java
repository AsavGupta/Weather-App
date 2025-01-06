import  javax.swing.*;

public class Main {
    public static int frameWidth = 400;
    public static int frameHeight = 600;
    public static void main(String[] args){

        JFrame frame = new JFrame("Gupta Weather");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameWidth,frameHeight);
        frame.setIconImage(WeatherScreen.imageResizer(System.getProperty("user.dir") + "\\Images\\Hail.png", 24,24).getImage());
        frame.setLocationRelativeTo(null);
        WeatherScreen weatherScreen = new WeatherScreen();
        frame.add(weatherScreen);
        frame.pack();

        frame.setVisible(true);
    }
}
