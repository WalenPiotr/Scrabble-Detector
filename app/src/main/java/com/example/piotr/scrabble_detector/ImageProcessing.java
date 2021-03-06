package com.example.piotr.scrabble_detector;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ImageProcessing {


    static Bitmap warp(Bitmap bitmap) {
        Mat sourceMat = createMat(bitmap);
        Mat outputMat = preprocessMat(sourceMat);
        List<Point> corners = CornerFinder.findCorners(outputMat);
        Mat warpedMat = warpMat(corners, sourceMat);
        return createBitmap(warpedMat);
    }


    static List<Bitmap> slice(Bitmap bitmap, int output_size) {
        Mat outputMat = createMat(bitmap);
        Size size = new Size(output_size, output_size);
        List<Mat> slices = ImageProcessing.sliceMat(outputMat, size);
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < slices.size(); i++) {
            bitmaps.add(createBitmap(slices.get(i)));
        }

        return bitmaps;
    }

    static Mat createMat(Bitmap bitmap) {
        Mat imageMat = new Mat();
        Utils.bitmapToMat(bitmap, imageMat);
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGRA2BGR);

        return imageMat;
    }

    static Bitmap createBitmap(Mat imageMat) {
        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(),
                Bitmap.Config.RGB_565);
        Utils.matToBitmap(imageMat, outputBitmap);
        return outputBitmap;
    }


    static Mat preprocessMat(Mat sourceMat) {
        Log.i("OpenCV", "Started bitmap processing");

        Mat imageMat = sourceMat.clone();
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(imageMat, imageMat, new Size(7, 7));
        Imgproc.Canny(imageMat, imageMat, 10.0, 100.0);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(5, 5));
        Imgproc.dilate(imageMat, imageMat, element);

        return imageMat;
    }


    static Mat warpMat(List<Point> sortedPoints, Mat sourceImageMat) {
        MatOfPoint2f src = new MatOfPoint2f();
        src.fromList(sortedPoints);
        Log.i("OpenCV", "warping... source points = " + src.toString());

        double size = 300;
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0, 0),
                new Point(size, 0),
                new Point(size, size),
                new Point(0, size)
        );

        Log.i("OpenCV", "warping... destination points = " + dst.toString());
        Mat M = Imgproc.getPerspectiveTransform(src, dst);
        Mat outputMat = new Mat();
        Imgproc.warpPerspective(sourceImageMat, outputMat, M, new Size(size, size));
        Log.i("OpenCV", "Image has been WarpedActivity");

        return outputMat;
    }

    static ArrayList<Mat> sliceMat(Mat image, Size size) {
        Log.i("OpenCV", "image " + image.toString());
        ArrayList<Mat> slices = new ArrayList<>();
        int width = image.width();
        int height = image.height();
        int slice_width = width / 15;
        int slice_height = height / 15;

        Log.i("OpenCV", "slice_width= " + Integer.toString(slice_width) + " slice_height= " + Integer.toString(slice_height));
        if (width > 0 && height > 0) {
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    int x = i * slice_width;
                    int y = j * slice_height;
                    //Log.i("OpenCV", "x = " + Integer.toString(x) + " y = "+Integer.toString(y));
                    Mat slice = image.submat(x, x + slice_width, y, y + slice_width);

                    //Log.i("OpenCV", "slice "+slice.toString());
                    Mat outSlice = new Mat();
                    Imgproc.resize(slice, outSlice, size);
                    slices.add(outSlice);
                }
            }
        }
        return slices;
    }
}
