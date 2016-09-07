package com.example.aldrin.places.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by aldrin on 2/9/16.
 */

public class RecyclerClickListener implements OnItemTouchListener {

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;
    private RecyclerView view;

    public RecyclerClickListener(Context context, OnItemClickListener mListener) {
        this.mListener = mListener;
        mGestureDetector = new GestureDetector(context, new MyGestureDetector());
    }

    public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            mListener.onItemLongClick(childView, view.getChildAdapterPosition(childView));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
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
