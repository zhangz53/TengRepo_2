using UnityEngine;
using System.Collections;

public class NetworkInputProviderScript : MonoBehaviour {

    public GameObject inputHandler, phidgetsIMU;
    public GameObject objectToMove;
    public GameObject inputReceiver;
    public GameObject selectionMethods;
    public Camera cam;                      // Camera to move in reference to.
    public bool enableNetworkKeyboard = true;
    public float speed = 1.0f;

    void Awake()
    {
        Screen.lockCursor = true;

        if (enableNetworkKeyboard)
        {

            if (objectToMove == null)
            {
                objectToMove = this.gameObject;
                Debug.Log("Warning: no object to move was selected. Defaulting to self.");
            }
            if (cam == null)
            {
                Debug.Log("Warning: no camera slected. Searching for Main Camera...");
                cam = GameObject.FindWithTag("MainCamera").camera;
                if (cam == null)
                {
                    enableNetworkKeyboard = false;
                    Debug.Log("Error: could not find Camera. Disabling Network Keyboard.");
                }
                else
                {
                    Debug.Log("Main Camera found.");
                }
            }

        }

        if (!enableNetworkKeyboard)
            Debug.Log("Network Keyboard is disabled.");
    }
    void Update()
    {
        Screen.lockCursor = true;

        if (enableNetworkKeyboard)
        {
            if (SystemInfo.deviceType == DeviceType.Handheld)
                return;

            BitArray inputBools = inputReceiver.GetComponent<ReceiverScript>().getBits();
            Quaternion[] quats = inputReceiver.GetComponent<ReceiverScript>().getQuats();
            float[] angles = inputReceiver.GetComponent<ReceiverScript>().getAngles();


            bool[] inputBoolArray = new bool[inputBools.Length];
            for(int i = 0; i < inputBools.Length; i++)
            {
                inputBoolArray[i] = inputBools[i];
            }

            inputReceiver.GetComponent<ReceiverScript>().testInput(inputBoolArray, quats, angles);


            if (Network.isClient)
            {
                networkView.RPC("SynchInput", RPCMode.Others, inputBoolArray, quats, angles);
            }
        }
    }

    [RPC]
    void SynchInput(bool[] inputBools, Quaternion[] quats, float[] angles)
    {
        inputReceiver.GetComponent<ReceiverScript>().testInput(inputBools, quats, angles);
    }

 
}

