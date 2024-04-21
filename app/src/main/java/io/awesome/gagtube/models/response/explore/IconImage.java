package io.awesome.gagtube.models.response.explore;

import com.google.gson.annotations.SerializedName;

public class IconImage{

	@SerializedName("iconType")
	private String iconType;

	public String getIconType(){
		return iconType;
	}

	@Override
 	public String toString(){
		return 
			"IconImage{" + 
			"iconType = '" + iconType + '\'' + 
			"}";
		}
}