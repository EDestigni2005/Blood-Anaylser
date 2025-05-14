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

                double luminance = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
                double purpleness = (color.getRed() + color.getBlue() - color.getGreen());
                double redness = (color.getRed() - (color.getBlue() + color.getGreen())/2);

                Color newColor;
                if (purpleness > purpleThreshold/255.0 && luminance < 0.7){
                    newColor = Color.PURPLE;
                } else if (redness > redThreshold/255.0 && luminance < 0.9){
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