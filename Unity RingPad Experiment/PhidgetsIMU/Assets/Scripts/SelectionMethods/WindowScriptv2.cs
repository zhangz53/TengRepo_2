using UnityEngine;
using System.Collections;

public class WindowScriptv2 : MonoBehaviour, ITransformable, IControllable
{
    public Material selectedMaterial;
    public Material deselectedMaterial;
    public Material ghostMaterial;

    private Camera cam;
    private GameObject ghost;

    private Quaternion lastRotationInv;
    private Quaternion[] quats;
    private float[] angles;

    public float IMUNoiseFactor = 0.00125f;
    public float MultiplierX = .00000000001f;
    public float MultiplierY = .00000000001f;

    public bool confirmTrans = false;

    void Start()
    {
        cam = GameObject.FindWithTag("MainCamera").camera;

        ghost = Instantiate(gameObject) as GameObject;
        ghost.renderer.material = ghostMaterial;
        ghost.transform.parent = transform;
        ghost.transform.localScale -= new Vector3(0.5f, 0.5f, 0);
        ghost.SetActive(false);
    }

    public void SwipeUp()
    {

    }
    public void SwipeDown()
    {

    }
    public void SwipeLeft()
    {

    }
    public void SwipeRight()
    {

    }
    public void SingleClick()
    {

    }
    public void DoubleClick()
    {

    }
    public void CCTwist()
    {

    }
    public void CTwist()
    {

    }

    public void Selected()
    {
        gameObject.renderer.material = selectedMaterial;
    }
    public void Deselected()
    {
        gameObject.renderer.material = deselectedMaterial;
        hideGhost();
    }
    
    public void GetIMUVals(Quaternion[] imuQuats, float[] imuAngles)
    {
        quats = imuQuats;
        angles = imuAngles;
    }

    public void Translate(Quaternion[] quats, float[] initAngles, float[] currAngles)
    {
        Vector3 centerOfCamera = cam.ScreenToWorldPoint(new Vector3(Screen.width / 2, Screen.height / 2, cam.nearClipPlane + 75));
        Quaternion cameraRotation = cam.transform.rotation;
        double[] deltaAndDirection = getDeltaAndDirection(initAngles, currAngles);

        //centerOfCamera.z = transform.position.z;
        if (!renderer.isVisible)
        {
            Quaternion currRotation = transform.rotation;
            Vector3 currRotEuler = Quaternion.ToEulerAngles(currRotation);
            currRotEuler.z = 90;
            currRotation = Quaternion.Euler(currRotEuler);

            if (!ghost.activeSelf)
            {

                ghost.SetActive(true);
                ghost.transform.position = centerOfCamera;

                ghost.transform.rotation = currRotation;
                ghost.transform.LookAt(cam.transform.position, Vector3.up);
                ghost.transform.Rotate(new Vector3(0, 1, 0), 180);
            }
            else
            {
                //translateBallJoint(ghost, deltaAndDirection);
                ghost.transform.position = centerOfCamera;

                ghost.transform.LookAt(cam.transform.position, Vector3.up);
                ghost.transform.Rotate(new Vector3(0, 1, 0), 180);

                if (!ghost.renderer.isVisible)
                    hideGhost();

            }

            if (confirmTrans)
            {
                transform.position = ghost.transform.position;
                transform.rotation = ghost.transform.rotation;
                confirmTrans = false;
                hideGhost();
            }
        }
        else
        {
            translateBallJoint(deltaAndDirection);
        }
    }
    public void Rotate(Quaternion[] quats, float[] initAngles, float[] currAngles)
    {
        ghost.SetActive(false);

        double[] deltaAndDirection = getDeltaAndDirection(initAngles, currAngles);
        rotateBallJoint(deltaAndDirection);
    }

    public void Scale(Quaternion[] quats, float[] initAngles, float[] currAngles)
    {
        ghost.SetActive(false);

        double[] deltaAndDirection = getDeltaAndDirection(initAngles, currAngles);
        scaleBallJoint(deltaAndDirection);
    }
    public void Reset(Quaternion[] quats, float[] initAngles, float[] currAngles)
    {

    }

    public void hideGhost()
    {
        if (ghost != null)
            ghost.SetActive(false);
    }

    double[] getDeltaAndDirection(float[] initAngles, float[] currAngles)
    {
        double[] deltaAndDirection = new double[6];

        float startPitch = initAngles[0];
        float startYaw = initAngles[1];
        float startRoll = initAngles[2];
        float currPitch = currAngles[0];
        float currYaw = currAngles[1];
        float currRoll = currAngles[2];

        deltaAndDirection[0] = Mathf.Abs(Mathf.Abs((float)currPitch) - Mathf.Abs((float)startPitch));
        deltaAndDirection[2] = Mathf.Abs(Mathf.Abs((float)currYaw) - Mathf.Abs((float)startYaw));
        deltaAndDirection[4] = Mathf.Abs(Mathf.Abs((float)currRoll) - Mathf.Abs((float)startRoll));

        deltaAndDirection[1] = ((startPitch > 0 && currPitch > 0 && currPitch > startPitch) || startPitch < 0 && (currPitch > 0 || (currPitch > 0 && currPitch < startPitch))) ? 1 : -1;
        deltaAndDirection[3] = ((startYaw > 0 && currYaw > 0 && currYaw < startYaw) || startYaw < 0 && (currYaw > 0 || currYaw < startYaw)) ? -1 : 1;
        deltaAndDirection[5] = ((startRoll > 0 && currRoll > 0 && currRoll > startRoll) || startRoll < 0 && (currRoll > 0 || currRoll < startRoll)) ? 1 : -1;


        return deltaAndDirection;
    }

