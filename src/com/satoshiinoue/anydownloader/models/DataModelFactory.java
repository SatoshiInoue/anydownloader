package com.satoshiinoue.anydownloader.models;

import android.content.Context;

/**
 * Used to construct DataModel objects based on type.
 */
public class DataModelFactory 
{
	/**
	 * Constructs a DataModel instance based on the given type.
	 * 
	 * @param url the resource url required to construct the DataModel object
	 * @param modelType the enum value used to determine which object type to construct
	 * @return the constructed DataModel object
	 */
	public static DataModel createDataModelInstance(final String url, final Context context, final ModelType modelType) {
		switch (modelType) {
			case TextModel:
				return new TextModel(url, context);
			case XMLModel:
				return new XMLModel(url, context);
			case JSONModel:
				return new JSONModel(url, context);	
			case FileModel:
				return new FileModel(url, context);
				
			default:
				return null;
		}
	}
}
