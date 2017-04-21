using UnityEngine;
using System.Collections;

namespace UnityMoverioBT200.Scripts.Util {

    // With inputFromAbsoluteMousePositoin enabled:
    // Uses the position of the mouse on the Moverio touchpad to move the Moverio (or attached object).
    // Moving the mouse further to the edges of the screen will increase movement speed.

    // With inputFromMouseDrag enabled:
    // Moves the Moverio (or attached object) by clicking, then holding and dragging in the desired direction of movement.
    // Dragging a away from where you clicked will accellerate until max speed is reached at dragDistance from the starting point.
    public class MoverioMover : MonoBehaviour {

        public bool inputFromAbsoluteMousePosition = true;
        public bool inputFromMouseDrag = true;

        private Camera moverioCamera;                // On EPSON Moverio BT-200 game object.

        public float horizontalMinSpeed = 0.01f;    // Minimum hoirzontal movement speed (only used with abs mouse position).
        public float horizontalMaxSpeed = 1.0f;     // Maximum hoirzontal movement speed.
        public float verticalMinSpeed = 0.01f;      // Minimum vertical movement speed (only used with abs mouse position).
        public float verticalMaxSpeed = 1.0f;       // Maximum vertical movement speed.

        public float dragDistance = 100.0f;         // How far to drag the mouse until max speed is reached (for using drag input).

        // What the middle division of the axis is.
        // So setting verticalDivisions to 5 means that 5 is the middle, with 4 divisions on either side.
        // Higher numbers of divisions mean smoother progression of acelleration.
        public int horizontalDivisionsMid = 70;
        public int verticalDivisionsMid = 40;
        // Centered divisions where movement does not occur.
        // Higher numbers gives a larger space in the center where movement does not occur.
        public int horizontalDeadDivisions = 30;
        public int verticalDeadDivisions = 17;

        private struct IntVector2 { public int x; public int y;}    // For holding 2D mouse section.

        private IntVector2 mouse_section_;  // The section of the screen the mouse is in, using horizontal and vertical divisions.
        private Vector2 mouse_drag_start_;  // The mouse's position at the start of a mouse drag.

        void Awake() {
            // Ensure there is at least 1 division.
            if (horizontalDivisionsMid < 1)
                horizontalDivisionsMid = 1;
            if (verticalDivisionsMid < 1)
                verticalDivisionsMid = 1;

            // Ensure there are at least 0 dead divisions.
            if (horizontalDeadDivisions < 0)
                horizontalDeadDivisions = 0;
            if (verticalDeadDivisions < 0)
                verticalDeadDivisions = 0;

            // Bounds check.
            if (horizontalDivisionsMid < horizontalDeadDivisions)
                horizontalDivisionsMid = horizontalDeadDivisions + 1;
            if (verticalDivisionsMid < verticalDeadDivisions)
                verticalDivisionsMid = verticalDeadDivisions + 1;

            if (moverioCamera == null) {
                Debug.Log("Warning: no camera slected. Searching for Main Camera...");
                moverioCamera = GameObject.FindWithTag("MainCamera").camera;
                if (moverioCamera == null) {
                    Debug.Log("Error: Main Camera could not be found. MoverioMover disabled.");
                    inputFromAbsoluteMousePosition = false;
                    inputFromMouseDrag = false;
                } else {
                    Debug.Log("Main Camera found.");
                }
            }
        }

        void Update() {

            if (inputFromAbsoluteMousePosition) {
                // Find the mouse position on the horizontal and vertical axes.
                updateMouseSection();
                // Move based on mouse position.
                MoveFromMouseSection();
            }
            if (inputFromMouseDrag) {
                if (Input.GetMouseButtonDown(0)) {
                    mouse_drag_start_ = Input.mousePosition; // Save the current mouse position at the start of a drag.
                }
                if (Input.GetMouseButton(0)) {
                    moveFromMouseDrag();                     // Move as mouse is dragged around.
                }
            }
        }
        private void moveFromMouseDrag() {
            float speed_x_ = getMovementSpeedFromDrag(horizontalMaxSpeed, dragDistance, mouse_drag_start_.x, Input.mousePosition.x);
            float speed_y_ = getMovementSpeedFromDrag(verticalMaxSpeed, dragDistance, mouse_drag_start_.y, Input.mousePosition.y);

            // Make a vectors to where the camera is looking.
            Vector3 right_ = moverioCamera.transform.right.normalized;
            Vector3 forward_ = moverioCamera.transform.forward.normalized;
            // Don't move vertically.
            right_.y = 0.0f;
            forward_.y = 0.0f;

            // Update X
            Vector3 displacement = right_ * speed_x_ * Time.deltaTime;
            transform.position += displacement;
            // Update Y
            displacement = forward_ * speed_y_ * Time.deltaTime;
            transform.position += displacement;
        }
        private float getMovementSpeedFromDrag(float maxSpeed, float maxDragDistance, float mouseStart, float mouseCurrent) {
            float movement_speed_ = 0.0f;

            int direction = mouseStart > mouseCurrent ? -1 : 1;
            float delta = mouseStart > mouseCurrent ? (float)(mouseStart - mouseCurrent) : (float)(mouseCurrent - mouseStart);

            if (delta != 0) {
                // How fast to move is a fraction of how far from the starting position the mouse has been dragged, up to maxDragDistance.
                movement_speed_ = Mathf.Min(maxSpeed, (float)(maxSpeed * Mathf.Abs(delta / maxDragDistance))) * direction;
            }

            return movement_speed_;
        }

