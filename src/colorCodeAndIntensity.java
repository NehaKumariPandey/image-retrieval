import java.util.List;

public class colorCodeAndIntensity {

    double[][] normalisedFeatureMatrix = new double[100][89];
    //25 + 64 bin ; 0-99 ->imgs 100->avg 101-> standard deviation
    double[][] featureVectorMatrix = new double[102][89];
    double[][] weightMatrix;

    public colorCodeAndIntensity(){

    }

    public void calcFeatureMatrix(int[][] histogramMatrix, int[][] colorCodeMatrix){

        for(int i=0; i<100; i++){
            for(int j=0; i<25; i++){

                featureVectorMatrix[i][j] = histogramMatrix[i][j]/ histogramMatrix[i][26];
            }
        }
        for(int i=0; i<100; i++){
            int k=0;
            for(int j=25; j<89; j++){
                featureVectorMatrix[i][j] = colorCodeMatrix[i][k++]/colorCodeMatrix[i][64];
            }
        }
    }
    public void calc_Avg_Std(){
        //calculate average
        for(int i=0; i<89; i++) {
            double sum = 0;
            for (int j=0; j < 100; j++) {
                sum += featureVectorMatrix[j][i];
            }
            featureVectorMatrix[100][i] = sum/(100);
        }

        //calculate standard deviation
        for(int i=0; i<89; i++){
            double sum =0;
            for(int j=0; j<100; j++){
                sum += Math.pow(featureVectorMatrix[j][i] - featureVectorMatrix[100][i], 2);
            }
            featureVectorMatrix[101][i] = Math.sqrt(sum/(100-1));
        }
    }

    public void calcNormalisedFeatureMatrix(){
     for(int i=0; i<89; i++){
         for(int j=0; j<100; j++){
             normalisedFeatureMatrix[j][i] = (featureVectorMatrix[j][i] - featureVectorMatrix[100][i])/featureVectorMatrix[101][i];
         }
     }
    }

    public void updateWeight(List<Integer> li){
        weightMatrix = new double[li.size()+2][89];  //li.size()+1 -> storing avg of each column/
                                                     //li.size()+2 ->
        for(int imgNo : li) {
            for (int i = 0; i < 89; i++) {
                weightMatrix[imgNo][i] = normalisedFeatureMatrix[imgNo][i];
            }
        }

        //calc avg of selected Imgs
        for(int i=0; i<89; i++){
            int sum = 0;
            for(int j=0; j<li.size(); j++){
                sum += weightMatrix[j][i];
            }
            int avg = sum;////(li.size());
            weightMatrix[li.size()+1][i] = avg;
        }

        //calc standard deviation

    }


}
