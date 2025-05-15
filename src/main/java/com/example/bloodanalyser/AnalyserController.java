package com.example.bloodanalyser;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

public class AnalyserController {
    @FXML private ImageView originalImageView;
    @FXML private ImageView tricolourImageView;
    @FXML private ImageView analysisImageView;
    @FXML private MenuItem openMenuItem;
    @FXML private TabPane tabPane;

    @FXML private MenuItem convertMenuItem;
    @FXML private MenuItem analyzeMenuItem;
    @FXML private MenuItem toggleNumberingMenuItem;
    @FXML private MenuItem colourThresholdsMenuItem;
    @FXML private MenuItem cellSizeMenuItem;
    @FXML private MenuItem exitMenuItem;

    private Image originalImage;
    private WritableImage tricolourImage;
    private WritableImage analysisImage;

    private boolean showNumbering = false;
    private int redThreshold = 30;
    private int purpleThreshold = 40;
    private int minCellSize = 30;
    private int maxCellSize = 3000;

    private BloodCellAnalyser.AnalysisResult analysisResult;

    public void initialize() {
        openMenuItem.setOnAction(e -> loadImage());

        convertMenuItem.setOnAction(e -> convertToTricolour());
        analyzeMenuItem.setOnAction(e -> analyseBloodCells());
        toggleNumberingMenuItem.setOnAction(e -> toggleNumbering());
        colourThresholdsMenuItem.setOnAction(e -> showColourThresholdDialog());
        cellSizeMenuItem.setOnAction(e -> showCellSizeDialog());
        exitMenuItem.setOnAction(e -> exitApplication());

        updateToggleNumberingText();
    }

    private void updateToggleNumberingText() {
        toggleNumberingMenuItem.setText(showNumbering ? "Hide Numbering" : "Show Numbering");
    }

    private void exitApplication() {
        Stage stage = (Stage) originalImageView.getScene().getWindow();
        stage.close();
    }

    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Blood Sample Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) originalImageView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            originalImage = new Image(selectedFile.toURI().toString());
            originalImageView.setImage(originalImage);
            originalImageView.setFitWidth(originalImage.getWidth());
            originalImageView.setFitHeight(originalImage.getHeight());

            tricolourImage = null;
            analysisImage = null;
            tricolourImageView.setImage(null);
            analysisImageView.setImage(null);

            analysisResult = null;

            tabPane.getSelectionModel().select(0);

            stage.setTitle("Blood Cell Analyser - " + selectedFile.getName());
        }
    }

    private void convertToTricolour() {
        if (originalImage == null) {
            showAlert("No Image", "Please load an image first.");
            return;
        }

        ImageProcessor processor = new ImageProcessor(redThreshold, purpleThreshold);
        tricolourImage = processor.convertToTriColor(originalImage);

        tricolourImageView.setImage(tricolourImage);
        tricolourImageView.setFitWidth(tricolourImage.getWidth());
        tricolourImageView.setFitHeight(tricolourImage.getHeight());

        tabPane.getSelectionModel().select(1);
    }

    private void analyseBloodCells() {
        if (tricolourImage == null) {
            showAlert("No Tricolour Image", "Please convert to tricolour first.");
            return;
        }

        BloodCellAnalyser analyser = new BloodCellAnalyser(minCellSize, maxCellSize, showNumbering);
        analysisResult = analyser.analyseImage(tricolourImage);
        analysisImage = analyser.createAnalysisImage(originalImage, analysisResult.getCells());

        analysisImageView.setImage(analysisImage);
        analysisImageView.setFitWidth(analysisImage.getWidth());
        analysisImageView.setFitHeight(analysisImage.getHeight());

        tabPane.getSelectionModel().select(2);

        showAnalysisResults();
    }

    private void showAnalysisResults() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Analysis Results");
        alert.setHeaderText("Blood Cell Analysis Complete");

        String content = String.format(
                "Total Red Blood Cells: %d\n" +
                        "White Blood Cells: %d\n" +
                        "Total Blood Cells: %d",
                analysisResult.getRedCellCount(),
                analysisResult.getWhiteCellCount(),
                analysisResult.getTotalCellCount()
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void toggleNumbering() {
        showNumbering = !showNumbering;
        updateToggleNumberingText();

        if (analysisResult != null && tricolourImage != null) {
            analyseBloodCells();
        }
    }

    private void showColourThresholdDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Colour Thresholds");
        dialog.setHeaderText("Adjust colour thresholds for cell detection");

        Slider redSlider = new Slider(0, 100, redThreshold);
        redSlider.setShowTickLabels(true);
        redSlider.setShowTickMarks(true);
        redSlider.setMajorTickUnit(20);

        Slider purpleSlider = new Slider(0, 100, purpleThreshold);
        purpleSlider.setShowTickLabels(true);
        purpleSlider.setShowTickMarks(true);
        purpleSlider.setMajorTickUnit(20);

        Label redLabel = new Label("Red Threshold: " + redThreshold);
        Label purpleLabel = new Label("Purple Threshold: " + purpleThreshold);

        redSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            redThreshold = newVal.intValue();
            redLabel.setText("Red Threshold: " + redThreshold);
        });

        purpleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            purpleThreshold = newVal.intValue();
            purpleLabel.setText("Purple Threshold: " + purpleThreshold);
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                redLabel, redSlider,
                purpleLabel, purpleSlider
        );
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (originalImage != null) {
                convertToTricolour();
                if (tricolourImage != null) {
                    analyseBloodCells();
                }
            }
        }
    }

    private void showCellSizeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Cell Size Parameters");
        dialog.setHeaderText("Adjust minimum and maximum cell size");

        Slider minSlider = new Slider(10, 200, minCellSize);
        minSlider.setShowTickLabels(true);
        minSlider.setShowTickMarks(true);
        minSlider.setMajorTickUnit(50);

        Slider maxSlider = new Slider(500, 5000, maxCellSize);
        maxSlider.setShowTickLabels(true);
        maxSlider.setShowTickMarks(true);
        maxSlider.setMajorTickUnit(1000);

        Label minLabel = new Label("Min Cell Size: " + minCellSize);
        Label maxLabel = new Label("Max Cell Size: " + maxCellSize);

        minSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            minCellSize = newVal.intValue();
            minLabel.setText("Min Cell Size: " + minCellSize);
        });

        maxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxCellSize = newVal.intValue();
            maxLabel.setText("Max Cell Size: " + maxCellSize);
        });

        VBox content = new VBox(10);
        content.getChildren().addAll(
                minLabel, minSlider,
                maxLabel, maxSlider
        );
        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (tricolourImage != null) {
                analyseBloodCells();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}