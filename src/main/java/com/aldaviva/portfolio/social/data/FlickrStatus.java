package com.aldaviva.portfolio.social.data;

public class FlickrStatus implements SocialStatus {

	private String photoPageUrl;
	private String thumbnailUrl;
	private String title;
	
	public String getPhotoPageUrl() {
		return photoPageUrl;
	}
	public void setPhotoPageUrl(final String photoPageUrl) {
		this.photoPageUrl = photoPageUrl;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(final String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(final String title) {
		this.title = title;
	}
	
	@Override
    public String toString() {
	    return "FlickrStatus [photoPageUrl=" + photoPageUrl + ", thumbnailUrl=" + thumbnailUrl + ", title=" + title + "]";
    }
}
