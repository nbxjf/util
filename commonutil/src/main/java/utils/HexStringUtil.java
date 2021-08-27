package utils;

/**
 * HexStringUtil
 */
public class HexStringUtil {
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public HexStringUtil() {
    }

    public static final String toHexString(byte[] bs) {
        if (bs == null) {
            return null;
        } else {
            char[] hexChars = new char[bs.length * 2];

            for (int i = 0; i < bs.length; ++i) {
                int v = bs[i] & 255;
                hexChars[i * 2] = hexArray[v >>> 4];
                hexChars[i * 2 + 1] = hexArray[v & 15];
            }

            return new String(hexChars);
        }
    }

    public static final byte[] toByteArray(String hexString) {
        if (hexString != null && hexString.length() % 2 == 0) {
            hexString = hexString.toLowerCase();
            char[] cs = hexString.toCharArray();
            byte[] bs = new byte[cs.length / 2];

            for (int i = 0; i < bs.length; ++i) {
                char b1 = cs[i * 2];
                char b2 = cs[i * 2 + 1];
                bs[i] = (byte)((b1 >= 97 ? b1 - 97 + 10 : b1 - 48) << 4 | (b2 >= 97 ? b2 - 97 + 10 : b2 - 48));
            }

            return bs;
        } else {
            return null;
        }
    }
}
