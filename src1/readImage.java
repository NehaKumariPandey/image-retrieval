/*
 * Project 1
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageFilter;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;


public class readImage
{
  int imageCount = 0;
  double coOccurenceMatrix[][];
  int[][] histogramMatrix = new int[100][26]; //100-images; 25-bins; 26th interval - keep image size
  double[] intensityBins = new double [26];
  double intensityMatrix [][] = new double[100][26];
  double colorCodeBins [] = new double [64];
  int colorCodeMatrix [][] = new int[100][64];

  /*Each image is retrieved from the file.  The height and width are found for the image and the getIntensity and
   * getColorCode methods are called.
  */
  public readImage()
  {
    BufferedImage image = null;
    while(imageCount < 100){
      try
      {
        String filename = "images\\images\\"+(imageCount+1)+".jpg";
        image = ImageIO.read(new File(filename));
        getIntensity(image, image.getHeight(), image.getWidth());
        getColorCode(image, image.getHeight(), image.getWidth());
        //writeIntensity();
      }
     catch(IOException ex){
        System.out.println("Error reading the file "+ex);
     } finally {
        imageCount++;
      }

    }

    writeIntensity();
    writeColorCode();
    
  }

  /**
   * computing coOccurence matrix & computing intensity
   * @param image
   * @param height
   * @param width
   */
  public void getIntensity(BufferedImage image, int height, int width){
    coOccurenceMatrix = new double[height][width];
    //System.out.println("Height of the image: "+height + " width "+width);
      for(int row = 0; row<height-1; row++){
        for(int col=0; col<width-1; col++){
          Color c = new Color(image.getRGB(col, row));
          int intensity = (int)(((0.299)*c.getRed()) + ((0.587)*c.getGreen()) + ((0.114)*c.getBlue()));
          coOccurenceMatrix[row][col] = intensity;
          //populate bin Matrix
         populateBinMatrix(intensity, height*width);
        }
      }

  }



  public void populateBinMatrix(int intensity, int imageSize){
    //25th column to store img size; 0-24 - bin
    histogramMatrix[imageCount][25] = imageSize;
    if(intensity >=250){
      histogramMatrix[imageCount][24] += 1;
    }else {
      histogramMatrix[imageCount][intensity / 10] += 1;
    }
  }


  
  //color code method
  public void getColorCode(BufferedImage image, int height, int width){
    for(int row=0; row<height; row++){
      for(int col=0; col<width; col++){
        Color c = new Color(image.getRGB(col, row));
        String r = Integer.toBinaryString(c.getRed());
        //System.out.println("red "+r);
        String b = Integer.toBinaryString(c.getBlue());
        String g = Integer.toBinaryString(c.getGreen());
        int colorCodedIntensityVal = Integer.parseInt(computeMSB2(r) + computeMSB2(b) + computeMSB2(g), 2);
        colorCodeMatrix[imageCount][colorCodedIntensityVal] +=1;
      }
    }
    System.out.println("imageCount: " + imageCount);
  }

  /**
   * Computes the 2 most significant bits from a binary string
   * @param str
   * @return
   */
  private String computeMSB2(String str) {
      if (str.length() == 1) {
          return "0" + str;
      }
      return str.substring(0,2);
  }
  
  
  ///////////////////////////////////////////////
  //add other functions you think are necessary//
  ///////////////////////////////////////////////
  
  //This method writes the contents of the colorCode matrix to a file named colorCodes.txt.
  public void writeColorCode(){
    /////////////////////
    ///your code///
    /////////////////
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter("colorcode.txt"));
      for (int i = 0; i < colorCodeMatrix.length; i++) {
        for (int j = 0; j < colorCodeMatrix[i].length; j++) {
          bw.write(colorCodeMatrix[i][j] + ",");
        }
        bw.newLine();
      }
      bw.flush();
    } catch (IOException e) {
      System.out.println("Exception: File could not be created!!");
    }

  }
  
  //This method writes the contents of the intensity matrix to a file called intensity.txt
  public void writeIntensity(){
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter("intensity.txt"));
      for (int i = 0; i < histogramMatrix.length; i++) {
        for (int j = 0; j < histogramMatrix[i].length; j++) {
          bw.write(histogramMatrix[i][j] + ",");
        }
        bw.newLine();
      }
      bw.flush();
    } catch (IOException e) {
      System.out.println("Exception: File could not be created!!");
    }
  }
  
  public static void main(String[] args)
  {

    new readImage();
  }

}
