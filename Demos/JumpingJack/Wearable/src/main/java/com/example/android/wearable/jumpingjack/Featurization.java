package com.example.android.wearable.jumpingjack;

import com.example.android.wearable.jumpingjack.fft.RealDoubleFFT;
import com.example.android.wearable.jumpingjack.math.Matrix4;
import com.example.android.wearable.jumpingjack.math.Vector3;

import java.util.ArrayList;

/**
 * Created by Teng on 9/21/2015.
 */
public class Featurization {

    public int fftBins = 64;///////8;  //try to change
    public int Fs = 100;  //about 66hz
    public double Ts = 1.0/Fs;  //about 0.015s
    public RealDoubleFFT mRealFFT;
    public double scale;
    private final static float MEAN_MAX = 16384f;   // Maximum signal value

    //for position translation
    public Vector3 pos;
    public Vector3 velocity;
    public Vector3 linAcc;
    public Vector3 filteredAcc;
    public Vector3 filter_velocity;
    public Vector3 filter_pos;
    public Matrix4 mMatrix;
    public static double stamp = 0.01;  //in seconds

    public ButterWorth mButterHp;

    public Featurization()
    {
        pos = new Vector3();
        velocity = new Vector3();
        linAcc = new Vector3();
        filteredAcc = new Vector3();
        filter_velocity = new Vector3();
        filter_pos = new Vector3();
        mMatrix = new Matrix4();

        mButterHp = new ButterWorth(ButterWorth.BandType.high);
        mButterHp.createDataSet();
        mButterHp.createDataSet();

        mRealFFT = new RealDoubleFFT(fftBins);
        scale =  MEAN_MAX * MEAN_MAX * fftBins * fftBins / 2d;
    }


    public double[] meanAxes(ArrayList<Vector3> list)
    {
        double[] axeValues = new double[3];
        axeValues[0] = 0.0;
        axeValues[1] = 0.0;
        axeValues[2] = 0.0;

        int sz = list.size();
        for(Vector3 acc : list)
        {
            axeValues[0] += acc.x;
            axeValues[1] += acc.y;
            axeValues[2] += acc.z;
        }

        axeValues[0] = axeValues[0] / sz;
        axeValues[1] = axeValues[1] / sz;
        axeValues[2] = axeValues[2] / sz;

        return axeValues;
    }

