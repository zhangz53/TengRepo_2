using UnityEngine;
using System.Collections;

namespace UnityMoverioBT200.Scripts.Util
{

  public class AnchorToHeadHorizontalDirection : MonoBehaviour
  {

    private Vector3 initialLocalPosition = Vector3.zero;

    void Start()
    {
      initialLocalPosition = transform.TransformVector(transform.localPosition);

    }

    // Update is called once per frame
    void Update()
    {
      if (transform.parent == null)
        return;

      //Gets the direction of the parent transform and extracts the components in XZ
      transform.rotation = Quaternion.Euler(0, transform.parent.rotation.eulerAngles.y, 0);
    }

  }

}