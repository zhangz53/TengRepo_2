using UnityEngine;
using System.Collections;
using System;
using UnityMoverioBT200.Scripts.Util;

namespace UnityMoverioBT200.Scripts.Providers
{

  public class NetworkProvider : MonoBehaviour
  {

    public bool AutoStartServer = false;
    public string IPofDesktopServer = string.Empty;
    public bool isConnected;

    private static NetworkProvider instance;
    public static NetworkProvider Instance
    {
      get
      {
        if (instance == null)
          instance = new NetworkProvider();
        return instance;
      }
    }

    public bool ShowGUI;

    public NetworkProvider()
    {
      instance = this;
      instance.ShowGUI = false;
      instance.isConnected = false;
    }

    ~NetworkProvider()
    {
      Debug.Log("Destroying the NetworkProvider");
    }

    // Use this for initialization
    void Start()
    {
      if (AutoStartServer)
      {
        if (SystemInfo.deviceType == DeviceType.Desktop)
          StartServer();
      }
    }

    void OnGUI()
    {
      if (!ShowGUI)
        return;

      GUILayout.Space(240);
      if (!isConnected)
      {
        if (SystemInfo.deviceType == DeviceType.Handheld)
        {
          if (GUILayout.Button("Android Client", GUILayout.Width(100), GUILayout.Height(30)))
            StartClient();
        }
        else
        {
          if (GUILayout.Button("Desktop Server", GUILayout.Width(100), GUILayout.Height(30)))
            StartServer();
        }
      }
      else if (Network.isServer)
      {
        GUILayout.Label(Network.player.ipAddress, GUILayout.Width(100), GUILayout.Height(50));
      }
      else if (Network.isClient)
      {
        GUILayout.Label("Connected to Server", GUILayout.Width(100), GUILayout.Height(50));
      }
    }

    private void StartClient()
    {
      Network.Connect(IPofDesktopServer, 8080);
      isConnected = true;

      //calls the WindowsViconConnector.OnNetworkStarted method
      MessageBroker.BroadcastAll("OnNetworkStarted", false);
    }

    private void StartServer()
    {
      Network.InitializeServer(2, 8080, true);
      isConnected = true;

      //calls the WindowsViconConnector.OnNetworkStarted method
      MessageBroker.BroadcastAll("OnNetworkStarted", true);
    }

    void OnDisconnectedFromServer(NetworkDisconnection info)
    {
      if (Network.isServer)
        Debug.Log("Local server connection disconnected");
      else
        if (info == NetworkDisconnection.LostConnection)
          Debug.Log("Lost connection to the server");
        else
          Debug.Log("Successfully diconnected from the server");

      isConnected = false;
    }
  }
}