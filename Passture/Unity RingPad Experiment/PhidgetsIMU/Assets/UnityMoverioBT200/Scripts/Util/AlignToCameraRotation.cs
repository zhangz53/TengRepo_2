﻿using UnityEngine;
using System.Collections;

namespace UnityMoverioBT200.Scripts.Util
{

  // Turn the attached object to face the same direction as the camera.
  // Hint: Use with directional lights to have the lightsource point where the camera is looking.
  public class AlignToCameraRotation : MonoBehaviour
  {
    public Camera cam;
    public bool isEnabled = true;
    void Awake()
    {
      if (!isEnabled)
      {
        Debug.Log("AlignToCameraRotation is disabled.");
      }
      else if (cam == null)
      {
        Debug.Log("Warning: camera not selected. Searching for Main Camera...");
        cam = GameObject.FindWithTag("MainCamera").camera;
        if (cam == null)
        {
          isEnabled = false;
          Debug.Log("Error: Main Camera not found. AlignToCameraRotation disabled.");
        }
        else
        {
          Debug.Log("Main Camera found.");
        }
      }
    }
    void Update()
    {
      if (isEnabled)
      {
        transform.rotation = cam.transform.rotation;
      }
    }
  }
}

