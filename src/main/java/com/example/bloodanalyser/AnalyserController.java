package com.example.bloodanalyser;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AnalyserController {
    @FXML private ImageView originalImageView;
    @FXML private ImageView tricolourImageView;
    @FXML private ImageView analysisImageView;
    @FXML private MenuItem openMenuItem;

    private Image originalImage;
    private WritableImage tricolourImage;
    private WritableImage analysisImage;

    private boolean showNumbering = false;
    private int redThreshold = 150;
    private int purpleThreshold = 180;
    private int minCellSize = 50;
    private int maxCellSize = 2000;

    public void initialize() {
        openMenuItem.setOnAction(e -> loadImage());
    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Blood Sample");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpeg", "*.jpg"));

        Stage stage = (Stage) originalImageView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null){
            originalImage = new Image(selectedFile.toURI().toString());
            originalImageView.setImage(originalImage);
            originalImageView.setFitWidth(originalImage.getWidth());
            originalImageView.setFitHeight(originalImage.getHeight());
        }
    }
}