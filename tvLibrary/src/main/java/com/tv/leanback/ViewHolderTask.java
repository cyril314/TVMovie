package com.tv.leanback;


import androidx.recyclerview.widget.RecyclerView;

/**
 * Interface for schedule task on a ViewHolder.
 */
public interface ViewHolderTask {
    void run(RecyclerView.ViewHolder viewHolder);
}