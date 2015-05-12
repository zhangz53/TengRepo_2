using UnityEngine;
using System.Collections;
using UnityMoverioBT200.Scripts.Controllers;

namespace UnityMoverioBT200.Scripts.Scenes
{
  public class DemoMenu : MonoBehaviour
  {

    void OnGUI()
    {
      GUILayout.BeginArea(new Rect(380, 200, 200, 200));
      if (GUILayout.Button("Selection Techniques", GUILayout.Width(200), GUILayout.Height(50)))
      {
        Application.LoadLevel(1);
        DestroyObject(this);
      }
      if (GUILayout.Button("Disambiguation", GUILayout.Width(200), GUILayout.Height(50)))
      {
        Application.LoadLevel(2); 
        DestroyObject(this);
      }
      GUILayout.EndArea();
    }
  }

}