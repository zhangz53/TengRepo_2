using UnityEngine;
using System.Collections;

public interface ITransformable
{
    void Selected();
    void Deselected();
    void Translate(Quaternion quat, float[] initAngles, float[] currAngles);
    void Rotate(Quaternion quat, float[] initAngles, float[] currAngles);

    void Scale(Quaternion quat, float[] initAngles, float[] currAngles);

    void Reset(Quaternion quat, float[] initAngles, float[] currAngles);

}