        private void updateMouseSection() {
            int total_divisions_H_ = (horizontalDivisionsMid * 2) - 1;  // Get all horizontal divisions.
            int total_divisions_V_ = (verticalDivisionsMid * 2) - 1;    // Get all vertical divisions.
            Vector2 mouse_pos_ = Input.mousePosition;                   // Get the current mouse position.
            int width_ = Screen.width;
            int height_ = Screen.height;

            // Find the 2D section the mouse is in.
            if (total_divisions_H_ > 1) {
                int section_width_ = width_ / total_divisions_H_;
                mouse_section_.x = (int)(mouse_pos_.x / section_width_) + 1;
            }
            if (total_divisions_V_ > 1) {
                int section_height_ = height_ / total_divisions_V_;
                mouse_section_.y = (int)(mouse_pos_.y / section_height_) + 1;
            }
            // Bounds check.
            if (mouse_section_.x <= 0)
                mouse_section_.x = 1;
            if (mouse_section_.x > total_divisions_H_)
                mouse_section_.x = total_divisions_H_;
            if (mouse_section_.y <= 0)
                mouse_section_.y = 1;
            if (mouse_section_.y > total_divisions_V_)
                mouse_section_.y = total_divisions_V_;
        }
        private void MoveFromMouseSection() {
            // Find how fast to move.
            float speed_x_ = getMovementSpeedFromSection(horizontalDivisionsMid, horizontalDeadDivisions, horizontalMinSpeed, horizontalMaxSpeed, mouse_section_.x);
            float speed_y_ = getMovementSpeedFromSection(verticalDivisionsMid, verticalDeadDivisions, verticalMinSpeed, verticalMaxSpeed, mouse_section_.y);

            // Make vectors to where the camera is looking.
            Vector3 right_ = moverioCamera.transform.right.normalized;
            Vector3 forward_ = moverioCamera.transform.forward.normalized;
            // Don't move vertically.
            right_.y = 0.0f;
            forward_.y = 0.0f;

            // Update X
            Vector3 displacement = right_ * speed_x_ * Time.deltaTime;
            transform.position += displacement;
            // Update Y
            displacement = forward_ * speed_y_ * Time.deltaTime;
            transform.position += displacement;
        }
        private float getMovementSpeedFromSection(int middle, int deadDivisions, float minSpeed, float maxSpeed, int section) {
            float movement_speed_ = 0.0f;
            float section_speed_ = 0.0f;
            int direction_ = middle > section ? -1 : 1;

            // Find how far the section is from the middle. 1 = farthest; middle = closest.
            if (middle < section) {
                section = middle - (section - middle);
            }

            // Find the speed for the mouse section.
            if (section <= middle - deadDivisions) {    // Only move if outside the "dead zone".
                // Get the percentage from min to max speed for the section.
                float section_speed_ratio_ = 1;         // How fast to move in the current section. Defaults to max speed.
                int end = middle - deadDivisions - 1;   // The first section after the min speed.

                // Make sure there are at least two movement sections (one section for min speed and one for max speed).
                if (end > 0) {
                    section_speed_ratio_ = (float)(middle - deadDivisions - section) / end;
                } // Defaults to max speed if there isn't a section for both min and max speed.

                // Get the speed for the section
                section_speed_ = minSpeed + (section_speed_ratio_ * (maxSpeed - minSpeed));
                movement_speed_ = section_speed_ * direction_;
            }

            return movement_speed_;
        }
    }
}