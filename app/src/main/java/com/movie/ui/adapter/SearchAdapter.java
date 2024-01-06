package com.movie.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.movie.R;
import com.movie.bean.Movie;

import java.util.ArrayList;

/**
 * @author aim
 * @date :2020/12/23
 * @description:
 */
public class SearchAdapter extends BaseQuickAdapter<Movie.Video, BaseViewHolder> {
    public SearchAdapter() {
        super(R.layout.item_search_layout, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, Movie.Video item) {
        helper.setText(R.id.tvName, String.format("%s %s %s %s", item.sourceName, item.name, item.type, item.note));
    }
}