import java.util.List;
//import java.util.Vector;

import org.opencv.core.Core;
//import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
//import org.opencv.core.Size;
//import org.opencv.imgproc.Imgproc;

public class Main {
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.out.println("OpenCV Version: " + Core.VERSION);
		Mat mat = tools.ReadPicture("Images_partie_OpenCV//Billard_Balls.jpg");
		//Mat mat = Mat.eye(3, 3,CvType.CV_8UC1);
		Mat mat_hsv = tools.BgrToHsv(mat);
		double [] pix_color = mat_hsv.get(0,0);
		System.out.println(pix_color[0] +" "+ pix_color[1] +" "+ pix_color[2]);
		// tools.GrayLevels(mat);
		// System.out.println("mat = "+mat.dump());
		//tools.HSV_Levels(mat);
		//Mat threshold_img = tools.DetecterCercles(mat);
		// tools.ImShow("Cercles Rouges", threshold_img);
		List<MatOfPoint> contours = tools.FoundBorder(mat);
		tools.RedBall(contours, mat);
	}
}
