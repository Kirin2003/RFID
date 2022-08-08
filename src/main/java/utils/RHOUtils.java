package utils;

/**
 * Used it DLS and EDLS to calculate the optimal frame size
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class RHOUtils {
    private static boolean isFirstRead = true;
    private static double[] P = null;
    private static double[] Rho = null;

    private static void initData(String filename){
        BufferedReader reader = null;
        P = new double[100];
        Rho = new double[100];
        boolean isP = false;
        boolean isRho = false;
        try (FileInputStream fileInputStream = new FileInputStream(filename)) {
            reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String strline;
            int  i  = 0, j = 0;
            while ((strline = reader.readLine()) != null){
                if (strline.contains("P"))
                    isP = true;
                if (strline.contains("RHO")){
                    isP = false;
                    isRho = true;
                }
                if (isP){
                    if (!(strline.contains("Col") || strline.contains("P"))){
                        //System.out.println("ppp");
                        //System.out.println("strline="+strline);
                        String[] subp = strline.split("    ");

                        //System.out.println("subp.length="+subp.length);
                        //System.out.println("subp="+subp);
                        for (int k = 0; k < subp.length; k++) {
                            //System.out.println("k="+k);
                            P[i++] = Double.valueOf(subp[k]);
                        }
                    }
                }
                if (isRho){
                    if (!(strline.contains("Col") || strline.contains("RHO"))){
                        ////NO USE String[] subp = strline.split("    ");

                        strline=strline.replaceAll("\\s+ {2,}", " ");

                        //System.out.println("strline["+strline+"]");

                        String[] subp = strline.trim().split(" ");


                        // System.out.println("RhoRhoRho");
                        // System.out.println("subp.length="+subp.length);
                        // System.out.println("subp="+subp);

                        for (int k = 0; k < subp.length; k++) {
                            Rho[j++] = Double.valueOf(subp[k]);
                            // System.out.println(" rho["+j+"]"+Double.valueOf(subp[k]));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the best RHO
     * @param missRate
     * @param filename
     * @return
     */
    public static double getBestRho(double missRate,String filename){
        if (isFirstRead){
            initData(filename);
            isFirstRead = false;
        }
        int index = Arrays.binarySearch(P, missRate);
        // System.out.println("index="+index);
        if (index < 0){
            if(index==-1){
                return (Rho[0]);
            }else{
                index = Math.abs(index+1);
                return (Rho[index]+Rho[index-1]) / 2;
            }
        }
        else {
            if (index == P.length)
                return Rho[Rho.length-1];
            else if (index == 0)
                return Rho[0];
            else
                return Rho[index];
        }
    }

}
