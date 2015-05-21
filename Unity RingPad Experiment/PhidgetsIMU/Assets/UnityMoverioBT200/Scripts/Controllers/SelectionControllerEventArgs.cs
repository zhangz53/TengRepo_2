// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by a tool.
//      Mono Runtime Version: 4.0.30319.1
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------
using System;
using UnityEngine;
using UnityMoverioBT200.Scripts.Providers;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public class SelectionControllerEventArgs : EventArgs
  {
    public System.DateTime EventTime;
    public ControllerType Device;
    public bool IsConflict = false;
    public bool IsConflictSolution = false;
    public int NrOfConflictedTargets = -1;
    public int IndexOfConflictSolution = -1;

    public MoverioTouchpadEventArgs MoverioEvent;

    public Vector3 PointerPx;
    public Vector3 PointerPos;
    public Quaternion PointerQuat;

    public String Tag;

    public SelectionControllerEventArgs(MoverioTouchpadEventArgs mieArgs)
    {
      EventTime = System.DateTime.Now;
      Device = ControllerType.TouchPad;
      MoverioEvent = mieArgs;

      PointerPx = Vector3.zero;
      PointerPos = Vector3.zero;
      PointerQuat = Quaternion.Euler(0, 0, 0);

      Tag = String.Empty;

      if (MoverioEvent != null)
        PointerPx = MoverioEvent.Type == MoverioTouchpadEventType.TouchStarted ? MoverioEvent.Origin : MoverioEvent.Last;
    }

    public TimeSpan TimeEllapsed()
    {
      return System.DateTime.Now - EventTime;
    }

  }


}