    public double[] stdvAxes(ArrayList<Vector3> list, double[] mean)
    {
        double[] axeValues = new double[3];
        axeValues[0] = 0.0;
        axeValues[1] = 0.0;
        axeValues[2] = 0.0;

        int sz = list.size();
        for(Vector3 acc : list)
        {
            axeValues[0] += ((acc.x - mean[0]) * (acc.x - mean[0]));
            axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]));
            axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]));
        }

        axeValues[0] = Math.sqrt( axeValues[0] / sz);
        axeValues[1] = Math.sqrt( axeValues[1] / sz);
        axeValues[2] = Math.sqrt( axeValues[2] / sz);

        return axeValues;
    }

    public double[] skewnessAxes(ArrayList<Vector3> list, double[] mean, double[] stdv)
    {
        double[] axeValues = new double[3];
        axeValues[0] = 0.0;
        axeValues[1] = 0.0;
        axeValues[2] = 0.0;

        int sz = list.size();
        for(Vector3 acc : list)
        {
            axeValues[0] += ((acc.x - mean[0]) * (acc.x - mean[0]) * (acc.x - mean[0]));
            axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]));
            axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]));
        }

        axeValues[0] = (axeValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0]);
        axeValues[1] = (axeValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1]);
        axeValues[2] = (axeValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2]);

        //double para = Math.sqrt(sz * (sz - 1)) / (sz - 1);
        //axeValues[0] = axeValues[0] * para;
        //axeValues[1] = axeValues[1] * para;
        //axeValues[2] = axeValues[2] * para;

        return axeValues;
    }

    public double[] kurtosisAxes(ArrayList<Vector3> list, double[] mean, double[] stdv)
    {
        double[] axeValues = new double[3];
        axeValues[0] = 0.0;
        axeValues[1] = 0.0;
        axeValues[2] = 0.0;

        int sz = list.size();
        for(Vector3 acc : list)
        {
            axeValues[0] += ((acc.x - mean[0]) * (acc.x - mean[0]) * (acc.x - mean[0]) * (acc.x - mean[0]));
            axeValues[1] += ((acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]) * (acc.y - mean[1]));
            axeValues[2] += ((acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]) * (acc.z - mean[2]));
        }

        axeValues[0] = (axeValues[0] / sz) / (stdv[0] * stdv[0] * stdv[0] * stdv[0]) - 3.0;
        axeValues[1] = (axeValues[1] / sz) / (stdv[1] * stdv[1] * stdv[1] * stdv[1]) - 3.0;
        axeValues[2] = (axeValues[2] / sz) / (stdv[2] * stdv[2] * stdv[2] * stdv[2]) - 3.0;

        return axeValues;
    }

    public Vector3 largestNeighbourAbsDiff(ArrayList<Vector3> list)
    {
        ArrayList<Vector3> diffs = neighbourAbsDiff(list);
        int peakIndex = localPeakIndex(diffs)[0];
        Vector3 result = new Vector3();
        result.Set(diffs.get(peakIndex));
        return result;
    }

    public ArrayList<Vector3> neighbourAbsDiff(ArrayList<Vector3> list)
    {
        ArrayList<Vector3> diffs = new ArrayList<Vector3>();
        if(list.size() < 2)
            return diffs;

        for(int itr = 1; itr < list.size(); itr++)
        {
            Vector3 diff = new Vector3();
            diff.Set(list.get(itr).sub(list.get(itr-1)));
            diff.Abs();
            diffs.add(diff);
        }

        return diffs;
    }

    public int[] localPeakIndex(ArrayList<Vector3> list){

        //get the 1st and 2nd peak index
        int sz = list.size();
        ArrayList<Double> absValue = new ArrayList<Double>();
        for(Vector3 acc : list){
            double sum = Math.abs(acc.x) + Math.abs(acc.y) + Math.abs(acc.z);
            absValue.add(sum);
        }

        //difference values
        ArrayList<Double> diff = new ArrayList<Double>();
        diff.add(0.0);
        for(int itra = 1; itra < absValue.size(); itra++)
        {
            diff.add(absValue.get(itra) - absValue.get(itra-1));
        }

        ArrayList<Integer> peakCandy = new ArrayList<Integer>();
        ArrayList<Double> candyValue = new ArrayList<Double>();
        for(int itrd = 0; itrd < (diff.size() -1 ); itrd++)
        {
            if(diff.get(itrd) > 0 && diff.get(itrd+1) < 0)
            {
                peakCandy.add(itrd);
                candyValue.add(absValue.get(itrd));
            }
        }

        //check pool
        if(candyValue.size() < 2)
        {
            int[] result = new int[2];
            result[0] = 0;
            result[1] = 1;
            return result;
        }


        //rand candyValue and the 1st and 2nd largest
        int index = getLargestInList(candyValue);
        int firstIndex = peakCandy.get(index);

        candyValue.remove(index);
        peakCandy.remove(index);

        index = getLargestInList(candyValue);
        int secondIndex = peakCandy.get(index);

        int[] result = new int[2];
        result[0] = firstIndex;
        result[1] = secondIndex;

        return result;
    }

    public int getLargestInList(ArrayList<Double> list)
    {
        double temp = 0.0;
        int target = 0;

        for(int itrl = 0; itrl < list.size(); itrl++)
        {
            if(list.get(itrl) > temp)
            {
                target = itrl;
                temp = list.get(itrl);
            }
        }

        return target;
    }

    public void getDisplacement(ArrayList<Vector3> accList)  //already converted to along and orthogonal to movements
    {
        int sz = accList.size();
        velocity.Set(Vector3.Zero);
        pos.Set(Vector3.Zero);

        //mButterLp.refreshDataSet(1);
        mButterHp.refreshDataSet(1);
        mButterHp.refreshDataSet(2);

        for(int itra = 0; itra < sz; itra++)
        {
            //how about filter the noise of acc
            linAcc.Set(accList.get(itra).x, accList.get(itra).y, accList.get(itra).z);
            //filteredAcc.Set(mButterLp.applyButterWorth(1, 1, linAcc));

            velocity.Add(linAcc.scl(stamp));
            filter_velocity.Set(mButterHp.applyButterWorth(1, 1, velocity));


            pos.Add(filter_velocity.scl(stamp));
            filter_pos.Set(mButterHp.applyButterWorth(2, 1, pos));

            //System.out.println(filteredAcc.y);
            //System.out.println(velocity.y);
            //System.out.println(pos.x);
        }

        //System.out.println();
    }

    public double[][] freq(ArrayList<Vector3> ac)  //size of ac should equal to fftBins
    {
        double[][] result = new double[3][fftBins/2];  //get fftbins samples
        if(ac.size() != fftBins)
        {
            return result;
        }

        double[] fftDataX = new double[fftBins];
        double[] fftDataY = new double[fftBins];
        double[] fftDataZ = new double[fftBins];

        for(int itra = 0; itra < fftBins; itra++)
        {
            fftDataX[itra] = ac.get(itra).x;
            fftDataY[itra] = ac.get(itra).y;
            fftDataZ[itra] = ac.get(itra).z;
        }

        mRealFFT.ft(fftDataX);
        mRealFFT.ft(fftDataY);
        mRealFFT.ft(fftDataZ);

        //convert to db
        convertToDb(fftDataX, scale);
        convertToDb(fftDataY, scale);
        convertToDb(fftDataZ, scale);

        double[] resultX = new double[fftBins/2];
        double[] resultY = new double[fftBins/2];
        double[] resultZ = new double[fftBins/2];

        for(int itr = 0; itr < fftBins/2; itr++)
        {
            resultX[itr] = fftDataX[itr];
            resultY[itr] = fftDataY[itr];
            resultZ[itr] = fftDataZ[itr];
        }

        result[0] = resultX;
        result[1] = resultY;
        result[2] = resultZ;

        return result;

    }

    public double[] convertToDb(double[] data, double maxSquared) {
        data[0] = db2(data[0], 0.0, maxSquared);
        int j = 1;
        for (int i=1; i < data.length - 1; i+=2, j++) {
            data[j] = db2(data[i], data[i+1], maxSquared);
        }
        data[j] = data[0];
        return data;
    }

    private double db2(double r, double i, double maxSquared) {
        return 5.0 * Math.log10((r * r + i * i) / maxSquared);
    }
}
