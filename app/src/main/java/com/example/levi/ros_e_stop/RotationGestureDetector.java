package com.example.levi.ros_e_stop;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by levi on 1/9/17.
 */


/*public class RotationGestureDetector {
    private static final int INVALID_POINTER_ID = -1;
    private float fX, fY, sX, sY;
    private int ptrID1, ptrID2;
    private float mAngle;

    private boolean p1, p2;

    private OnRotationGestureListener mListener;

    public float getAngle() {
        return mAngle;
    }

    public RotationGestureDetector(OnRotationGestureListener listener){
        mListener = listener;
        ptrID1 = INVALID_POINTER_ID;
        ptrID2 = INVALID_POINTER_ID;
        p1 = p2 = false;
    }

    public boolean isRotating()
    {
        return (p1);
    }

    public boolean onTouchEvent(MotionEvent event){
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                ptrID1 = event.getPointerId(event.getActionIndex());
                //p1 = true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                ptrID2 = event.getPointerId(event.getActionIndex());
                sX = event.getX(event.findPointerIndex(ptrID1));
                sY = event.getY(event.findPointerIndex(ptrID1));
                fX = event.getX(event.findPointerIndex(ptrID2));
                fY = event.getY(event.findPointerIndex(ptrID2));

                mListener.OnRotationBegin(this);
                //p2 = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID){
                    float nfX, nfY, nsX, nsY;
                    nsX = event.getX(event.findPointerIndex(ptrID1));
                    nsY = event.getY(event.findPointerIndex(ptrID1));
                    nfX = event.getX(event.findPointerIndex(ptrID2));
                    nfY = event.getY(event.findPointerIndex(ptrID2));

                    mAngle = angleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY);

                    if (mListener != null) {
                        mListener.OnRotation(this);
                    }
                    p1 = true;
                }
                else
                {
                    //if (mListener != null) {
                    //    mListener.OnRotationStop(this);
                    //}
                }
                break;
            case MotionEvent.ACTION_UP:
                ptrID1 = INVALID_POINTER_ID;
                if (mListener != null) {
                    mListener.OnRotationStop(this);
                }
                //p1 = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                ptrID2 = INVALID_POINTER_ID;
                //p2 = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                ptrID1 = INVALID_POINTER_ID;
                ptrID2 = INVALID_POINTER_ID;
                //p1 = p2 = false;
                break;
        }
        return true;
    }

    private float angleBetweenLines (float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY)
    {
        float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
        float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );

        float angle = ((float)Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return angle;
    }

    public static interface OnRotationGestureListener {
        public void OnRotation(RotationGestureDetector rotationDetector);
        public void OnRotationStop(RotationGestureDetector rotationDetector);
        public void OnRotationBegin(RotationGestureDetector rotationGestureDetector);
    }
}*/

public class RotationGestureDetector {

    private static final int INVALID_POINTER_ID = -1;
    private PointF mFPoint = new PointF();
    private PointF mSPoint = new PointF();
    private int mPtrID1, mPtrID2;
    private float mAngle;
    private View mView;

    private OnRotationGestureListener mListener;

    public float getAngle() {
        return mAngle;
    }

    public RotationGestureDetector(OnRotationGestureListener listener, View v) {
        mListener = listener;
        mView = v;
        mPtrID1 = INVALID_POINTER_ID;
        mPtrID2 = INVALID_POINTER_ID;
    }

    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_OUTSIDE:
                //Log.d(this, "ACTION_OUTSIDE");
                break;
            case MotionEvent.ACTION_DOWN:
                //Log.v(this, "ACTION_DOWN");
                mPtrID1 = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //Log.v(this, "ACTION_POINTER_DOWN");
                mPtrID2 = event.getPointerId(event.getActionIndex());

                mListener.OnRotationBegin(this);

                getRawPoint(event, mPtrID1, mSPoint);
                getRawPoint(event, mPtrID2, mFPoint);

                break;
            case MotionEvent.ACTION_MOVE:
                if (mPtrID1 != INVALID_POINTER_ID && mPtrID2 != INVALID_POINTER_ID) {
                    PointF nfPoint = new PointF();
                    PointF nsPoint = new PointF();

                    getRawPoint(event, mPtrID1, nsPoint);
                    getRawPoint(event, mPtrID2, nfPoint);

                    mAngle = angleBetweenLines(mFPoint, mSPoint, nfPoint, nsPoint);

                    if (mListener != null) {
                        mListener.OnRotation(this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mPtrID1 = INVALID_POINTER_ID;
                mListener.OnRotationStop(this);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mPtrID2 = INVALID_POINTER_ID;
                mListener.OnRotationStop(this);
                break;
            case MotionEvent.ACTION_CANCEL:
                mPtrID1 = INVALID_POINTER_ID;
                mPtrID2 = INVALID_POINTER_ID;
                mListener.OnRotationStop(this);
                break;
            default:
                break;
        }
        return true;
    }

    void getRawPoint(MotionEvent ev, int index, PointF point) {
        final int[] location = {0, 0};
        mView.getLocationOnScreen(location);

        float x = ev.getX(index);
        float y = ev.getY(index);

        double angle = Math.toDegrees(Math.atan2(y, x));
        angle += mView.getRotation();

        final float length = PointF.length(x, y);

        x = (float) (length * Math.cos(Math.toRadians(angle))) + location[0];
        y = (float) (length * Math.sin(Math.toRadians(angle))) + location[1];

        point.set(x, y);
    }

    private float angleBetweenLines(PointF fPoint, PointF sPoint, PointF nFpoint, PointF nSpoint) {
        float angle1 = (float) Math.atan2((fPoint.y - sPoint.y), (fPoint.x - sPoint.x));
        float angle2 = (float) Math.atan2((nFpoint.y - nSpoint.y), (nFpoint.x - nSpoint.x));

        float angle = ((float) Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return -angle;
    }

    public interface OnRotationGestureListener {
        void OnRotation(RotationGestureDetector rotationDetector);

        void OnRotationBegin(RotationGestureDetector rotationDetector);

        void OnRotationStop(RotationGestureDetector rotationDetector);
    }
}