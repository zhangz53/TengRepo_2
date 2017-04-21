using UnityEngine;
using System.Collections;
using UnityMoverioBT200.Scripts.Controllers;
using UnityMoverioBT200.Scripts.Util;

namespace UnityMoverioBT200.Scripts.Util
{
  public class Target : MonoBehaviour
  {

    public enum TargetStateType
    {
      Normal,
      HoveredConflicted,
      HoveredSolution
    }

    public string ColorField = "_Color";

    public Color BaseColor = Color.gray;
    public Color ConflictedColor = new Color32(0, 171, 169, 255); //more than one object in the scene is highlighted
    public Color HoveredColor = new Color32(0, 80, 239, 255);
    public Color SelectedColor = new Color32(170, 0, 255, 255);
    public int SelectionFeedbackMillis = 100; //milliseconds

    public Color HighlightedColor = Color.red;

    private TargetStateType State = TargetStateType.Normal;

    private bool highlighted = false;
    public bool Highlighted
    {
      get { return highlighted; }
      set
      {
        all_SynchState(State.ToString(), value);
        if (Network.isServer)
          networkView.RPC("all_SynchState", RPCMode.Others, State.ToString(), value);
      }
    }

    void Awake()
    {
      all_SynchState(State.ToString(), Highlighted);
      if (Network.isServer)
        networkView.RPC("all_SynchState", RPCMode.Others, State.ToString(), Highlighted);
    }

    [RPC]
    void all_SynchState(string state, bool hl)
    {
      State = (TargetStateType)System.Enum.Parse(typeof(TargetStateType), state);
      highlighted = hl;

      if (inFeedback)
        return;

      Color additional = Color.black;
      if (highlighted)
        additional = HighlightedColor;

      switch (State)
      {
        case TargetStateType.HoveredSolution:
          renderer.material.SetColor(ColorField, HoveredColor + additional);
          break;
        case TargetStateType.HoveredConflicted:
          renderer.material.SetColor(ColorField, ConflictedColor + additional);
          break;
        case TargetStateType.Normal:
        default:
          renderer.material.SetColor(ColorField, Highlighted ? HighlightedColor : BaseColor);
          break;
      }
    }

    [RPC]
    void all_TriggerSelection()
    {
      StartCoroutine(SelectionFeedback());
    }

    private bool inFeedback = false;

    IEnumerator SelectionFeedback()
    {
      inFeedback = true;
      renderer.material.SetColor(ColorField, SelectedColor);

      yield return new WaitForSeconds(SelectionFeedbackMillis / 1000.0f);

      inFeedback = false;
      all_SynchState(State.ToString(), Highlighted);
    }

    void Hovered(SelectionControllerEventArgs args)
    {
      TargetStateType prevState = State;

      TargetStateType newState = TargetStateType.HoveredSolution;
      if (args.IsConflict)
        newState = TargetStateType.HoveredConflicted;

      if (args.IsConflictSolution)
        newState = TargetStateType.HoveredSolution;

      if (newState != prevState)
      {
        all_SynchState(newState.ToString(), Highlighted);
        if (Network.isClient)
          networkView.RPC("all_SynchState", RPCMode.Others, newState.ToString(), Highlighted);

        //only client and standalone
        if (!Network.isServer)
        {
          if (prevState != TargetStateType.HoveredSolution && prevState != TargetStateType.HoveredConflicted)
            NotifyEventListeners(SelectionEventArgs.SelectionEventType.Hovered, args);
        }
      }
    }

    void NotHovered(SelectionControllerEventArgs args)
    {
      TargetStateType newState = TargetStateType.Normal;

      all_SynchState(newState.ToString(), Highlighted);
      if (Network.isClient)
        networkView.RPC("all_SynchState", RPCMode.Others, newState.ToString(), Highlighted);

      //only client and standalone
      if (!Network.isServer)
        NotifyEventListeners(SelectionEventArgs.SelectionEventType.Unhovered, args);
    }

    void Selected(SelectionControllerEventArgs args)
    {
      all_TriggerSelection();
      if (Network.isClient)
        networkView.RPC("all_TriggerSelection", RPCMode.Others);

      //only client and standalone
      if (!Network.isServer)
        NotifyEventListeners(SelectionEventArgs.SelectionEventType.Selected, args);
    }

    void NotifyEventListeners(SelectionEventArgs.SelectionEventType eventType, SelectionControllerEventArgs controllerArgs)
    {
      SelectionEventArgs args = new SelectionEventArgs(controllerArgs);
      args.Type = eventType;
      args.Target = gameObject;

      MessageBroker.BroadcastAll("On" + args.Type.ToString(), args);
    }

  }

}