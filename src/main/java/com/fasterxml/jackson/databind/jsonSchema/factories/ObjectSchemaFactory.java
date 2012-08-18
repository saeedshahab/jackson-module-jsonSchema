package com.fasterxml.jackson.databind.jsonSchema.factories;


import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonSchema.factories.SchemaFactoryWrapper.SchemaFactoryWrapperProvider;
import com.fasterxml.jackson.databind.jsonSchema.types.JsonSchema;
import com.fasterxml.jackson.databind.jsonSchema.types.ObjectSchema;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

public class ObjectSchemaFactory implements JsonObjectFormatVisitor, SchemaProducer {

	protected SchemaFactoryWrapperProvider factoryWrapperProvider;
	protected SchemaFactory parent;
	protected ObjectSchema schema;
	
	public ObjectSchemaFactory(SchemaFactory parent, ObjectSchema schema) {
		this.parent = parent;
		this.schema = schema;
	}
	

	public SchemaFactoryWrapperProvider getFactoryWrapperProvider() {
		return factoryWrapperProvider;
	}


	public SchemaFactory getParent() {
		return parent;
	}


	public SerializerProvider getProvider() {
		return parent.getProvider();
	}


	public ObjectSchema getSchema() {
		return schema;
	}


	private JsonSerializer<Object> getSer(BeanProperty writer) {
		JsonSerializer<Object> ser = null;
		if (writer instanceof BeanPropertyWriter) {
			ser = ((BeanPropertyWriter)writer).getSerializer();
		}
		if (ser == null) {
			Class<?>	serType = writer.getType().getRawClass();
			try {
				return getProvider().findValueSerializer(serType, writer);
			} catch (JsonMappingException e) {
				// TODO: log error
			}
		}
		return ser;
	}


	public void optionalProperty(BeanProperty writer) {
		schema.putOptionalProperty(writer.getName(), propertySchema(writer));
	}


	public void optionalProperty(String name) {
		schema.putOptionalProperty(name, JsonSchema.minimalForFormat(JsonFormatTypes.ANY));
	}	
	
	public void optionalProperty(String name, JsonFormatVisitable handler, JavaType propertyTypeHint) {
		schema.putOptionalProperty(name, propertySchema(handler, propertyTypeHint));
	}
	
	public void property(BeanProperty writer) {
		schema.putProperty(writer.getName(), propertySchema(writer));
	}
	
	public void property(String name) {
		schema.putProperty(name, JsonSchema.minimalForFormat(JsonFormatTypes.ANY));
	}

	public void property(String name, JsonFormatVisitable handler, JavaType propertyTypeHint) {
		schema.putProperty(name, propertySchema(handler, propertyTypeHint));
	}
	
	protected JsonSchema propertySchema(BeanProperty writer) {
		SchemaFactoryWrapper visitor = factoryWrapperProvider.SchemaFactoryWrapper();
		visitor.setProvider(parent.getProvider());
		JsonSerializer<Object> ser = getSer(writer);
		if (ser != null && ser instanceof JsonFormatVisitable) {
			((JsonFormatVisitable)ser).acceptJsonFormatVisitor(visitor, writer.getType());
		} else {
			visitor.expectAnyFormat(writer.getType());
		}
		return visitor.finalSchema();
	}
	
	protected JsonSchema propertySchema(JsonFormatVisitable handler, JavaType propertyTypeHint) {
		SchemaFactoryWrapper visitor = factoryWrapperProvider.SchemaFactoryWrapper();
		visitor.setProvider(parent.getProvider());
		handler.acceptJsonFormatVisitor(visitor, propertyTypeHint);
		return visitor.finalSchema();
	}
	
	public void setFactoryWrapperProvider(
			SchemaFactoryWrapperProvider factoryWrapperProvider) {
		this.factoryWrapperProvider = factoryWrapperProvider;
	}
	
	public void setParent(SchemaFactory parent) {
		this.parent = parent;
	}


	public void setProvider(SerializerProvider provider) {
		parent.setProvider(provider);
	}


	public void setSchema(ObjectSchema schema) {
		this.schema = schema;
	}

}
