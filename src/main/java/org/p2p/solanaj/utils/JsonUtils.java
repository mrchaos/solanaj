package org.p2p.solanaj.utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import org.p2p.solanaj.rpc.RpcException;

import okio.Buffer;

/**
 * Object를 
 *  - json string으로 변환
 *  -   
 */
public class JsonUtils {
    /**
     * json object에서 원하는 값/object를 가져온다.
     * @param path json tree map에서 최종 key값 까지의 경로
     *              예) "value.data.parsed.info.mint"
     * @param map map object
     * @return path에 해당하는 object
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    static public Object getObjectFromMap(String path, Object map ) throws Exception {
        Map<String,Object> m = (Map<String,Object>)map;
        String[] keys = path.split("\\.");
        Object obj = null;        
        String key = null;
        for (int i=0; i < keys.length; i++) {
            key = keys[i];
            obj = m.getOrDefault(key, null);
            // object가 null이거나 key경로의 마지막인 경우
            if(Objects.isNull(obj)
                || i == keys.length - 1) {
                return obj;
            }
            // key의 value가 Map instance인 경우 mao으로 mapping
            else if (obj instanceof Map) {
                m = (Map<String,Object>)Map.class.cast(obj);
            }
            else {
                return null;
            }
        }
        return obj;
    }
    /**
     *  Object를 대상 object로 casting
     * @param <T>
     * @param jsonObject
     * @param clazz class
     * @return T
     * @throws RpcException
     * @throws IOException
     */
    static public <T> T castJson(Object obj, Class<T> clazz) 
        throws RpcException, IOException {

        JsonAdapter<T> adapter = new Moshi.Builder()
                .build()
                .adapter(Type.class.cast(clazz));
        adapter = adapter.serializeNulls();
        
        return adapter.fromJsonValue(obj);
    } 

    static public String toJsonPrettyString(Object jsonObject) throws IOException {
        return toJsonString(jsonObject,true);
    }
    /**
     * Object를 jsonstring으로 변환
     * @param obj  object
     * @param pretty indent를 적용하여 출력할지 여부
     * @return
     * @throws IOException
     */
    // @SuppressWarnings("unchecked")
    static public String toJsonString(Object obj,boolean pretty) throws IOException {        
        if (!Objects.isNull(obj)) {
            final Moshi moshi = new Moshi.Builder()
                        .build();
            final Buffer buffer = new Buffer();
            final JsonWriter jsonWriter = JsonWriter.of(buffer);
            if(pretty) {
                jsonWriter.setIndent(" ");
            }
            // null도 표현될 수 있데 serializeNulls 적용  
            moshi.adapter(Object.class)
                .serializeNulls().toJson(jsonWriter,obj);            
            return buffer.readUtf8();
        }
        return null; 
    }
}
