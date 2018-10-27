public class practice {
    public static void main(String[] args){
        double[] ar = {0.25, 0.1, 0.4, 0.4};
        double avg = 0;
        for(double a: ar){
            avg += a;
        }
        avg /= 4;
        double sum = 0;
        for(double a : ar) {
            sum += Math.pow(a - avg, 2);
        }
        System.out.println("standard deviation "+ Math.sqrt(sum/3));
        System.out.println(" hey done");
    }
}
