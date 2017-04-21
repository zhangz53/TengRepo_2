using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;

namespace ButterWorthFilter
{
  public class ButterWorth : MonoBehaviour
  {
    //public ArrayList<double> dcof;
    //public ArrayList<double> ccof;

    private List<double> dcof;
    private List<double> ccof;

    public enum BandType
    {
      high,
      low,
      band
    }

    private BandType bandType;

    public ButterWorth(BandType _type)
    {
      //by default
      dcof = new List<double>();
      ccof = new List<double>();

      if (_type == BandType.high)
      {
        //1 order,  0.1hz cutoff ?
        dcof.Add(1.0);
        dcof.Add(-0.980555318909954);

        ccof.Add(0.990277659454977);
        ccof.Add(-0.990277659454977);

        bandType = BandType.high;
      }
      else if (_type == BandType.low)
      {
        dcof.Add(1.0);
        dcof.Add(-0.303346683607342);

        ccof.Add(0.348326658196329);
        ccof.Add(0.348326658196329);
        bandType = BandType.low;
      }

    }

    public Vector3 applyButterWorth(List<Vector3> xValues, List<Vector3> yValues)  //in real time
    {
      //y[n] = ccof[0] * x[n] + ccof[1]* x[n-1] - dcof[1] * y[n-1]
      if (xValues.Count - yValues.Count != 1)
      {
        print("error");
        return Vector3.zero;
      }

      //double to double?

      Vector3 yValue = new Vector3();
      //yValue.Set(
      //    ((xValues.get(1).scl(ccof.get(0)))
      //    .add((xValues.Item(0).scl(ccof.Item(1)))))
      //    .sub((yValues.Item(0).scl(dcof.get(1))))
      //    );

      yValue = ((xValues[1] * (float)ccof[0]) + ((xValues[0] * (float)ccof[1])) - ((yValues[0] * (float)dcof[1])));

      //if(bandType == BandType.high)
      //	System.out.println( xValues.get(1).x  + " * " + ccof.get(0) + " + " + xValues.get(0).x  + " * " + ccof.get(1) + " - "  + yValues.get(0).x + " * " + dcof.get(1));

      return yValue;
    }
  }
}