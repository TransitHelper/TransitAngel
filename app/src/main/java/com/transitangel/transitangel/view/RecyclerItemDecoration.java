package com.transitangel.transitangel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.transitangel.transitangel.R;


public class RecyclerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    public RecyclerItemDecoration(Context context) {
        this (context, R.drawable.recycler_view_divider);
    }

    public RecyclerItemDecoration(Context context, @DrawableRes int dividerResId) {
        mDivider = ContextCompat.getDrawable(context, dividerResId);
    }

    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }

}
