package com.ocrconverter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
public class ImagePreprocessor {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public BufferedImage preprocessImage(String imagePath) throws IOException {
        Mat image = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            throw new IOException("Could not read the image file: " + imagePath);
        }
        // 1. Grayscale Conversion
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        // 2. Thresholding (Binarization) - Using Otsu's method for automatic thresholding
        Mat binaryImage = new Mat();
        Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        // 3. Noise Removal - Using median blur
        Mat denoisedImage = new Mat();
        Imgproc.medianBlur(binaryImage, denoisedImage, 3); // Kernel size 3
        // Convert back to BufferedImage for Tesseract
        BufferedImage bufferedImage = matToBufferedImage(denoisedImage);
        return bufferedImage;
    }
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] data = new byte[mat.cols() * mat.rows() * mat.channels()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}