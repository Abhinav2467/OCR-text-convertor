package com.ocrconverter;

import java.io.IOException;
import java.util.Scanner;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;

public class TextConverterApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ImagePreprocessor preprocessor = new ImagePreprocessor();
        OCRProcessor ocrProcessor = new OCRProcessor();

        System.out.println("Handwritten Text to Digital Text Converter (Console)");
        System.out.print("Enter the path to the image file: ");
        String imagePath = scanner.nextLine();

        try {
            BufferedImage preprocessedImage = preprocessor.preprocessImage(imagePath);
            String extractedText = ocrProcessor.performOCR(preprocessedImage);

            System.out.println("\nExtracted Text:\n");
            System.out.println(extractedText);

        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        } catch (TesseractException e) {
            System.err.println("Error during OCR: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
