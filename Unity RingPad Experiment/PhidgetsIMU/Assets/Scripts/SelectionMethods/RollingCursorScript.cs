using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.UI;
using System.Linq;

public class RollingCursorScript : MonoBehaviour, IControllable
{
    public GameObject placeHolder;
    public  Text aText;
    public List<GameObject> moreObjects;
    public Camera cam;

    private List<GameObject> objects;

    private int selected;
    private int currTask;
    private int totalCount;
    private bool toggleTransform;
    private bool confirmNewInstance;
    private BitArray taskBools;
    private Quaternion initQuat;
    private Quaternion currQuat;
    private float[] initAngles;
    private float[] currAngles;

    public Collider aCollider;

    private enum Tasks
    {
        TRANSLATE,
        ROTATE,
        SCALE,
        TOTAL_TASKS
    }

    void Start()
    {
        objects = new List<GameObject>();
        totalCount = 0;
        selected = -1;

        foreach(GameObject anObject in moreObjects)
        {
            addObject(anObject);
        }
 
        //addObject();
        taskBools = new BitArray((int)Tasks.TOTAL_TASKS, false);
        currTask = 0;
        initQuat = Quaternion.identity;
        currQuat = Quaternion.identity;
        initAngles = null;
        currAngles = null;
        confirmNewInstance = false;
        if (cam == null)
            cam = Camera.main;
    }

    public void SwipeUp()
    {
        confirmNewInstance = false;
    }
    public void SwipeDown()
    {
        if (!toggleTransform)
        {
            if (confirmNewInstance)
            {
                addObject();
                confirmNewInstance = false;
            }
            else if (!confirmNewInstance)
                confirmNewInstance = true;
            
        }
    }
    public void SwipeLeft()
    {
        if(!toggleTransform)
        {
            objects[selected].GetComponent<WindowScript>().Deselected();
            if (selected > 0)
                selected--;
        }
        else
        {
            taskBools[currTask] = false;
            if (currTask > 0)
            {
                currTask--;
            }
            else
            {
                currTask = 2;
            }
            taskBools[currTask] = true;
        }
        confirmNewInstance = false;
    }
    public void SwipeRight()
    {
        if (!toggleTransform)
        {
            objects[selected].GetComponent<WindowScript>().Deselected();
            if (selected < totalCount - 1)
                selected++;
        }
        else
        {
            taskBools[currTask] = false;
            if (currTask < 2)
            {
                currTask++;
            }
            else
            {
                currTask = 0;
            }
            taskBools[currTask] = true;
        }
        confirmNewInstance = false;
    }
    public void SingleClick()
    {
        if(toggleTransform)
        {
            objects[selected].GetComponent<WindowScript>().confirmTrans = true;
        }
    }
    public void DoubleClick()
    {
        //toggleTransform = toggleTransform ? false : true;
        if (toggleTransform)
            toggleTransform = false;
        else
        {
            toggleTransform = true;
            taskBools[(int)Tasks.TRANSLATE] = true;
            currTask = 0;
        }
        confirmNewInstance = false;
        objects[selected].GetComponent<WindowScript>().confirmTrans = false;

    }

    public void CCTwist()
    {
        print("Twisted CC");
    }
    public void CTwist()
    {
        print("Twisted C");
    }

