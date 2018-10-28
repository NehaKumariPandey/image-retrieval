import java.util.List;

public class colorCodeAndIntensity {

    double[][] normalisedFeatureMatrix = new double[100][89];
    //25 + 64 bin ; 0-99 ->imgs
    double[][] featureVectorMatrix = new double[100][89];
    double[][] featureMatrixAvgDev = new double[2][89];
    double[][] relevantImageMatrix;
    double[][] weightMatrixAvgStd = new double[4][89]; // 0->avg of the column; 1->std deviation 2-> weight(1/std dev) 3-> normalised weight

    public colorCodeAndIntensity(){

    }

    public void calcFeatureMatrix(double[][] histogramMatrix, double[][] colorCodeMatrix){

        int intensityRows = histogramMatrix.length;
        int intensityCols = histogramMatrix[0].length - 2;
        for(int i=0; i<intensityRows; i++){
            for(int j=0; i<intensityCols; i++){
                featureVectorMatrix[i][j] = histogramMatrix[i][j]/ histogramMatrix[i][26];  // divide by sum
            }
        }

        int colorCodeRows = colorCodeMatrix.length;
        int colorCodeCols = colorCodeMatrix[0].length - 1;
        for(int i=0; i<colorCodeRows; i++){
            int k=0;
            for(int j=intensityCols; j<(intensityCols)+ (colorCodeCols); j++){
                featureVectorMatrix[i][j] = colorCodeMatrix[i][k++]/colorCodeMatrix[i][64];
                System.out.print(featureVectorMatrix[i][j]+", ");
            }
            System.out.println();
        }
    }
    public void calc_Avg_Std(){
        //calculate average
        int cols = featureVectorMatrix[0].length;
        int rows = featureVectorMatrix.length;
        for(int i=0; i<cols; i++) {
            double sum = 0;
            for (int j=0; j < rows; j++) {
                sum += featureVectorMatrix[j][i];
            }
            featureMatrixAvgDev[0][i] = sum/rows;
        }

        //calculate standard deviation
        for(int i=0; i<cols; i++){
            double sum =0;
            for(int j=0; j<rows; j++){
                sum += Math.pow(featureVectorMatrix[j][i] - featureMatrixAvgDev[0][i], 2);
            }
            featureMatrixAvgDev[1][i] = Math.sqrt(sum/(rows-1));
        }
    }

    public void calcNormalisedFeatureMatrix(){
        int cols = featureVectorMatrix[0].length;
        int rows = featureVectorMatrix.length;
     for(int i=0; i<cols; i++){
         for(int j=0; j<rows; j++){
             //gaussian normalisation
             double sub = (featureVectorMatrix[j][i] - featureMatrixAvgDev[0][i]);
             normalisedFeatureMatrix[j][i] = sub /(featureMatrixAvgDev[1][i]);
         }
     }
    /* for(int i=0; i<100; i++){
         for(int j=0; j<89; j++){
             System.out.print(normalisedFeatureMatrix[i][j] +", ");
         }
         System.out.println();
     }*/
    }

    public void extractFeatureMatrix(List<Integer> li){
        int cols = normalisedFeatureMatrix[0].length;
        relevantImageMatrix = new double[li.size()][cols];

        int k=0;
        for(int imgNo : li) {
            for (int i = 0; i < cols; i++) {
                relevantImageMatrix[k++][i] = normalisedFeatureMatrix[imgNo][i];
            }
        }

        //calc avg of selected Imgs
        for(int i=0; i<cols; i++){
            double sum = 0;
            for(int j=0; j<li.size(); j++){
                sum += relevantImageMatrix[j][i];
            }
            double avg = sum/(li.size());
            weightMatrixAvgStd[0][i] = avg;
        }
        int rows = featureVectorMatrix.length;
        //calc standard deviation
        for (int i=0; i<cols; i++){
            double sum = 0;
            for(int j=0; j<rows; j++){
                double diff = relevantImageMatrix[j][i] - weightMatrixAvgStd[0][i];
                sum += Math.pow(diff, 2);
            }
            weightMatrixAvgStd[1][i] = Math.sqrt(sum/(rows-1));
        }
    }

  public void calcNormalisedWeight(){
        int cols = featureVectorMatrix[0].length;
        double min = 0;
        for(int i=0; i<cols; i++){
            // find min value of non zero std-dev
            if(weightMatrixAvgStd[1][i] != 0) {
                min = Math.min(min, weightMatrixAvgStd[1][i]);
            }
        }
        //update std deviation
        for(int i=0; i<cols; i++){
            if(weightMatrixAvgStd[1][i] == 0 && weightMatrixAvgStd[0][i] == 0){
                weightMatrixAvgStd[1][i] = 0;
            }else if(weightMatrixAvgStd[1][i] == 0){
                weightMatrixAvgStd[1][i] = (0.5 * min);
            }

        }

        //calc Weight
      double sumOfWeight = 0;
        for(int i=0; i<cols; i++){
            weightMatrixAvgStd[2][i] = 1/(weightMatrixAvgStd[1][i]);
            sumOfWeight += weightMatrixAvgStd[2][i];
        }
        //normalised weight
        for(int i=0; i<cols; i++){
            weightMatrixAvgStd[3][i] = weightMatrixAvgStd[2][i] / sumOfWeight;
        }
  }
   public void calcWeightedDistance(){

   }

}
