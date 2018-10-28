/* Project 1*/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.io.*;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.*;

public class CBIR extends JFrame{
    
    private JLabel photographLabel = new JLabel();  //container to hold a large 
    private JButton [] button; //creates an array of JButtons
    private int [] buttonOrder = new int [101]; //creates an array to keep up with the image order
    private double [] imageSize = new double[101]; //keeps up with the image sizes
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout5;
    private GridLayout gridLayout6;
    private JPanel panelRight;
    //private JPanel panelBottom2;
    private JPanel panelLeft;
    private JPanel buttonPanel;
    private Double [][] intensityMatrix = new Double [100][26];
    private Double [][] colorCodeMatrix = new Double [100][64];
    private TreeMap <Double , Integer> map;
    int picNo = 0;
    int imageCount = 1; //keeps up with the number of images displayed since the first page.

    /**
     * Create the text files by reading the input images and create the CBIR UI
     * @param args
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new readImage();    // create the intensity and the color coded file
                CBIR app = new CBIR();
                app.setVisible(true);
            }
        });
    }

    /**
     * Constructor -- handles the entire processing logic
     */
    public CBIR() {
      //The following lines set up the interface including the layout of the buttons and JPanels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("CBIR Demo: Please Select an Image");

        panelRight = new JPanel();
        //panelBottom2 = new JPanel();
        panelLeft = new JPanel();
        buttonPanel = new JPanel();
        gridLayout1 = new GridLayout(4, 5, 30, 30);
        gridLayout2 = new GridLayout(2, 1, 50, 50);
        gridLayout5 = new GridLayout(4, 4, 50, 50);
        gridLayout6 = new GridLayout(1, 2, 10, 50);

        setLayout(gridLayout6);
        panelRight.setBackground(Color.BLACK);
        panelLeft.setBackground(Color.BLACK);
        //panelBottom2.setBackground(Color.BLACK);
        panelRight.setLayout(gridLayout1);

        //panelBottom2.setLayout(gridLayout1);
        panelLeft.setLayout(gridLayout2);
        add(panelLeft);
        add(panelRight);

        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(50, 2, 2, 2));
        buttonPanel.setLayout(gridLayout5);
        panelLeft.add(photographLabel);


        panelLeft.add(buttonPanel);
        JButton previousPage = new JButton("Previous");
        JButton nextPage = new JButton("Next");
        JButton intensity = new JButton("Intensity");
        JButton colorCode = new JButton("Color Code");
        JButton intensityColor = new JButton("Intensity + Color Code");
        JCheckBox relevance = new JCheckBox("Relevance");

        previousPage.setFont(new Font("Tahoma", Font.BOLD, 30));
        nextPage.setFont(new Font("Tahoma", Font.BOLD, 30));
        intensity.setFont(new Font("Tahoma", Font.BOLD, 30));
        colorCode.setFont(new Font("Tahoma", Font.BOLD, 30));
        intensityColor.setFont(new Font("Tahoma", Font.BOLD, 30));
        relevance.setFont( new Font("Tahoma", Font.BOLD, 30));


        relevance.setHorizontalTextPosition(SwingConstants.LEADING);

       // buttonPanel.add(nextPage).createImage(2,3);
        buttonPanel.add(previousPage);
        buttonPanel.add(nextPage);
        buttonPanel.add(intensity);
        buttonPanel.add(colorCode);
        buttonPanel.add(relevance);
        buttonPanel.add(intensityColor);
        nextPage.addActionListener(new nextPageHandler());
        previousPage.addActionListener(new previousPageHandler());
        intensity.addActionListener(new intensityHandler());
        colorCode.addActionListener(new colorCodeHandler());
        relevance.addActionListener(new relevanceHandler());

        // this centers the frame on the screen
        setLocationRelativeTo(null);

        button = new JButton[101];
        /*This for loop goes through the images in the database and stores them as icons and adds
         * the images to JButtons and then to the JButton array
        */
        //InputStream in = getClass().getResourceAsStream("/file.txt");
        //BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        for (int i = 1; i < 101; i++) {
                ImageIcon icon;

                icon = new ImageIcon(getClass().getResource("images/" + i + ".jpg"));

