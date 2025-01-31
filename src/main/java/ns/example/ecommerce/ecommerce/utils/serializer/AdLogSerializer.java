package ns.example.ecommerce.ecommerce.utils.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import ns.example.ecommerce.ecommerce.domain.LogData.AdLog;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class AdLogSerializer implements Serializer<AdLog> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, AdLog data) {
        try {
            if (data == null){
                return null;
            }
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SecurityException("Exception Occured");
        }
    }

    @Override
    public void close() {
    }

}