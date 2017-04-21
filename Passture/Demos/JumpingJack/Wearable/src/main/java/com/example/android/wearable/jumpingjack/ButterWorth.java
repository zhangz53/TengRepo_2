package com.example.android.wearable.jumpingjack;

import com.example.android.wearable.jumpingjack.math.Vector3;
import java.util.ArrayList;


/**
 * Created by Teng on 9/21/2015.
 */
public class ButterWorth {

    public ArrayList<Double> dcof;
    public ArrayList<Double> ccof;
    public ArrayList<ArrayList<Vector3>> inputDataSets;
    public ArrayList<ArrayList<Vector3>> outputDataSets;

    public enum BandType{
        high,
        low,
        band
    }

    public ButterWorth(BandType _type)
    {
        dcof = new ArrayList<Double>();
        ccof = new ArrayList<Double>();

        inputDataSets = new ArrayList<ArrayList<Vector3>>();
        outputDataSets = new ArrayList<ArrayList<Vector3>>();

        if(_type == BandType.high){
            dcof.add(1.0);
            dcof.add(-0.993736471541615);

            ccof.add(0.996868235770807);
            ccof.add(-0.996868235770807);
        }
    }

    public void createDataSet()
    {
        ArrayList<Vector3> inputSet = new ArrayList<Vector3>();
        ArrayList<Vector3> outputSet = new ArrayList<Vector3>();

        inputDataSets.add(inputSet);
        outputDataSets.add(outputSet);
    }

    public void refreshDataSet(int index)
    {
        ArrayList<Vector3> inputSet = inputDataSets.get(index - 1);
        ArrayList<Vector3> outputSet = outputDataSets.get(index - 1);

        inputSet.clear();
        outputSet.clear();
    }

    public Vector3 applyButterWorth(ArrayList<Vector3> xValues, ArrayList<Vector3> yValues)  //in real time
    {
        //y[n] = ccof[0] * x[n] + ccof[1]* x[n-1] - dcof[1] * y[n-1]
        if(xValues.size() - yValues.size() != 1){
            System.out.println("error");
            return Vector3.Zero;
        }

        //double to double?

        Vector3 yValue = new Vector3();
        yValue.Set(
                ((xValues.get(1).scl(ccof.get(0)))
                        .add(  (xValues.get(0).scl(ccof.get(1))  )))
                        .sub(  (yValues.get(0).scl(dcof.get(1))  ))
        );

        //if(bandType == BandType.high)
        //	System.out.println( xValues.get(1).x  + " * " + ccof.get(0) + " + " + xValues.get(0).x  + " * " + ccof.get(1) + " - "  + yValues.get(0).x + " * " + dcof.get(1));

        return yValue;
    }

    public Vector3 applyButterWorth(int datasetIndex, int order,  Vector3 xValue)
    {
        Vector3 yValue = new Vector3();
        if(datasetIndex > inputDataSets.size())
            return yValue;

        ArrayList<Vector3> xValues = inputDataSets.get(datasetIndex - 1);
        ArrayList<Vector3> yValues = outputDataSets.get(datasetIndex - 1);

        Vector3 tempx = new Vector3();
        tempx.Set(xValue);
        xValues.add(tempx);

        if(xValues.size() > (order + 1))
        {
            xValues.remove(0);
        }

        if(xValues.size() == (order + 1))
        {
            yValue = applyButterWorth(xValues, yValues);
            yValues.add(yValue);
        }else
        {
            Vector3 tempy = new Vector3();
            tempy.Set(xValue);
            yValues.add(tempy);

            yValue.Set(tempy);
        }

        if(yValues.size() > order)
        {
            yValues.remove(0);
        }

        return yValue;
    }

}
