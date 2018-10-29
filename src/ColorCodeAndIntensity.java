import java.util.List;

public class ColorCodeAndIntensity {

    private double[][] featureVectorMatrix = new double[100][89];
    private double[][] featureMatrixAvgDev = new double[2][89];
    private double[][] normalisedFeatureMatrix = new double[100][89]; //0-99 ->imgs; 25 + 64 bin
    private double[][] weightMatrixAvgStd = new double[4][89]; // 0->avg of the column; 1->std deviation 2-> weight(1/std dev) 3-> normalised weight

    ColorCodeAndIntensity(double[][] histogramMatrix, double[][] colorCodeMatrix) {
        calcFeatureMatrix(histogramMatrix, colorCodeMatrix);
        calc_Avg_Std();
        calcNormalisedFeatureMatrix();
        // set default weight matrix
        for (int i=0;i<weightMatrixAvgStd[0].length;i++) {
            weightMatrixAvgStd[3][i] = (double) 1/89;
        }
    }

    public double computeRelevanceFeedbackDistance(int i, int j) {
        double dist = 0;
        for(int k=0; k<normalisedFeatureMatrix[0].length; k++){
            dist += (Math.abs((normalisedFeatureMatrix[i][k])  - (normalisedFeatureMatrix[j][k]))) * weightMatrixAvgStd[3][k];
        }
        return dist;
    }

    public void computeNormalizedWeights(List<Integer> relevantImageNos) {
        if (relevantImageNos.size()>1) {
            extractFeatureMatrix(relevantImageNos);
            calcNormalisedWeight();
        }
    }

    private void calcFeatureMatrix(double[][] histogramMatrix, double[][] colorCodeMatrix){

        int intensityRows = histogramMatrix.length;
        int intensityCols = histogramMatrix[0].length - 2;
        for(int i=0; i<intensityRows; i++){
            for(int j=0; j<intensityCols; j++){
                featureVectorMatrix[i][j] = histogramMatrix[i][j]/histogramMatrix[i][26];  // divide by sum
            }
        }

        int colorCodeRows = colorCodeMatrix.length;
        int colorCodeCols = colorCodeMatrix[0].length - 1;
        for(int i=0; i<colorCodeRows; i++){
            int k=0;
            for(int j=intensityCols; j<(intensityCols)+ (colorCodeCols); j++){
                featureVectorMatrix[i][j] = colorCodeMatrix[i][k++]/colorCodeMatrix[i][64];
            }
        }
        // print the array
        /*for (int i=0;i<featureVectorMatrix.length;i++) {
            for (int j=0;j<featureVectorMatrix[0].length; j++) {
                System.out.print(featureVectorMatrix[i][j] + ",");
            }
            System.out.println();
        }*/
    }

    private void calc_Avg_Std(){
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
            for (int j=0; j<rows; j++){
                sum += Math.pow(featureVectorMatrix[j][i] - featureMatrixAvgDev[0][i], 2);
            }
            featureMatrixAvgDev[1][i] = Math.sqrt(sum/(rows-1));
        }
    }

    private void calcNormalisedFeatureMatrix() {
        int cols = featureVectorMatrix[0].length;
        int rows = featureVectorMatrix.length;
        for(int i=0; i<cols; i++){
            for(int j=0; j<rows; j++){
                if (featureMatrixAvgDev[1][i] == 0) {
                    normalisedFeatureMatrix[j][i] = 0;
                } else {
                    //gaussian normalisation
                    double sub = (featureVectorMatrix[j][i] - featureMatrixAvgDev[0][i]);
                    normalisedFeatureMatrix[j][i] = sub / (featureMatrixAvgDev[1][i]);
                }
            }
        }
    }

    private void extractFeatureMatrix(List<Integer> li) {
        int cols = normalisedFeatureMatrix[0].length;
        double[][] relevantImageMatrix = new double[li.size()][cols];

        int k=0;
        for(int imgNo : li) {
            for (int i = 0; i < cols; i++) {
                relevantImageMatrix[k][i] = normalisedFeatureMatrix[imgNo][i];
            }
            k++;
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
        //calc standard deviation
        for (int i=0; i<cols; i++){
            double sum = 0;
            for(int j=0; j<li.size(); j++){
                double diff = relevantImageMatrix[j][i] - weightMatrixAvgStd[0][i];
                sum += Math.pow(diff, 2);
            }
            weightMatrixAvgStd[1][i] = Math.sqrt(sum/(li.size()-1));
        }
    }

  private void calcNormalisedWeight(){
        int cols = featureVectorMatrix[0].length;
        double min = Integer.MAX_VALUE;
        for(int i=0; i<cols; i++){
            // find min value of non zero std-dev
            if(weightMatrixAvgStd[1][i] != 0) {
                min = Math.min(min, weightMatrixAvgStd[1][i]);
            }
        }
        //update std deviation
        for(int i=0; i<cols; i++){
            // when both mean and std dev == 0
            if (weightMatrixAvgStd[1][i] == 0 && weightMatrixAvgStd[0][i] == 0) {
                weightMatrixAvgStd[1][i] = 0;
            }
            // when mean is non zero, but std dev == 0
            else if(weightMatrixAvgStd[1][i] == 0) {
                weightMatrixAvgStd[1][i] = (0.5 * min);
            }
        }

        //calc Weight
      double sumOfWeight = 0;
        for(int i=0; i<cols; i++){
            if (weightMatrixAvgStd[1][i]==0) {
                weightMatrixAvgStd[2][i] = 0;
            } else {
                weightMatrixAvgStd[2][i] = 1/(weightMatrixAvgStd[1][i]);
            }
            sumOfWeight += weightMatrixAvgStd[2][i];
        }
        //normalised weight
        for(int i=0; i<cols; i++) {
            if (sumOfWeight==0) {
                weightMatrixAvgStd[3][i] = 0;
            } else {
                weightMatrixAvgStd[3][i] = weightMatrixAvgStd[2][i] / sumOfWeight;
            }
            //System.out.print(weightMatrixAvgStd[3][i] + ",");
        }
      System.out.println();
  }

}
