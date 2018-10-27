/*
 * Project 1
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.ImageIO;


public class readImage
{
  public colorCodeAndIntensity ci;
  int imageCount = 0;
  double[][] coOccurenceMatrix;
  int[][] histogramMatrix = new int[100][27]; //0-24 -> bin; 25th  - store image size; 26-sum of bin value for each img
  int[][] colorCodeMatrix = new int[100][65]; //0-63 bin; 64 - sum of bin value for each img


  /* Each image is retrieved from the file
   * Each retrieved image - image size(height*width) is calculated to compute Intensity and Color code method
   */
  public readImage()
  {
    ci = new colorCodeAndIntensity();
    BufferedImage image = null;
    while(imageCount < 100){
      try
      {
        //String filename = "images\\images\\"+(imageCount+1)+".jpg";
        image = ImageIO.read(getClass().getResource("images/" + (imageCount+1) + ".jpg"));
        getIntensity(image, image.getHeight(), image.getWidth());
        getColorCode(image, image.getHeight(), image.getWidth());
      }
     catch(IOException ex){
        System.out.println("Error reading the file "+ex);
     } finally {
        imageCount++;
      }
    }
    relevanceFeedback();
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
      for(int row = 0; row<height-1; row++){
        for(int col=0; col<width-1; col++){
          Color c = new Color(image.getRGB(col, row));
          int intensity = (int)(((0.299)*c.getRed()) + ((0.587)*c.getGreen()) + ((0.114)*c.getBlue()));
          coOccurenceMatrix[row][col] = intensity;
          populateHistogramMatrixforIntensity(intensity, height*width);
        }
      }
    populateBinSumValue();
  }

  public void populateHistogramMatrixforIntensity(int intensity, int imageSize){
    //0-24 -> bin; 25th column - store image size
    histogramMatrix[imageCount][25] = imageSize;
    if(intensity >=250){
      histogramMatrix[imageCount][24] += 1;
    }else {
      histogramMatrix[imageCount][intensity / 10] += 1;
    }
  }
  // for intensity
  public void populateBinSumValue(){

    // i = each image j= each bin
    for(int i=0; i<100; i++) {
      int sum=0;
      for (int j = 0; j < 25; j++) {
        sum += histogramMatrix[i][j];
      }
      histogramMatrix[i][26] = sum;
    }
  }
  //for color code
  public void calcBinSumValue() {

    for(int i=0; i<100; i++){
      int sum = 0;
      for(int j=0; j<64; j++){
        sum += colorCodeMatrix[i][j];
      }
      colorCodeMatrix[i][64] = sum;
    }
  }
  /**
   * compute colorCodeMatrix to find Manhattan distance between two images
   * @param image
   * @param height
   * @param width
   */
  public void getColorCode(BufferedImage image, int height, int width){
    for(int row=0; row<height; row++){
      for(int col=0; col<width; col++){
        Color c = new Color(image.getRGB(col, row));
        String r = Integer.toBinaryString(c.getRed());
        String b = Integer.toBinaryString(c.getBlue());
        String g = Integer.toBinaryString(c.getGreen());
        int colorCodedIntensityVal = Integer.parseInt(computeMSB2(r) + computeMSB2(b) + computeMSB2(g), 2);
        colorCodeMatrix[imageCount][colorCodedIntensityVal] +=1;
      }
    }
    calcBinSumValue();
}

  /**
   * Computes the 2 most significant bits from a binary string
   * @param str
   * @return 2 most significant bits from a binary string
   */
  private String computeMSB2(String str) {
      if (str.length() == 1) {
          return "0" + str;
      }
      return str.substring(0,2);
  }

  /**
   * This method writes the contents of the colorCode matrix to a file named colorCodes.txt
   */

  public void writeColorCode(){
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter("colorcode.txt"));
      for (int i = 0; i < colorCodeMatrix.length; i++) {
        for (int j = 0; j < colorCodeMatrix[i].length; j++) {
          bw.write(colorCodeMatrix[i][j] + ",");
        }
        bw.newLine();
      }
      bw.flush();
    } catch (IOException ex) {
      System.out.println("Exception: File could not be created!! "+ex);
    }
  }

  /**
   * This method writes the contents of the intensity matrix to a file called intensity.txt
   */
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
    } catch (IOException ex) {
      System.out.println("Exception: File could not be created!! "+ex);
    }
  }

  public void relevanceFeedback(){
    ci.calcFeatureMatrix(histogramMatrix, colorCodeMatrix);
    ci.calc_Avg_Std();
    ci.calcNormalisedFeatureMatrix();
    ci.updateWeight(new ArrayList<Integer>());
  }


  public static void main(String[] args)
  {
    new readImage();
  }
}
