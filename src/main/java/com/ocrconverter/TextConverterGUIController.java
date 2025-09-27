package com.ocrconverter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
public class TextConverterGUIController implements Initializable {
    @FXML
    private Button browseButton;
    @FXML
    private Button convertButton;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private Label imagePathLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private ProgressBar progressBar;
    private File selectedImageFile;
    private Tesseract tesseract;
    private static final double MAX_IMAGE_WIDTH = 600;
    private static final double MAX_IMAGE_HEIGHT = 400;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize Tesseract
        tesseract = new Tesseract();
        try {
            tesseract.setDatapath("tessdata");
        } catch (Exception e) {
            displayError("Error initializing Tesseract: " + e.getMessage());
            convertButton.setDisable(true);
        }

        // Populate the language ComboBox
        languageComboBox.getItems().addAll("eng", "hin", "jpn", "tam", "mal");
        languageComboBox.setValue("eng");
        convertButton.setDisable(true);
        progressBar.setVisible(false);
    }
    
    @FXML
    private void handleBrowseButtonAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.tif", "*.tiff")
        );

        selectedImageFile = fileChooser.showOpenDialog(((Button) event.getSource()).getScene().getWindow());

        if (selectedImageFile != null) {
            imagePathLabel.setText(selectedImageFile.getAbsolutePath());
            Image image = new Image(selectedImageFile.toURI().toString());
            double width = image.getWidth();
            double height = image.getHeight();

            if (width > MAX_IMAGE_WIDTH) {
                height = height * (MAX_IMAGE_WIDTH / width);
                width = MAX_IMAGE_WIDTH;
            }
            if (height > MAX_IMAGE_HEIGHT) {
                width = width * (MAX_IMAGE_HEIGHT / height);
                height = MAX_IMAGE_HEIGHT;
            }
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            convertButton.setDisable(false);
        } else {
            imagePathLabel.setText("No image selected");
            imageView.setImage(null);
            convertButton.setDisable(true);
        }
    }

    @FXML
    private void handleConvertButtonAction(ActionEvent event) {
        if (selectedImageFile == null) {
            displayError("Please select an image to convert.");
            return;
        }

        String selectedLanguage = languageComboBox.getValue();
        tesseract.setLanguage(selectedLanguage);
        convertButton.getStyleClass().add("processing");
        convertButton.setText("Processing...");
        convertButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(0);

        Task<String> ocrTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                try {
                    String result = tesseract.doOCR(selectedImageFile);
                    return result;
                } catch (TesseractException e) {
                    throw new Exception("Error during OCR: " + e.getMessage(), e);
                }
            }

            @Override
            protected void succeeded() {
                String ocrResult = getValue();
                outputTextArea.setText(ocrResult);
                onOCRComplete(true);
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                String errorMessage = "An unexpected error occurred.";
                if (e != null) {
                    errorMessage = e.getMessage();
                }
                displayError(errorMessage);
                onOCRComplete(false);
            }
        };

        new Thread(ocrTask).start();
    }

    private void displayError(String errorMessage) {
        outputTextArea.setText("Error: " + errorMessage);
        outputTextArea.getStyleClass().add("error");
        convertButton.getStyleClass().remove("processing");
        convertButton.getStyleClass().add("error");
        convertButton.setText("Error");
        progressBar.setVisible(false);
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> {
            outputTextArea.getStyleClass().remove("error");
            convertButton.getStyleClass().remove("error");
            convertButton.setText("Convert to Text");
            convertButton.setDisable(selectedImageFile == null);
        });
        pause.play();

    }

    private void onOCRComplete(boolean success) {
        progressBar.setVisible(false);
        if (success) {
            convertButton.getStyleClass().remove("processing");
            convertButton.getStyleClass().add("success");
            convertButton.setText("Conversion Complete");
        } else {
            convertButton.getStyleClass().remove("processing");
            convertButton.getStyleClass().add("error");
            convertButton.setText("Error");
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            convertButton.getStyleClass().remove("success");
            convertButton.getStyleClass().remove("error");
            convertButton.setText("Convert to Text");
            convertButton.setDisable(selectedImageFile == null);
        });
        pause.play();
    }
}