    public void GetIMUVals(Quaternion quat, float[] angles)
    {

        if (!toggleTransform)
        {
            if (!objects[selected].renderer.isVisible)
            {
                int i = 0;
                foreach (GameObject anObject in objects)
                {
                    if (anObject.renderer.isVisible)
                    {
                        selected = i;
                        break;
                    }
                    i++;
                }
            }

            initQuat = quat;
            initAngles = angles;
            changeText("Selection");

            List<GameObject> SortedList = objects.OrderBy(o => o.transform.position.x).ToList<GameObject>();

            SortedList = sortObjectsOnScreen();

            objects = SortedList;
            foreach (GameObject anObject in objects)
            {
                anObject.GetComponent<WindowScript>().Deselected();
                anObject.transform.LookAt(cam.transform.position, Vector3.up);
                anObject.transform.Rotate(new Vector3(0, 1, 0), 180);
            }

        }

        else
        {
            currQuat = quat;
            currAngles = angles;

            if (initQuat != null)
            {
                if (taskBools[(int)Tasks.TRANSLATE])
                {
                    objects[selected].GetComponent<WindowScript>().Translate(currQuat, initAngles, currAngles);
                    changeText("Translate");
                }
                else if (taskBools[(int)Tasks.ROTATE])
                {
                    objects[selected].GetComponent<WindowScript>().Rotate(currQuat, initAngles, currAngles);
                    changeText("Rotate");
                }
                else if (taskBools[(int)Tasks.SCALE])
                {
                    objects[selected].GetComponent<WindowScript>().Scale(currQuat, initAngles, currAngles);
                    changeText("Scale");
                }
            }
        }

        if (selected > -1)
            objects[selected].GetComponent<WindowScript>().Selected();
    }

    /*public void GetIMUVals(Quaternion[] quats, float[] angles)
    {

        if(!toggleTransform)
        {
            if(!objects[selected].renderer.isVisible)
            //if(!aCollider.bounds.Contains(objects[selected].transform.position))
            {
                int i = 0;
                foreach (GameObject anObject in objects)
                {
                    //if(aCollider.bounds.Contains(anObject.transform.position))
                    if (anObject.renderer.isVisible)
                    {
                        selected = i;
                        break;
                    }
                    i++;
                }
            }

            initQuats = quats;
            initAngles = angles;
            changeText("Selection");

            List<GameObject> SortedList = objects.OrderBy(o => o.transform.position.x).ToList<GameObject>();

            SortedList = sortObjectsOnScreen();

            objects = SortedList;
            foreach (GameObject anObject in objects)
            {
                anObject.GetComponent<WindowScript>().Deselected();
                anObject.transform.LookAt(cam.transform.position, Vector3.up);
                anObject.transform.Rotate(new Vector3(0, 1, 0), 180);
            }

        }

        else
        {
            currQuats = quats;
            currAngles = angles;

            if(initQuats != null)
            {
                if(taskBools[(int)Tasks.TRANSLATE])
                {
                    objects[selected].GetComponent<WindowScript>().Translate(currQuats, initAngles, currAngles);
                    changeText("Translate");
                }
                else if (taskBools[(int)Tasks.ROTATE])
                {
                    objects[selected].GetComponent<WindowScript>().Rotate(currQuats, initAngles, currAngles);
                    changeText("Rotate");
                }
                else if (taskBools[(int)Tasks.SCALE])
                {
                    objects[selected].GetComponent<WindowScript>().Scale(currQuats, initAngles, currAngles);
                    changeText("Scale");
                }
            }
        }

        if (selected > -1)
            objects[selected].GetComponent<WindowScript>().Selected();
    }
    */


    private void addObject()
    {
        if (selected > -1)
            objects[selected].GetComponent<WindowScript>().Deselected();

        GameObject childObject = Instantiate(placeHolder) as GameObject;
        childObject.transform.parent = transform;
        objects.Add(childObject);
        totalCount++;
        selected = totalCount - 1;
    }

    private void addObject(GameObject childObject)
    {
        if (selected > -1)
            objects[selected].GetComponent<WindowScript>().Deselected();

        childObject.transform.parent = transform;
        objects.Add(childObject);
        totalCount++;
        selected = totalCount - 1;
    }

    private void changeText(string text)
    {
        aText.text = text;
    }

    private List<GameObject> sortObjectsOnScreen()
    {
        List<GameObject> SortedList = objects.OrderBy(o => cam.WorldToScreenPoint(o.transform.position).x).ToList<GameObject>();
        return SortedList;
    }
}
