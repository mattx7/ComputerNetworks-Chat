package chat_app.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * JSON MapperWrapper.
 */
public class JSON {

    @NotNull
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @NotNull
    private String json;

    private JSON(@NotNull String json) {
        this.json = json;
    }

    /**
     * Maps JSON to given clazz.
     *
     * @param clazz Convert to this type. Not null.
     * @return New instance from clazz. Not null.
     */
    @NotNull
    public <T> T to(@NotNull Class<T> clazz) throws IOException {
        Preconditions.checkNotNull(clazz, "clazz must not be null.");

        return JSON.MAPPER.readValue(json, clazz);
    }

    /**
     * Returns String in JSON format
     */
    @NotNull
    public String asString() {
        return this.json;
    }

    /**
     * Creates a JSON instance.
     *
     * @param json has be in JSON format.
     * @return new instance.
     */
    @NotNull
    public synchronized static JSON valueOf(@NotNull String json) throws IOException {
        Preconditions.checkNotNull(json, "json must not be null.");

        return new JSON(json);
    }


    /**
     * Maps an object to JSON as string.
     *
     * @param obj Not null.
     * @return String with JSON format. Not null.
     */
    @NotNull
    public synchronized static String format(@NotNull Object obj) throws IOException {
        Preconditions.checkNotNull(obj, "obj must not be null.");

        return MAPPER.writeValueAsString(obj);
    }
}
