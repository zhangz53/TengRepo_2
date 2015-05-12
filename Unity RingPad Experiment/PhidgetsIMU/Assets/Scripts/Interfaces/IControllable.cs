using UnityEngine;
using System.Collections;

public interface IControllable
{
    void SwipeUp();
    void SwipeDown();
    void SwipeLeft();
    void SwipeRight();
    void SingleClick();
    void DoubleClick();
    void CCTwist();
    void CTwist();
    void GetIMUVals(Quaternion quat, float[] angles);
}
