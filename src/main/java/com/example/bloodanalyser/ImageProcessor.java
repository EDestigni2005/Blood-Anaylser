package com.example.bloodanalyser;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageProcessor {
    private final int redThreshold;
    private final int purpleThreshold;

    public ImageProcessor(int redThreshold, int purpleThreshold){
        this.redThreshold = redThreshold;
        this.purpleThreshold = purpleThreshold;
    }

    public WritableImage convertToTriColor(Image originalImage){
        int width = (int) originalImage.getWidth();
        int height = (int) originalImage.getHeight();

        WritableImage tricolourImage = new WritableImage(width, height);
        PixelReader pixelReader = originalImage.getPixelReader();
        PixelWriter pixelWriter = tricolourImage.getPixelWriter();

        for (int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                Color color = pixelReader.getColor(x, y);

                double red = color.getRed() * 255;
                double green = color.getGreen() * 255;
                double blue = color.getBlue() * 255;

                double luminance = 0.299 * red + 0.587 * green + 0.114 * blue;
                double redness = red - (green + blue) / 2;
                double purpleness = (red + blue) - green * 1.5;

                Color newColor;
                if (purpleness > purpleThreshold && luminance < 200) {
                    newColor = Color.PURPLE;
                } else if (redness > redThreshold && luminance > 100 && luminance < 220) {
                    newColor = Color.RED;
                } else {
                    newColor = Color.WHITE;
                }

                pixelWriter.setColor(x, y, newColor);
            }
        }
        return tricolourImage;
    }
}