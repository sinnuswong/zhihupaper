package com.sinnus.zhihupaper.model;

import java.util.List;

/**
 * Created by sinnus on 2015/11/16.
 */
public class Latest extends Before {
    private List<TopStory> top_stories;

    public List<TopStory> getTop_stories() {
        return top_stories;
    }

    public void setTop_stories(List<TopStory> top_stories) {
        this.top_stories = top_stories;
    }
}
