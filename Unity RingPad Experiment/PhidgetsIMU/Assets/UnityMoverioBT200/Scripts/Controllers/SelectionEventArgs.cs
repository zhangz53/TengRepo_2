using UnityEngine;
using System.Collections;
using System;
using UnityMoverioBT200.Scripts.Controllers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class SelectionEventArgs : EventArgs
  {
    public enum SelectionEventType { Hovered, Unhovered, Selected };

    public SelectionEventType Type;
    public System.DateTime Time;
    public GameObject Target;
    //public bool Conflict;

    public SelectionControllerEventArgs ControllerEvent;

    public SelectionEventArgs(SelectionControllerEventArgs cEvent)
    {
      Time = System.DateTime.Now;
      ControllerEvent = cEvent;
    }
  }

}