using UnityEngine;
using System.Collections;
using UnityMoverioBT200.Scripts.Controllers;
using UnityMoverioBT200.Scripts.Providers;
using UnityMoverioBT200.Scripts.Util;

namespace UnityMoverioBT200.Scripts.Scenes
{

  public class PhoneIconsSceneUI : MonoBehaviour
  {

    void Start()
    {
      CommToAndroid.Instance.ShowGUI = true;
      NetworkProvider.Instance.ShowGUI = true;
      RotationProvider.Instance.ShowGUI = true;
    }
  }

}