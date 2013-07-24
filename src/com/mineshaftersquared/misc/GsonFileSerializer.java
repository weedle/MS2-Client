package com.mineshaftersquared.misc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonFileSerializer implements JsonSerializer<File>, JsonDeserializer<File> {

	@Override
	public JsonElement serialize(File file, Type type, JsonSerializationContext context) {
		try {
			return new JsonPrimitive(file.getCanonicalPath());
		} catch (IOException ex) {
			return JsonNull.INSTANCE;
		}
	}
	
	@Override
	public File deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) {
			return null;
		}
		return new File(json.getAsJsonPrimitive().getAsString());
	}

}
