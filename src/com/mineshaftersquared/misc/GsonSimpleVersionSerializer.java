package com.mineshaftersquared.misc;

import java.lang.reflect.Type;

import com.creatifcubed.simpleapi.SimpleVersion;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GsonSimpleVersionSerializer implements JsonSerializer<SimpleVersion>, JsonDeserializer<SimpleVersion> {

	@Override
	public SimpleVersion deserialize(JsonElement arg0, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
		return new SimpleVersion(arg0.getAsString());
	}

	@Override
	public JsonElement serialize(SimpleVersion arg0, Type arg1, JsonSerializationContext arg2) {
		return new JsonPrimitive(arg0.toString());
	}

}
