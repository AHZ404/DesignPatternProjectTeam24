package database;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public final class JavaTimeJsonModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public JavaTimeJsonModule() {
        super("JavaTimeJsonModule");
        add(LocalDate.class, LocalDate::parse);
        add(LocalTime.class, LocalTime::parse);
        add(LocalDateTime.class, LocalDateTime::parse);
    }

    private <T> void add(Class<T> type, Function<String, T> parser) {
        addSerializer(type, new JsonSerializer<T>() {
            @Override
            public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString(value.toString());
            }
        });
        addDeserializer(type, new JsonDeserializer<T>() {
            @Override
            public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getValueAsString();
                return (text == null || text.isBlank()) ? null : parser.apply(text.trim());
            }
        });
    }
}
