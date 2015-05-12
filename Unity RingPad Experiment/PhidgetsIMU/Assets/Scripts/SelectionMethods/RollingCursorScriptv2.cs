using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.UI;
using System.Linq;

public class RollingCursorScriptv2 : MonoBehaviour, IControllable
{
    public GameObject placeHolder;
    public Text aText;
    public List<GameObject> moreObjects;
    public Camera cam;

    private IControllable activeObject;
    private ITransformable transfObject;
    private List<GameObject> objects;

    private int highlighted;
    private int currTask;
    private int totalCount;

    private bool selected;
    private bool toggleTransform;
    private bool confirmNewInstance;

    private BitArray taskBools;

    private Quaternion[] initQuats;
    private Quaternion[] currQuats;

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
        highlighted = -1;

        foreach (GameObject anObject in moreObjects)
        {
            addObject(anObject);
        }

        //addObject();
        taskBools = new BitArray((int)Tasks.TOTAL_TASKS, false);
        currTask = 0;
        initQuats = null;
        currQuats = null;
        initAngles = null;
        currAngles = null;
        confirmNewInstance = false;
        if (cam == null)
            cam = Camera.main;
    }

    public void SwipeUp()
    {
        if (selected)
            activeObject.SwipeUp();
    }
    public void SwipeDown()
    {
        if (selected)
            activeObject.SwipeDown();
    }
    public void SwipeLeft()
    {
        if (!selected)
        {
            transfObject = (ITransformable)objects[highlighted].GetComponent(typeof(ITransformable));
            transfObject.Deselected();
            if (highlighted > 0)
                highlighted--;
        }
        else
        {
            activeObject.SwipeLeft();
        }
    }
    public void SwipeRight()
    {
        if (!selected)
        {
            transfObject = (ITransformable)objects[highlighted].GetComponent(typeof(ITransformable));
            transfObject.Deselected();
            if (highlighted < totalCount - 1)
                highlighted++;
        }
        else
        {
            activeObject.SwipeRight();
        }
    }
    public void SingleClick()
    {
        if (!selected)
        {
        }
        else
            activeObject.SingleClick();
    }
    public void DoubleClick()
    {
       selected = selected ? false : true;
       if (selected)
          activeObject.DoubleClick();  
    }
    public void GetIMUVals(Quaternion quat, float[] angles)
    {

    }

    public void GetIMUVals(Quaternion[] quats, float[] angles)
    {

        if (!selected)
        {
            if (!objects[highlighted].renderer.isVisible)
            {
                int i = 0;
                foreach (GameObject anObject in objects)
                {
                    if (anObject.renderer.isVisible)
                    {
                        highlighted = i;
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
                transfObject = (ITransformable)anObject.GetComponent(typeof(ITransformable));
                transfObject.Deselected();
                anObject.transform.LookAt(cam.transform.position, Vector3.up);
                anObject.transform.Rotate(new Vector3(0, 1, 0), 180);
            }

        }

        else
        {
            currQuats = quats;
            currAngles = angles;

            if (initQuats != null)
            {
                Quaternion[] allQuats = new Quaternion[6];
                float[] allAngles = new float[6];
                for(int i = 0; i < 3; i++)
                {
                    allQuats[i] = initQuats[i];
                    allQuats[i + 3] = currQuats[i];

                    allAngles[i] = initAngles[i];
                    allAngles[i + 3] = currAngles[i];
                }
                //if(activeObject != null)
                    //activeObject.GetIMUVals(allQuats, allAngles);
            }
        }

        if (highlighted > -1)
        {
            transfObject = (ITransformable)objects[highlighted].GetComponent(typeof(ITransformable));
            transfObject.Selected();
            activeObject = (IControllable)objects[highlighted].GetComponent(typeof(IControllable));
        }
    }

    public void CCTwist()
    {
        if (!selected)
        {

        }
        else
            activeObject.CCTwist();
    }
    public void CTwist()
    {
        if (!selected)
        {

        }
        else
            activeObject.CTwist();
    }

    private void addObject()
    {
        if (highlighted > -1)
            objects[highlighted].GetComponent<WindowScript>().Deselected();

        GameObject childObject = Instantiate(placeHolder) as GameObject;
        childObject.transform.parent = transform;
        objects.Add(childObject);
        totalCount++;
        highlighted = totalCount - 1;
    }

    private void addObject(GameObject childObject)
    {
        if (highlighted > -1)
        {
            transfObject = (ITransformable)objects[highlighted].GetComponent(typeof(ITransformable));
            transfObject.Deselected();
        }
        childObject.transform.parent = transform;
        objects.Add(childObject);
        totalCount++;
        highlighted = totalCount - 1;
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
