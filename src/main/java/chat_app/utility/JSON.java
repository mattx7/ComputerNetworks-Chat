package chat_app.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * JSON MapperWrapper.
 */
class JSON {

    @NotNull
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @NotNull
    private String json;

    /**
     * Use {@link #valueOf(String)}.
     */
    private JSON(@NotNull final String json) {
        this.json = json;
    }

    /**
     * Maps JSON to given clazz.
     *
     * @param clazz Convert to this type. Not null.
     * @return New instance from clazz. Not null.
     */
    @NotNull
    <T> T to(@NotNull final Class<T> clazz) throws IOException {
        Preconditions.checkNotNull(clazz, "clazz must not be null.");

        return JSON.MAPPER.readValue(json, clazz);
    }

    /**
     * Creates a JSON instance.
     *
     * @param json has be in JSON format.
     * @return new instance.
     */
    @NotNull
    synchronized static JSON valueOf(@NotNull final String json) throws IOException {
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
    synchronized static String format(@NotNull final Object obj) throws IOException {
        Preconditions.checkNotNull(obj, "obj must not be null.");

        return MAPPER.writeValueAsString(obj);
    }
}
