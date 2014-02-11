package de.ovgu.wdok.guinan.graph;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PropertyValueSerializer extends JsonSerializer<GuinanNode> {

    @Override
    public void serialize(GuinanNode property_value, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(property_value.getLabel());
        //jsonGenerator.writeString(property_value.datatoJSONString());
        jsonGenerator.writeEndObject();
    }

}