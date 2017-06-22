using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System;

public class PlaneMoveSimulation : MonoBehaviour {

	//simulate a plane departing
	Vector3 planePosition;	
	float acceleratinRate = 1.0f;
	float velocity = 0.0f;
	double takeoffRotation = 10.0 * 3.14 / 180 ;

	// Use this for initialization
	void Start () {

	}
	
	// Update is called once per frame
	void Update () {
		planePosition = transform.position;	
		//print(planePosition.x);
		
		velocity += Time.deltaTime * acceleratinRate;

		if(planePosition.x > 0)
		{
			//aircraft rolling
			transform.Translate(-1.0f * velocity, 0, 0, Space.Self);
		}else
		{
			//aircraft taking off
			transform.Translate(-1.0f * velocity * (float)Math.Cos(takeoffRotation), 1.0f * velocity * (float)Math.Sin(takeoffRotation), 0, Space.Self);
		}
		
	}
}
