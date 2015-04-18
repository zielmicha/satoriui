package net.atomshare.satori;

import com.google.common.collect.ImmutableMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class Util {
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> makeMap(Object... args) {
        ImmutableMap.Builder<K, V> builder = new ImmutableMap.Builder<>();
        assert(args.length % 2 == 0);
        for(int i=0; i < args.length; i += 2) {
            builder.put((K)args[i], (V)args[i+1]);
        }
        return builder.build();
    }

    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
