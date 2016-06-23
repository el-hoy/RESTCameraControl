package org.restcameracontrol.serializers;

import java.io.IOException;

import org.restcameracontrol.beans.CameraInfo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CameraInfoSerializer extends JsonSerializer<CameraInfo> {

	@Override
	public void serialize(CameraInfo cameraInfo, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("id", cameraInfo.getId());
		jgen.writeStringField("manufacturer", cameraInfo.getManufacturer());
		jgen.writeStringField("model", cameraInfo.getModel());
		jgen.writeStringField("serialNumber", cameraInfo.getSerialNumber());
		jgen.writeStringField("status", cameraInfo.getStatus().name());

		jgen.writeEndObject();
	}
}