                     if(icon != null){
                    button[i] = new JButton(icon);
                    //panelRight.add(button[i]);
                    button[i].addActionListener(new IconButtonHandler(i, icon));
                    buttonOrder[i] = i;
                         JCheckBox rel_button = new JCheckBox("Relevant");
                         ComponentOrientation co = ComponentOrientation.LEFT_TO_RIGHT;
                    button[i].add(rel_button).setComponentOrientation(co);

                    imageSize[i] = icon.getIconHeight() * icon.getIconWidth();
                }
        }

        readIntensityFile();    // reads the generatedIntensity.txt file
        readColorCodeFile();     // reads the colorcode.txt file
        displayFirstPage();
    }

    /**
     * Helper method to compute manhattan distance for intensity
     * @param i
     * @param j
     * @return
     */
    public double computeManhattanDistForIntensity(int i, int j){
        double dist = 0;
        for(int k=0; k<25; k++){
            dist += Math.abs((intensityMatrix[i][k]/intensityMatrix[i][25])  - (intensityMatrix[j][k]/intensityMatrix[j][25]));
        }
        return dist;
    }

    /**
     * Helper method to compute manhattan distance for color code
     * @param i
     * @param j
     * @return
     */
    public double computeManhattanDistForColourCode(int i, int j){
        double dist = 0;
        for(int k=0; k<64; k++){
            dist += Math.abs((colorCodeMatrix[i][k]/imageSize[i+1])  - (colorCodeMatrix[j][k]/imageSize[j+1]));
        }
        return dist;
    }
    
    /*This method opens the intensity text file containing the intensity matrix with the histogram bin values for each image.
     * The contents of the matrix are processed and stored in a two dimensional array called intensityMatrix.
    */
    public void readIntensityFile(){
      Scanner read;
         try {
             read = new Scanner(new File("./generatedIntensity.txt"));
             int lineCount = 0;
             while (read.hasNextLine()) {
                 String[] currentLine = read.nextLine().trim().split(",");
                 for (int i = 0; i < currentLine.length-1; i++) {
                     intensityMatrix[lineCount][i] = Double.parseDouble(currentLine[i]);
                 }
                 lineCount++;
             }
         }
         catch(FileNotFoundException ex){
           System.out.println("The file generatedIntensity.txt does not exist "+ex);
         }
      
    }
    
    /*This method opens the color code text file containing the color code matrix with the histogram bin values for each image.
     * The contents of the matrix are processed and stored in a two dimensional array called colorCodeMatrix.
    */
    private void readColorCodeFile(){
      Scanner read;
         try{
           read =new Scanner(new File ("./generatedColorCode.txt"));
             int lineCount = 0;
             while (read.hasNextLine()) {
                 String[] currentLine = read.nextLine().trim().split(",");
                 for (int i = 0; i < currentLine.length-1; i++) {
                     colorCodeMatrix[lineCount][i] = Double.parseDouble(currentLine[i]);
                 }
                 lineCount++;
             }
         }
         catch(FileNotFoundException EE){
           System.out.println("The file colorcode.txt does not exist " + EE);
         }
    }
    
    /*This method displays the first twenty images in the panelBottom.  The for loop starts at number one and gets the image
     * number stored in the buttonOrder array and assigns the value to imageButNo.  The button associated with the image is 
     * then added to panelRight.  The for loop continues this process until twenty images are displayed in the panelRight
    */
    private void displayFirstPage(){
      int imageButNo = 0;
      panelRight.removeAll();
      for(int i = 1; i < 21; i++){
        imageButNo = buttonOrder[i];
        panelRight.add(button[imageButNo]);
        imageCount ++;
      }
      panelRight.revalidate();
      panelRight.repaint();

    }
    
    /*This class implements an ActionListener for each iconButton.  When an icon button is clicked, the image on the 
     * the button is added to the photographLabel and the picNo is set to the image number selected and being displayed.
    */ 
    private class IconButtonHandler implements ActionListener{
      int pNo = 0;
      ImageIcon iconUsed;
      
      IconButtonHandler(int i, ImageIcon j){
        pNo = i;
        iconUsed = j;  //sets the icon to the one used in the button
      }
      
      public void actionPerformed( ActionEvent e){
        photographLabel.setIcon(iconUsed);
        picNo = pNo;
      }
      
    }
    
    /*This class implements an ActionListener for the nextPageButton.  The last image number to be displayed is set to the 
     * current image count plus 20.  If the endImage number equals 101, then the next page button does not display any new 
     * images because there are only 100 images to be displayed.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
    */
    private class nextPageHandler implements ActionListener{

      public void actionPerformed( ActionEvent e){
          int imageButNo = 0;
          int endImage = imageCount + 20;
          if(endImage <= 101){
            panelRight.removeAll();
            for (int i = imageCount; i < endImage; i++) {
                    imageButNo = buttonOrder[i];
                    panelRight.add(button[imageButNo]);
                    imageCount++;
            }

            panelRight.revalidate();
            panelRight.repaint();
          }
      }
      
    }
    
    /*This class implements an ActionListener for the previousPageButton.  The last image number to be displayed is set to the 
     * current image count minus 40.  If the endImage number is less than 1, then the previous page button does not display any new 
     * images because the starting image is 1.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
    */
    private class previousPageHandler implements ActionListener{

      public void actionPerformed( ActionEvent e){
          int imageButNo = 0;
          int startImage = imageCount - 40;
          int endImage = imageCount - 20;
          if(startImage >= 1){
            panelRight.removeAll();
            /*The for loop goes through the buttonOrder array starting with the startImage value
             * and retrieves the image at that place and then adds the button to the panelRight.
            */
            JPanel relevPanel = new JPanel();
            //GridLayout relevGridLayout = new GridLayout(2,1,0, 0);
            //relevPanel.setLayout(relevGridLayout);
            for (int i = startImage; i < endImage; i++) {
                    imageButNo = buttonOrder[i];
                    //panelRight.add(button[imageButNo]);
                   JCheckBox rel_button = new JCheckBox("Relevant");
                   rel_button.setMnemonic(KeyEvent.VK_B);
                   rel_button.setBackground(Color.BLACK);
                rel_button.setForeground(Color.WHITE);
                //rel_button.setVerticalTextPosition(SwingConstants.BOTTOM);
                   rel_button.addActionListener(new relevanceHandler());
                    //rel_button.setBounds(1,2,1,1);
                    //panelRight.add(rel_button);

                ///
                relevPanel.add(button[imageButNo]);


                panelRight.add(relevPanel);
                    imageCount--;
          
            }
  
            panelRight.revalidate();
            panelRight.repaint();
          }
      }
      
    }
    
    
    /*This class implements an ActionListener when the user selects the intensityHandler button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one.
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
     * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */
    private class intensityHandler implements ActionListener{

      public void actionPerformed( ActionEvent e){
          if(picNo <= 0 ){
              return;
          }
          double [] distance = new double [101];
          map = new TreeMap<Double, Integer>();
          double d = 0;
          int compareImage = 0;
          int pic = (picNo - 1);
          int picIntensity = 0;
         // double picSize = imageSize[pic];  //101

          for(int i=0; i<imageSize.length-1; i++){
              //distance[i] = computeManhattanDistForIntensity(pic, i);
              map.put(computeManhattanDistForIntensity(pic, i), i+1);
          }
          rankImages();
	  }
    }

    private void rankImages() {
        // set the value of buttonOrder with proximity to the selecye
        int i=1;
        for(Map.Entry<Double, Integer> key : map.entrySet()){
            buttonOrder[i++]= key.getValue();
        }
        //repaint the display page
        imageCount=1;
        displayFirstPage();
    }
    
    /*This class implements an ActionListener when the user selects the colorCode button.  The image number that the
     * user would like to find similar images for is stored in the variable pic.  pic takes the image number associated with
     * the image selected and subtracts one to account for the fact that the intensityMatrix starts with zero and not one. 
     * The size of the image is retrieved from the imageSize array.  The selected image's intensity bin values are 
     * compared to all the other image's intensity bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     */ 
    private class colorCodeHandler implements ActionListener{

      public void actionPerformed( ActionEvent e) {
          if (picNo <= 0) {
              return;
          }
          map = new TreeMap<>();
          int pic = (picNo - 1);
          for (int i = 0; i < imageSize.length - 1; i++) {
              map.put(computeManhattanDistForColourCode(pic, i), i + 1);
          }
          rankImages();
      }
	}
	private class relevanceHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){

        }
    }
}


