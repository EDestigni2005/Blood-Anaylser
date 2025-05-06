package com.example.bloodanalyser;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class AnalyserController {
    @FXML private Label welcomeText;
    @FXML private ImageView imageView;
    @FXML private MenuItem openMenuItem, convertMenuItem, thresholdMenuItem;

    public void initialize() {

    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpeg", "*.jpg"));
    }
}