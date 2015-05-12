using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UnityEngine;

namespace UnityMoverioBT200.Scripts.Controllers
{

  public abstract class MoverioController : MonoBehaviour
  {

    public bool ShowGUI;

    public ControllerSettings Settings
    {
      set;
      protected get;
    }

    protected bool runLocal = true;

    public bool RunLocal 
    {
      get { return runLocal && !Network.isServer; }
      set 
      { 
        runLocal = value;
        OnRunLocal();
      }
    }

    virtual public void OnRunLocal() { }

    public bool IsCurrentController 
    {
      get { return Settings.Controller == this; }
    }

  }

}
