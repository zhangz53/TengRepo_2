using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BallMoveSimulation : MonoBehaviour {

	//use keyboard event to control the position of the ball object
	//the center is 0,0,0 in the world coordinate


	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		transform.RotateAround(Vector3.zero, Vector3.up, 20 * Time.deltaTime);	
	}
}
