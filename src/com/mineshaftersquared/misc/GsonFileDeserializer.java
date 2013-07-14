package com.mineshaftersquared.misc;

import java.io.File;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GsonFileDeserializer implements JsonDeserializer<File> {
	@Override
	public File deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) {
			return null;
		}
		return new File(json.getAsJsonPrimitive().getAsString());
	}
}
