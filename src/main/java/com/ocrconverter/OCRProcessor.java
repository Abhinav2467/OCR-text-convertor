package com.ocrconverter;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;
import java.io.File;

public class OCRProcessor {

    private Tesseract tesseract;

    public OCRProcessor() {
        tesseract = new Tesseract();
        // Set the path to the tessdata directory.
        // If 'tessdata' is in the project root, this should work:
        tesseract.setDatapath("tessdata");
        tesseract.setOcrEngineMode(1);
        tesseract.setPageSegMode(3);

        // If you placed it somewhere else, use the absolute path to the PARENT directory:
        // tesseract.setDataPath("C:\\Program Files\\Tesseract-OCR");
    }

    public String performOCR(BufferedImage image, String language) throws TesseractException {
        tesseract.setLanguage(language);
        return tesseract.doOCR(image);
    }

    // You can keep this for backward compatibility if needed,
    // it will default to the language set previously (if any) or English.
    public String performOCR(BufferedImage image) throws TesseractException {
        return tesseract.doOCR(image);
    }
}