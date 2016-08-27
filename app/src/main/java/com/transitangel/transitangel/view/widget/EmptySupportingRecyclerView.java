package com.transitangel.transitangel.view.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class EmptySupportingRecyclerView extends RecyclerView {

    private View mEmptyView;

    private final AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public EmptySupportingRecyclerView(Context context) {
        super(context);
    }

    public EmptySupportingRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptySupportingRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty() {
        if (mEmptyView != null && getAdapter() != null) {
            final boolean isEmptyViewVisible = getAdapter().getItemCount() == 0;
            mEmptyView.setVisibility(isEmptyViewVisible ? VISIBLE : GONE);
            setVisibility(isEmptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(mDataObserver);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mDataObserver);
        }

        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
        checkIfEmpty();
    }

    public View getEmptyView() {
        return mEmptyView;
    }
}
