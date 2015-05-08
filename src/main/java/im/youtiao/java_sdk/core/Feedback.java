package im.youtiao.java_sdk.core;

import org.codehaus.jackson.annotate.JsonProperty;

public class Feedback {
	
	@JsonProperty("sticker")
	private String sticker;
	
	@JsonProperty("creator_id")
	private Integer creatorId;
	
	public String getSticker() {
		return sticker;
	}
	
	public void setSticker(String sticker) {
		this.sticker = sticker;
	}

	public Integer getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Integer creatorId) {
		this.creatorId = creatorId;
	}	
}