    void translateBallJoint(double[] deltaAndDirection)
    {
        double deltaPitch = deltaAndDirection[0];
        double directionPitch = deltaAndDirection[1];
        double deltaYaw = deltaAndDirection[2];
        double directionYaw = deltaAndDirection[3];
        double deltaRoll = deltaAndDirection[4];
        double directionRoll = deltaAndDirection[5];

        if (deltaPitch >= 30)
            gameObject.transform.Translate(0, (float)(directionPitch * deltaPitch / 10 * .1f), 0, Space.Self);

        else if (deltaYaw >= 30)
            gameObject.transform.Translate((float)(directionYaw * deltaYaw / 10 * .1f), 0, 0, Space.Self);

        else if (deltaRoll > 30)
            gameObject.transform.Translate(0, 0, (float)-directionRoll * .1f, Space.Self);

    }

    void translateBallJoint(GameObject ghost, double[] deltaAndDirection)
    {

        double deltaPitch = deltaAndDirection[0];
        double directionPitch = deltaAndDirection[1];
        double deltaYaw = deltaAndDirection[2];
        double directionYaw = deltaAndDirection[3];
        double deltaRoll = deltaAndDirection[4];
        double directionRoll = deltaAndDirection[5];

        if (deltaPitch >= 30)
            ghost.transform.Translate(0, (float)(directionPitch * deltaPitch / 10 * .1f), 0, Space.Self);

        else if (deltaYaw >= 30)
            ghost.transform.Translate((float)(directionYaw * deltaYaw / 10 * .1f), 0, 0, Space.Self);

        else if (deltaRoll > 30)
            ghost.transform.Translate(0, 0, (float)-directionRoll * .1f, Space.Self);
    }

    private void rotateBallJoint(double[] deltaAndDirection)
    {
        double deltaPitch = deltaAndDirection[0];
        double directionPitch = deltaAndDirection[1];
        double deltaYaw = deltaAndDirection[2];
        double directionYaw = deltaAndDirection[3];
        double deltaRoll = deltaAndDirection[4];
        double directionRoll = deltaAndDirection[5];


        if (deltaPitch >= 30)
            gameObject.transform.Rotate(-(float)directionPitch, 0, 0, Space.Self);

        else if (deltaYaw >= 30)
            gameObject.transform.Rotate(0, (float)directionYaw, 0, Space.Self);

        else if (deltaRoll > 30)
            gameObject.transform.Rotate(0, 0, (float)directionRoll, Space.Self);
    }

    private void scaleBallJoint(double[] deltaAndDirection)
    {
        double deltaPitch = deltaAndDirection[0];
        double directionPitch = deltaAndDirection[1];
        double deltaYaw = deltaAndDirection[2];
        double directionYaw = deltaAndDirection[3];
        double deltaRoll = deltaAndDirection[4];
        double directionRoll = deltaAndDirection[5];


        if (deltaPitch >= 30)
            gameObject.transform.localScale += new Vector3(0, (float)directionPitch * 0.01f, 0);

        else if (deltaYaw >= 30)
            gameObject.transform.localScale += new Vector3((float)directionYaw * 0.01f, 0, 0);

        else if (deltaRoll > 30)
            gameObject.transform.localScale += new Vector3(0, 0, (float)directionRoll * 0.01f);
    }

    void translateLikeGyro(Quaternion aQuat)
    {
        Quaternion rotationDiff = lastRotationInv * aQuat;
        lastRotationInv = Quaternion.Inverse(aQuat);

        Vector3 rotPosNeg = RotAsPosNeg(rotationDiff);


        Vector3 pointerLocation = transform.position;

        pointerLocation.x += MultiplierX * CDFunction(rotPosNeg.y);
        pointerLocation.y += MultiplierY * CDFunction(rotPosNeg.x * -1);



        transform.position = pointerLocation;


        Vector2 minValues = new Vector2(-Screen.width / 2, -Screen.height / 2);
        Vector2 maxValues = new Vector2(Screen.width / 2, Screen.height / 2);

        //Vector3 boundedPointerLocation = transform.position;

        //boundedPointerLocation.x = Mathf.Max(minValues.x, Mathf.Min(maxValues.x, pointerLocation.x));
        //boundedPointerLocation.y = Mathf.Max(minValues.y, Mathf.Min(maxValues.y, pointerLocation.y));

    }

    static Vector3 RotAsPosNeg(Quaternion rotationT)
    {
        Vector3 diffRotVec3 = new Vector3();

        diffRotVec3.x = rotationT.eulerAngles.x;
        if (diffRotVec3.x > 180f)
            diffRotVec3.x = -1 * (360 - diffRotVec3.x);
        diffRotVec3.y = rotationT.eulerAngles.y;
        if (diffRotVec3.y > 180f)
            diffRotVec3.y = -1 * (360 - diffRotVec3.y);
        diffRotVec3.z = rotationT.eulerAngles.z;
        if (diffRotVec3.z > 180f)
            diffRotVec3.z = -1 * (360 - diffRotVec3.z);

        return diffRotVec3;
    }

    float CDFunction(float angleDiff)
    {
        float sign = Mathf.Sign(angleDiff);
        float val = Mathf.Abs(angleDiff);
        float cdCorrectedVal = Mathf.Atan(val * Mathf.Deg2Rad - IMUNoiseFactor);

        return sign * Mathf.Max(0f, cdCorrectedVal);
    }


    public void GetIMUVals(Quaternion quat, float[] angles)
    {

    }
    public void Translate(Quaternion quat, float[] initAngles, float[] currAngles)
    {

    }
    public void Rotate(Quaternion quat, float[] initAngles, float[] currAngles)
    {

    }

    public void Scale(Quaternion quat, float[] initAngles, float[] currAngles)
    {

    }

    public void Reset(Quaternion quat, float[] initAngles, float[] currAngles)
    {

    }
}
