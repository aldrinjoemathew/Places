package com.example.aldrin.places.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by aldrin on 2/9/16.
 */

public class RecyclerClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemTouchListener mListener;
    private GestureDetector mGestureDetector;
    private RecyclerView view;

    public RecyclerClickListener(Context context, OnItemTouchListener mListener) {
        this.mListener = mListener;
        mGestureDetector = new GestureDetector(context, new MyGestureDetector());
    }

    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("info", "single tap confirmed");
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("info", "long press");
            super.onLongPress(e);
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onItemLongClick(childView, view.getChildAdapterPosition(childView));
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("info", "double tap");
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onDoubleTap(childView, view.getChildAdapterPosition(childView));
            return super.onDoubleTap(e);
        }
    }

    public interface OnItemTouchListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onDoubleTap(View childView, int childAdapterPosition);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        this.view = view;
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null) {
            mGestureDetector.onTouchEvent(e);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
