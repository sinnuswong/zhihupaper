package com.sinnus.zhihupaper.model;

/**
 * Created by sinnus on 2015/12/3.
 */
public class TopStory extends Story {
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "TopStoriesEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", ga_prefix='" + ga_prefix + '\'' +
                ", image='" + image + '\'' +
                ", type=" + type +
                '}';
    }
}
