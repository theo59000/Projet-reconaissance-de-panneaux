//import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
//import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
//import org.opencv.highgui.*;

import java.awt.image.BufferedImage;

public class tools {
    public static Mat ReadPicture(String fichier){
        File f = new File(fichier);
        if (!f.exists()) {
            System.err.println("Erreur : le fichier n'existe pas : " + f.getAbsolutePath());
            return new Mat();
        }
        Mat m = Highgui.imread(f.getAbsolutePath());      //Highgui.imread pour opencv 2.*
        return m;
    }
   

    public static void PrintTestBGR(Mat mat){
        for(int i = 0 ; i < mat.dims() ; i++) {
            for(int j=0; j<mat.dims() ; j++){
                double [] pixelColor = mat.get(i,j);
                if (pixelColor[0]==255 && pixelColor[1]==255 && pixelColor[2]==255){
                    System.out.print(" ");
                }
                else {System.out.print("+");}
        }
        System.out.println();
    }
    }

    public static void ImShow(String title, Mat img){
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".png", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try{
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.setTitle(title);
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static Mat BgrToHsv(Mat mat){
        Mat mat_hsv = Mat.zeros(mat.size(), mat.type());
        Imgproc.cvtColor(mat,mat_hsv,Imgproc.COLOR_BGR2HSV);
        return mat_hsv;
    }
    public static Mat HsvToBgr(Mat mat){
        Mat mat_bgr = Mat.zeros(mat.size(), mat.type());
        Imgproc.cvtColor(mat,mat_bgr,Imgproc.COLOR_HSV2BGR);
        return mat_bgr;
    }

    public static void GrayLevels(Mat mat){
        Mat mat_hsv = tools.BgrToHsv(mat);
        tools.ImShow("ImageSansModif", mat);
		tools.ImShow("HSV", mat_hsv);
        Vector<Mat> channels = new Vector<Mat>();
		Core.split(mat_hsv,channels);
        for(int i =0; i < channels.size(); i++){
            tools.ImShow(Integer.toString(i),channels.get(i));}
    }
    public static void HSV_Levels(Mat mat){
        Mat output = Mat.zeros(mat.size(),mat.type());
        Imgproc.cvtColor(mat,output, Imgproc.COLOR_HSV2BGR);
        Vector<Mat> channels = new Vector<Mat>();
        Core.split(output,channels);
        double hsv_values[][] = {{1,255,255},{179,1,255},{179,0,1}};
        for(int i =0 ; i < 3;i++){
            Mat chans[] = new Mat[3];
            for(int j =0; j<3;j++){
                Mat empty = Mat.ones(mat.size(),CvType.CV_8UC1);
                Mat comp = Mat.ones(mat.size(),CvType.CV_8UC1);
                Scalar v = new Scalar(hsv_values[i][j]);
                Core.multiply(empty, v, comp);
                chans[j]=comp;
            }
            chans[i] = channels.get(i);
            Mat dst = Mat.zeros(output.size(),output.type());
            Mat res = Mat.ones(dst.size(),dst.type());
            Core.merge(Arrays.asList(chans),dst);
            Imgproc.cvtColor(dst,res, Imgproc.COLOR_HSV2BGR);
            ImShow(Integer.toString(i),res);
        }
    }
    public static Mat SetThreshold(int inf,int sup,Mat mat){
        Mat hsv_image = BgrToHsv(mat);
        Mat threshold_img = new Mat();
        Core.inRange(hsv_image, new Scalar(inf,10,10), new Scalar(sup,255,255), threshold_img);
        return threshold_img;
    }
    public static Mat DetecterCercles(Mat mat){
        Mat threshold_img = new Mat();
		Mat threshold_img1 = tools.SetThreshold(0,10,mat);
		Mat threshold_img2 = tools.SetThreshold(160,179, mat);
		Core.bitwise_or(threshold_img1, threshold_img2, threshold_img);
		Imgproc.GaussianBlur(threshold_img, threshold_img, new Size(9,9),2,2);//masque appliqué à l'image
        return threshold_img;
    }
    public static List<MatOfPoint> FoundBorder(Mat mat){
        Mat hsv_image = Mat.zeros(mat.size(),mat.type());
        Imgproc.cvtColor(mat, hsv_image, Imgproc.COLOR_BGR2HSV);
        //tools.ImShow("HSV", hsv_image);
        Mat threshold_img = tools.DetecterCercles(mat);
        //tools.ImShow("Seuillage", threshold_img);
        int thresh = 100;
        Mat canny_output = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();
        Imgproc.Canny(threshold_img, canny_output, thresh, thresh*2);
        Imgproc.findContours(canny_output, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(canny_output.size(), CvType.CV_8UC3);
        Random rand = new Random();
        for (int i =0; i<contours.size(); i++){
            Scalar color = new Scalar( rand.nextInt(255 - 0 + 1),rand.nextInt(255 - 0 +1),
            rand.nextInt(255 - 0 +1));
            Imgproc.drawContours(drawing,contours, i, color,1,8,hierarchy,0, new Point());
        }
        tools.ImShow("Contours", drawing);
        return contours;
        

    }

    public static void DetecterFormes(Mat m){       //pas utilisée
        Mat hsv_image = Mat.zeros(m.size(),m.type());
        Imgproc.cvtColor(m, hsv_image, Imgproc.COLOR_BGR2HSV);
        tools.ImShow("HSV", hsv_image);
        Mat threshold_img = tools.DetecterCercles(hsv_image);
        tools.ImShow("Seuillage", threshold_img);
        List<MatOfPoint> contours = tools.FoundBorder(threshold_img);
    }
    
    public static void RedBall(List<MatOfPoint> contours, Mat mat){ //entoure les formes circulaires rouges
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        float[] radius = new float[1];
        Point center = new Point();
        for(int c = 0 ; c < contours.size(); c++){
            MatOfPoint contour = contours.get(c);
            double contourArea = Imgproc.contourArea(contour);
            matOfPoint2f.fromList(contour.toList());
            Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);
            if ((contourArea/(Math.PI*radius[0]*radius[0])) >= 0.8){        //donne la tolérance de déformation du cercle max
                Core.circle(mat,center, (int) radius[0], new Scalar(0,255,0),2);
                Rect rect = Imgproc.boundingRect(contour);
                Core.rectangle(mat, new Point(rect.x,rect.y),
                    new Point(rect.x+rect.width , rect.y+rect.height),
                    new Scalar(0,255,0), 2);
                Mat tmp = mat.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
                Mat ball = Mat.zeros(tmp.size(),tmp.type());
                tmp.copyTo(ball);
                tools.ImShow("Ball", ball); //renvoie dans des fenêtres individuelles les cercles
            }
            
        }
        //tools.ImShow("Détection des cercles",mat);
    }
}
