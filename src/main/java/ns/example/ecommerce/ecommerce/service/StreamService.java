package ns.example.ecommerce.ecommerce.service;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StreamService {

    private static final Serde<String> STRING_SERDE = Serdes.String();

    @KafkaListener(topics="test-topic", groupId = "spring")
    public void consume(String message){
        log.info("Consumed Message. body: %s".formatted(message));
    }

    public void filterPipeLine(StreamsBuilder sb){
        KStream<String, String> kStream = sb.stream("test-stream", Consumed.with(STRING_SERDE, STRING_SERDE));
        kStream.filter((key, value) -> value.contains("aaa")).to("next-topic");
        // Kafka Stream의 필터링은 대소문자를 구분함
    }

    public void joinPipeLine(StreamsBuilder sb){
        KStream<String, String> leftStream = sb.stream("leftTopic", Consumed.with(STRING_SERDE, STRING_SERDE))
                .selectKey((k,v) -> v);
        KStream<String, String> rightStream = sb.stream("rightTopic", Consumed.with(STRING_SERDE, STRING_SERDE))
                .selectKey((k,v) -> v.substring(0, v.indexOf(":")));

        KStream<String, String> joinStream = leftStream.join(rightStream,
                (leftValue, rightValue) -> leftValue + " + " + rightValue,
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofMinutes(1)));

        joinStream.print(Printed.toSysOut());

        joinStream.to("joinTopic");
    }
}
