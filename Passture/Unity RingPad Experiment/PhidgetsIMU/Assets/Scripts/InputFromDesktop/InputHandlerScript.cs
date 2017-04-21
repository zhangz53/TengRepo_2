using UnityEngine;
using System.Collections;

/* Grabs and stores input to be called on later.
 * 
 * To use, "getInput" should be called every frame. Handles any input that does not
 * involve the IMU. It checks for relative mouse movements and calculates to see if
 * a swipe was done. Also checks for mouse single/double clicks.
 *      - Such as in the target object's Update function.
 * When done with the current set of input, call "reset".
 *      - Such as at the end of the target object's FixedUpdate function.
 */


public class InputHandlerScript : MonoBehaviour {

    private BitArray inputBools;
    private enum inputs {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        CLICK,
        DOUBLE_CLICK,
        TOTAL_INPUTS
    }
    
    public GameObject imuHandler;
    public float clickDelta = 0.20f;

    private bool doubleClick = false;
    private bool singleClick = false;
    private float clickTime;

	void Start () {
        inputBools = new BitArray((int)inputs.TOTAL_INPUTS, false);
        swiped = false;
	}
    void Update()
    {
        //Screen.lockCursor = true;
        //getInput();
    }

    public void reset() {
        // Remove old inputs.        
        inputBools.SetAll(false);
    }

    // Send this BitArray to input receiver to process.
    public BitArray getInput(bool usingMouseInput=false) {
        // Get inputs.
        inputBools.SetAll(false);

        getClicks();
        getSwipe();

        ////Check for any input.
        //if (Input.anyKey)
        //{
        //    if (Input.GetKeyDown("1"))
        //        inputBools[(int)inputs.ONE] = true;
        //    if (Input.GetKeyDown("2"))
        //        inputBools[(int)inputs.TWO] = true;
        //    if (Input.GetKeyDown("3"))
        //        inputBools[(int)inputs.THREE] = true;
        //    inputBools[(int)inputs.ANY] = true;
        //}
        return inputBools;
    }
    public bool leftKeyDown() {
        return inputBools[(int)inputs.LEFT];
    }
    public bool rightKeyDown() {
        return inputBools[(int)inputs.RIGHT];
    }
    public bool upKeyDown() {
        return inputBools[(int)inputs.UP];
    }
    public bool downKeyDown() {
        return inputBools[(int)inputs.DOWN];
    }
    public bool clickKeyDown() {
        return inputBools[(int)inputs.CLICK];
    }
    public bool doubleClickKeyDown()
    {
        return inputBools[(int)inputs.DOUBLE_CLICK];
    }

    //public bool anyKeyDown()
    //{
    //    return inputBools[(int)inputs.ANY];
    //}

    //public bool oneKeyDown()
    //{
    //    return inputBools[(int)inputs.ONE];
    //}
    //public bool twoKeyDown()
    //{
    //    return inputBools[(int)inputs.TWO];
    //}
    //public bool threeKeyDown()
    //{
    //    return inputBools[(int)inputs.THREE];
    //}
    //public bool circleMade()
    //{
    //    return inputBools[(int)inputs.CIRCLE];
    //}
    //public bool doneTaskDown()
    //{
    //    return inputBools[(int)inputs.DONE];
    //}

    public float minSwipeLength = 1.5f;
    public float timeOut = 2.0f;
    public float timeBetweenSwipes = 1.0f;

    public Vector2 aVector2;
    bool swiped;
    float startTime, lastSwipeTime;

    
    // Calculates single/double clicks
    public void getClicks()
    {
        // Single or double click test
        if (Input.GetMouseButtonDown(0))
        {
            if (!singleClick)
            {
                singleClick = true;
                clickTime = Time.time;
                print("single click");
                inputBools[(int)inputs.CLICK] = true;
            }
            else
            {
                singleClick = false;
                //print("double click");
                inputBools[(int)inputs.DOUBLE_CLICK] = true;
                inputBools[(int)inputs.CLICK] = false;
            }

        }

        if (singleClick)
        {
            if ((Time.time - clickTime) > clickDelta)
            {
                singleClick = false;
                inputBools[(int)inputs.CLICK] = false;
            }
        }

    }

    // Calculates swipe and swipe direction
    public void getSwipe(bool usingMouseInput = true)
    {

        float currTime = Time.time;

        if (!swiped && currTime - lastSwipeTime > timeBetweenSwipes)
        {
            swiped = true;
            startTime = Time.time;
        }

        float x, y;
        if (swiped)
        {
            x = Input.GetAxis("Mouse X");
            y = Input.GetAxis("Mouse Y");

            aVector2 = new Vector2(x, y);


            if (currTime - startTime > timeOut)
            {
                swiped = false;
                return;
            }

            if (Mathf.Abs(x) >= minSwipeLength || Mathf.Abs(y) >= minSwipeLength)
            {
                lastSwipeTime = Time.time;
                swiped = false;

                if (Mathf.Abs(x) > Mathf.Abs(y))
                {
                    if (x > 0)
                    {
                        inputBools[(int)inputs.RIGHT] = true;
                        print("right");
                    }
                    else
                    {
                        print("left");
                        inputBools[(int)inputs.LEFT] = true;
                    }
                }
                else if (Mathf.Abs(x) < Mathf.Abs(y))
                {
                    if (y > 0)
                    {
                        print("up");
                        inputBools[(int)inputs.UP] = true;
                    }
                    else
                    {
                        inputBools[(int)inputs.DOWN] = true;
                        print("down");
                    }
                }

            }
        }

    }

    public float moveFactor = 10f;

    public Vector3 getAxis(bool usingMouseInput = true)
    {


        float x = Input.GetAxis("Mouse X");
        float y = Input.GetAxis("Mouse Y");

        float xDist, yDist;

        xDist = x * moveFactor;
        yDist = y * moveFactor;
        return aVector2 = new Vector2(xDist, yDist);

    }
}