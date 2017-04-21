package com.example.android.wearable.jumpingjack;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.android.wearable.jumpingjack.math.Vector3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * Created by Teng on 9/21/2015.
 */
public class PredictSVM {

    public svm_model linear_model;
    public Featurization featurization;
    public double[] feature_max;
    public double[] feature_min;
    private double y_lower;
    private double y_upper;
    private boolean y_scaling = false;
    private double y_max = -Double.MAX_VALUE;
    private double y_min = Double.MAX_VALUE;
    public int maxIndex;
    public BufferedReader fp_restore = null;
    public BufferedReader fp_restore_2 = null;

    public double lower = -1.0;
    public double upper = 1.0;

    public PredictSVM(InputStream modelFileinputstream, InputStream rangeFileinputstream, InputStream rangeFileinputstream2)
    {
        featurization = new Featurization();
        linear_model = loadModel(modelFileinputstream);
        getScaleRange(rangeFileinputstream, rangeFileinputstream2);
    }

    private BufferedReader rewind(BufferedReader fp, InputStream filenameinputstream) throws IOException
    {
        fp.close();
        return new BufferedReader(new InputStreamReader( filenameinputstream));
    }

    private void getScaleRange(InputStream rangeFileInputStream, InputStream rangeFileInputStream2)
    {
        maxIndex = 0;

        //get max index size
        if(rangeFileInputStream != null)
        {
            int idx, c;

            fp_restore = new BufferedReader(new InputStreamReader(rangeFileInputStream));

            try {
                if((c = fp_restore.read()) == 'y')
                {
                    fp_restore.readLine();
                    fp_restore.readLine();
                    fp_restore.readLine();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                fp_restore.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                fp_restore.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String restore_line = null;
            try {
                while((restore_line = fp_restore.readLine())!=null)
                {
                    StringTokenizer st2 = new StringTokenizer(restore_line);
                    idx = Integer.parseInt(st2.nextToken());
                    maxIndex = Math.max(maxIndex, idx);
                }
            } catch (NumberFormatException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /*
            try {
                fp_restore = rewind(fp_restore, rangeFileInputStream);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
            try {
                fp_restore.close();
            }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Log.d("test", "test point 1");
        }

        try {
            feature_max = new double[(maxIndex+1)];
            feature_min = new double[(maxIndex+1)];
        } catch(OutOfMemoryError e) {
            System.err.println("can't allocate enough memory");
            System.exit(1);
        }

        for(int i=0;i<=maxIndex;i++)
        {
            feature_max[i] = -Double.MAX_VALUE;
            feature_min[i] = Double.MAX_VALUE;
        }

        if(rangeFileInputStream2 != null)
        {

            fp_restore_2 = new BufferedReader(new InputStreamReader(rangeFileInputStream2));

            // fp_restore rewinded in finding max_index
            int idx, c;
            double fmin, fmax;

            /*
            try {
                fp_restore_2.mark(2);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }				// for reset

            try {
                if((c = fp_restore.read()) == 'y')
                {
                    fp_restore.readLine();		// pass the '\n' after 'y'
                    StringTokenizer st = new StringTokenizer(fp_restore.readLine());
                    y_lower = Double.parseDouble(st.nextToken());
                    y_upper = Double.parseDouble(st.nextToken());
                    st = new StringTokenizer(fp_restore.readLine());
                    y_min = Double.parseDouble(st.nextToken());
                    y_max = Double.parseDouble(st.nextToken());
                    y_scaling = true;
                }
                else {

                    fp_restore.reset();
                }
            } catch (NumberFormatException | IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }*/

            try {
                if(fp_restore_2.read() == 'x') {
                    fp_restore_2.readLine();		// pass the '\n' after 'x'
                    StringTokenizer st = new StringTokenizer(fp_restore_2.readLine());
                    lower = Double.parseDouble(st.nextToken());
                    upper = Double.parseDouble(st.nextToken());
                    String restore_line = null;
                    while((restore_line = fp_restore_2.readLine())!=null)
                    {
                        StringTokenizer st2 = new StringTokenizer(restore_line);
                        idx = Integer.parseInt(st2.nextToken());
                        fmin = Double.parseDouble(st2.nextToken());
                        fmax = Double.parseDouble(st2.nextToken());
                        if (idx <= maxIndex)
                        {
                            feature_min[idx] = fmin;
                            feature_max[idx] = fmax;

                            Log.d("test range", "" + feature_max[idx] + "  " + feature_min[idx]);
                        }
                    }
                }
            } catch (NumberFormatException | IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                fp_restore.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public svm_model loadModel(InputStream modelFileInputStream)
    {
        BufferedReader br_model = null;

        br_model = new BufferedReader(new InputStreamReader(modelFileInputStream));

        svm_model model = null;

        try {
            model = svm.svm_load_model(br_model);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Log.d("test model", "" + model.label[0] + " " + model.label[1] + " " + model.label[2]);

        return model;
    }

    private double predictWithDefaultModel(double[] features)
    {
        svm_node[] nodes = new svm_node[features.length-1];
        for (int i = 1; i < features.length; i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features[i];

            nodes[i-1] = node;
        }

        int totalClasses = 3;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(linear_model,labels);

        double[] prob_estimates = new double[totalClasses];
        double v = svm.svm_predict_probability(linear_model, nodes, prob_estimates);

        return v;
    }

    public double predictWearSwipe(ArrayList<Vector3> ac)
    {
        double[] testData = calculateFeatures_Wear(ac);
        double predictValue = predictWithDefaultModel(testData);
        return predictValue;
    }

    public double[] calculateFeatures_Wear(ArrayList<Vector3> ac)
    {
        double[] means = featurization.meanAxes(ac);

        //stdv
        double[] stdvs = featurization.stdvAxes(ac, means);

        //skewness
        double[] skews = featurization.skewnessAxes(ac, means, stdvs);

        //kurtosis
        double[] kurs = featurization.kurtosisAxes(ac, means, stdvs);

        //diff peak
        Vector3 diffPeaks = featurization.largestNeighbourAbsDiff(ac);  //might be most important feature

        featurization.getDisplacement(ac);

        //frequency
        double[][] freqs = featurization.freq(ac);   //3 by fftBins/2 array
        //feature X frequencies
        double[] freqX = freqs[0];
        //feature Y frequencies
        double[] freqY = freqs[1];
        //feature Z frequencies
        double[] freqZ = freqs[2];

        //return the results
        double[] features = new double[]{1.0,
                stdvs[0], stdvs[1], stdvs[2], skews[0], skews[1], skews[2], kurs[0], kurs[1], kurs[2], diffPeaks.x, diffPeaks.y, diffPeaks.z, //12
                featurization.filter_velocity.x, featurization.filter_velocity.y, featurization.filter_velocity.z,

                freqX[1], freqX[2], freqX[3], freqX[4],freqX[5], freqX[6], freqX[7], freqX[8], freqX[9], freqX[10],
                freqX[11], freqX[12], freqX[13], freqX[14], freqX[15], freqX[16], freqX[17], freqX[18], freqX[19], freqX[20],
                freqX[21],freqX[22],freqX[23],freqX[24],freqX[25],freqX[26],freqX[27],freqX[28],freqX[29],freqX[30],freqX[31],

                freqY[1], freqY[2], freqY[3], freqY[4],freqY[5], freqY[6], freqY[7], freqY[8], freqY[9], freqY[10],
                freqY[11], freqY[12], freqY[13], freqY[14], freqY[15], freqY[16], freqY[17], freqY[18], freqY[19], freqY[20],
                freqY[21],freqY[22],freqY[23],freqY[24],freqY[25],freqY[26],freqY[27],freqY[28],freqY[29],freqY[30],freqY[31],

                freqZ[1], freqZ[2], freqZ[3], freqZ[4],freqZ[5], freqZ[6], freqZ[7], freqZ[8], freqZ[9], freqZ[10],
                freqZ[11], freqZ[12], freqZ[13], freqZ[14], freqZ[15], freqZ[16], freqZ[17], freqZ[18], freqZ[19], freqZ[20],
                freqZ[21],freqZ[22],freqZ[23],freqZ[24],freqZ[25],freqZ[26],freqZ[27],freqZ[28],freqZ[29],freqZ[30],freqZ[31]  //3 * 31

        };

        //Log.d("test 1", "" + features[10]);

        //need to be scaled
        for(int itrf = 1; itrf < features.length; itrf++)
        {
            features[itrf] =  scaleOutput(features[itrf], itrf);
        }

        //Log.d("test 2", "" + features[10]);

        return features;
    }


    private double scaleOutput(double value, int index)
    {
		/* skip single-valued attribute */
        if(feature_max[index] == feature_min[index])
            return 0.0;

        double sValue = 0.0;

        if(value == feature_min[index])
            sValue = lower;
        else if(value == feature_max[index])
            sValue = upper;
        else
            sValue = lower + (upper-lower) *
                    (value-feature_min[index])/
                    (feature_max[index]-feature_min[index]);


        return sValue;
    }
}
