package danielbernal.app.spark;

//import static com.datastax.spark.connector.japi.CassandraJavaUtil.javaFunctions;
//import static com.datastax.spark.connector.japi.CassandraJavaUtil.mapToRow;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import scala.Tuple2;

public class SparkProcessor {

    public static JavaSparkContext sparkContext;

    public void createContext() throws InterruptedException {

        Logger.getLogger("org")
                .setLevel(Level.OFF);
        Logger.getLogger("akka")
                .setLevel(Level.OFF);

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "use_a_separate_group_id_for_each_stream");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Collection<String> topics = Arrays.asList("bigdata-tweets");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[1]");
        sparkConf.setAppName("SparkAndKakfa");
        sparkConf.set("spark.kafka.connection.host", "127.0.0.1");

        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, Durations.seconds(10));

        sparkContext = streamingContext.sparkContext();

        streamingContext.checkpoint("./.checkpoint");

        JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(
                streamingContext, LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams));

        JavaPairDStream<String, String> results;
        results = messages.mapToPair(record -> {
            //System.out.println("Printing record from Twitter produced by Kafka and consumed by Spark");
            //System.out.println(record.value() + "\n\n");
            return new Tuple2<>(record.key(), record.value());
        });


        JavaDStream<String> lines = results.map(tuple2 -> tuple2._2());

        System.out.println("Printing record from Twitter produced by Kafka and consumed by Spark");
        lines.foreachRDD(x -> x.collect().stream().forEach(y -> System.out.println(y)));

        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
