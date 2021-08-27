package utils.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import utils.HexStringUtil;

/**
 * Java内置的对象序列化
 */
public class JavaObjectSerializer implements Serializer {

    @Override
    public String toString(Object value) {
        byte[] bytes = toBytes(value);
        return HexStringUtil.toHexString(bytes);
    }

    @Override
    public byte[] toBytes(Object value) {
        if (value == null) {
            return null;
        }

        try {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                    oos.writeObject(value);
                    oos.flush();
                    out.flush();
                    return out.toByteArray();
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public <T> T toObject(String value, Class<T> clazz) {
        byte[] bytes = HexStringUtil.toByteArray(value);
        return toObject(bytes, clazz);
    }

    @Override
    public <T> T toObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null) {
            return null;
        }

        try {
            try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
                try (ObjectInputStream ois = new ObjectInputStream(in)) {
                    Object object = ois.readObject();
                    return (T)object;
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
