using UnityEngine;
using System.Collections;

namespace UnityMoverioBT200.Scripts.Providers
{

  // Move an object with a NetworkedKeyboard.
  // Forward/back/left/right are determined by where the given camera is facing.
  public class NetworkKeyboardProvider : MonoBehaviour
  {

    public GameObject objectToMove;
    public Camera cam;                      // Camera to move in reference to.
    public bool enableNetworkKeyboard = true;
    public float speed = 1.0f;

    void Awake()
    {
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
      if (enableNetworkKeyboard)
      {
        if (SystemInfo.deviceType == DeviceType.Handheld)
          return;

        float horizontal = Input.GetAxis("Horizontal");
        float vertical = Input.GetAxis("Vertical");

        if (Network.isClient)
          networkView.RPC("SynchInput", RPCMode.Others, horizontal, vertical);
      }
    }

    [RPC]
    void SynchInput(float h, float v)
    {
      // Update X
      Vector3 direction = cam.transform.right.normalized;
      Vector3 displacement = h * direction * speed * Time.deltaTime;
      objectToMove.transform.position += displacement;

      // Update Y
      direction = cam.transform.forward.normalized;
      displacement = v * direction * speed * Time.deltaTime;
      objectToMove.transform.position += displacement;
    }
  }
}