package com.sinnus.zhihupaper.model;

import java.util.List;

/**
 * Created by sinnus on 2015/11/16.
 */
public class Before {
    public String date;
    public List<Story> stories;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Story> getStories() {
        return stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }
}
