using UnityEngine;
using System.Collections;

public class ReceiverScript : MonoBehaviour 
{
    /* Grabs and stores input to be called on later.
 * 
 * To use, "getInput" should be called every frame. Handles any input that does not
 * involve the IMU. It checks for relative mouse movements and calculates to see if
 * a swipe was done. Also checks for mouse single/double clicks.
 *      - Such as in the target object's Update function.
 * When done with the current set of input, call "reset".
 *      - Such as at the end of the target object's FixedUpdate function.
 */

    public GameObject inputHandler, phidgetsIMU;
    public GameObject aControllableObject;
    public bool USING_MOUSE_INPUT;
    public bool USING_GLASSES;
    public bool LOCK_CURSOR = true;

    private float twistThreshold = 90;
    private double millsToPast = 75;
    private bool trigger;

    private GameObject anObject;
    private BitArray taskBools, modeBools;
    private BitArray inputBools;
    private Quaternion quat;
    private Quaternion[] quats;
    private float[] imuAngles;

    private enum inputs
    {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        CLICK,
        DOUBLE_CLICK,
        TOTAL_INPUTS
    }

    private enum tasks
    {
        TRANS,
        ROTATE,
        SCALE,
        ANY_TASK,
        TOTAL_TASKS
    }

    private enum modes
    {
        ONE,
        TWO,
        THREE,
        TOTAL_MODES
    }

	// Use this for initialization
	void Start () 
    {
        Screen.lockCursor = LOCK_CURSOR;
        taskBools = new BitArray((int)tasks.TOTAL_TASKS, false);
        modeBools = new BitArray((int)modes.TOTAL_MODES, false);
        trigger = false;
        //setOtherModesZero((int)modes.ONE);
	}
	
	// Update is called once per frame
    // Gets input from touchpad and IMU, then calls testInput() to test for command(s) done
	void Update () 
    {
        if (!USING_GLASSES)
        {
            Screen.lockCursor = LOCK_CURSOR;
            inputBools = inputHandler.GetComponent<InputHandlerScript>().getInput(USING_MOUSE_INPUT);
            quat = phidgetsIMU.GetComponent<PhidgetsIMU>().getQuaternion();
            quats = phidgetsIMU.GetComponent<PhidgetsIMU>().getQuaternions();
            imuAngles = phidgetsIMU.GetComponent<PhidgetsIMU>().getAngles();
            testInput();
        }
	}

    // testInput: Tests the inputs and calls the appropriate command for the controllable to process.
    // controllable is the selection method's game object. have it accept the quaternion and angles, as well as
    // swipes/gestures/etc. 
    public void testInput()
    {
        anObject = aControllableObject;

        IControllable controllable = (IControllable)anObject.GetComponent(typeof(IControllable));

        controllable.GetIMUVals(quat, imuAngles);

        int twistDirection = testTwist();

        if (twistDirection == 2)
            controllable.CTwist();
        if (twistDirection == 1)
            controllable.CCTwist();
        if (inputBools[(int)inputs.DOUBLE_CLICK])
        {
            phidgetsIMU.GetComponent<PhidgetsIMU>().SetBaseline();

            controllable.DoubleClick();
        }
        if (inputBools[(int)inputs.CLICK])
            controllable.SingleClick();
        if (inputBools[(int)inputs.DOWN])
            controllable.SwipeDown();
        if (inputBools[(int)inputs.LEFT])
            controllable.SwipeLeft();
        if (inputBools[(int)inputs.RIGHT])
            controllable.SwipeRight();
        if (inputBools[(int)inputs.UP])
            controllable.SwipeUp();

        inputHandler.GetComponent<InputHandlerScript>().reset(); // Done with current inputs, flush them.
    }


    //testInput: Call this in the network input provider script to invoke the Moverio instance of the application.
    public void testInput(bool[] networkInputs, Quaternion[] networkQuats, float[] networkAngles)
    {

        anObject = aControllableObject;

        IControllable controllable = (IControllable)anObject.GetComponent(typeof(IControllable));

        controllable.GetIMUVals(quat, imuAngles);

        if (networkInputs[(int)inputs.DOUBLE_CLICK])
        {
            print("test");
            phidgetsIMU.GetComponent<PhidgetsIMU>().SetBaseline();
            controllable.DoubleClick();
        }
        if (networkInputs[(int)inputs.CLICK])
            controllable.SingleClick();
        if (networkInputs[(int)inputs.DOWN])
            controllable.SwipeDown();
        if (networkInputs[(int)inputs.LEFT])
            controllable.SwipeLeft();
        if (networkInputs[(int)inputs.RIGHT])
            controllable.SwipeRight();
        if (networkInputs[(int)inputs.UP])
            controllable.SwipeUp();

        inputHandler.GetComponent<InputHandlerScript>().reset(); // Done with current inputs, flush them.
    }

    int testTwist()
    {
        int returnVal = 0;
        float[] currAngles = phidgetsIMU.GetComponent<PhidgetsIMU>().getAngles();

        float[] historicAngles = phidgetsIMU.GetComponent<PhidgetsIMU>().GetHistoricAngles(millsToPast);

        float diffAngles = Mathf.Abs(Mathf.Abs(currAngles[2]) - Mathf.Abs(historicAngles[2]));

        float startRoll = (historicAngles[2]);
        float currRoll = (currAngles[2]);

        bool clockwise = ((startRoll > 0 && (currRoll > 0 && currRoll > startRoll) || currRoll < 0) || startRoll < 0 && (currRoll > 0 || (currRoll < startRoll) && currRoll < 0)) ? true : false;
        clockwise = (startRoll > 0 && ((currRoll < 0) || ((currRoll > 0) && currRoll > startRoll)) || ((startRoll < 0) && (currRoll < 0) && currRoll > startRoll)) ? true : false;


        // IMU will jump to values over 180 and under -180 with sudden movements. Can cause logic to read twist in opposite direction. DO SLOW TWISTS
        if (currRoll < -180)
            clockwise = true;
        else if (currRoll > 180)
            clockwise = false;

        if (!trigger)
        {
            trigger = diffAngles > twistThreshold;
            if (trigger)
            {
                if (clockwise)
                {
                    returnVal = 1;
                }
                else
                {
                    returnVal = 2;
                }
            }
        }
        else
        {
            if (Input.GetMouseButtonDown(0))
                trigger = false;
        }

        return returnVal;
    }

    public float[] getAngles()
    {
        imuAngles = phidgetsIMU.GetComponent<PhidgetsIMU>().getAngles();

        return imuAngles;
    }

    public Quaternion getQuat()
    {
        return phidgetsIMU.GetComponent<PhidgetsIMU>().getQuaternion();
    }

    public Quaternion[] getQuats()
    {
        quats = phidgetsIMU.GetComponent<PhidgetsIMU>().getQuaternions();

        return quats;
    }

    public BitArray getBits()
    {
        inputBools = inputHandler.GetComponent<InputHandlerScript>().getInput(USING_MOUSE_INPUT);

        return inputBools;
    }

}
