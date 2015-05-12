using UnityEngine;
using System;
using System.Collections;
using UnityMoverioBT200.Scripts.Util;

namespace UnityMoverioBT200.Scripts.Providers
{

  public class NetworkMouseProviderScript : MonoBehaviour
  {
    public Camera cam;                      // Camera to move in reference to.
    public bool enableNetworkMouse = true;

    private System.DateTime timeLastReset;

    private static NetworkMouseProviderScript instance;
    public static NetworkMouseProviderScript Instance
    {
      get
      {
        if (instance == null)
          instance = new NetworkMouseProviderScript();
        return instance;
      }
    }
    public NetworkMouseProviderScript()
    {
      instance = this;
    }

    ~NetworkMouseProviderScript()
    {
      Debug.Log("Destroying the MoverioInputProvider");
    }

    // Use this for initialization
    void Awake()
    {
      if (enableNetworkMouse)
      {
        if (cam == null)
        {
          Debug.Log("Warning: no camera slected. Searching for Main Camera...");
          cam = GameObject.FindWithTag("MainCamera").camera;
          if (cam == null)
          {
            enableNetworkMouse = false;
            Debug.Log("Error: could not find Camera. Disabling Network Keyboard.");
          }
          else
          {
            Debug.Log("Main Camera found.");
          }
        }
      }

      if (!enableNetworkMouse)
        Debug.Log("Network Mouse is disabled.");
    }

    bool isFingerDown = false;
    System.DateTime timeTouchOrigin, timeTouchLastMove;
    Vector3 touchOrigin, touchLastPosition;

    // Update is called once per frame
    void FixedUpdate()
    {
      if (enableNetworkMouse)
      {
        // Get input from mouse to pass for later
        Vector3 mousePosition = Input.mousePosition;
        float x, y;
        x = Input.GetAxis("Mouse X");
        y = Input.GetAxis("Mouse Y");

        int mouseDown = 0;

        if (Input.GetMouseButtonDown(0) && !isFingerDown)
        {
          isFingerDown = true;
          mouseDown = 1;
        }
        if (Input.GetMouseButton(0))
          mouseDown = 1;
        if (Input.GetMouseButtonUp(0))
        {
          mouseDown = 0;
          isFingerDown = false;
        }

        bool isMouseDown = mouseDown != 0;

        // Calls all SyncMouseInput in other game objects to receive desktop mouse input
        if (Network.isClient || Network.isServer)
          networkView.RPC("SyncMouseInput", RPCMode.OthersBuffered, mousePosition, mouseDown, x, y);
      }

    }

    [RPC]
    void SyncMouseInput(Vector3 mousePosition, int mouseDownVal, float x, float y)
    {

    }
  }


}
