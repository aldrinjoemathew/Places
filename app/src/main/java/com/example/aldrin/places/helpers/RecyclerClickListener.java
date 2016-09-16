package com.example.aldrin.places.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onItemLongClick(childView, view.getChildAdapterPosition(childView));
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            View childView1 = view.findChildViewUnder(e1.getX(), e1.getY());
            View childView2 = view.findChildViewUnder(e2.getX(), e2.getY());
            int pos1 = view.getChildAdapterPosition(childView1);
            int pos2 = view.getChildAdapterPosition(childView2);
            mListener.onFling(childView1, childView2, pos1, pos2);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onDoubleTap(childView, view.getChildAdapterPosition(childView));
            return super.onDoubleTap(e);
        }
    }

    public interface OnItemTouchListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onDoubleTap(View childView, int childAdapterPosition);
        void onFling(View childView1, View childView2, int pos1, int pos2);
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
