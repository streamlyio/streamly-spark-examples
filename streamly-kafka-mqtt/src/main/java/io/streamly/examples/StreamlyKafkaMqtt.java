package io.streamly.examples;

import static java.lang.Math.toIntExact;

import java.io.PrintStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

/**
 * This class process data coming from a secured/unsecured Kafka topic count
 * them and send the results to a secured MQTT topic.
 * 
 **/

public class StreamlyKafkaMqtt {
	private static final Pattern SPACE = Pattern.compile(" ");
	static Logger log = LoggerFactory.getLogger(StreamlyKafkaMqtt.class);
	private static int seconds = 0;

	public static void main(String[] args) throws InterruptedException, MqttException {
		tieSystemOutAndErrToLog();
		if (args.length < 7) {
			System.err.println(
					"Usage: StreamlyKafkaMqtt <mqttBrokerUrl> <mqttTopic> <mqttClientID> <mqttUsername> <mqttPassword> <kafkaBrokers> <kafkaTopics>");
			System.exit(1);
		}

		if (args.length > 8) {
			System.err.println(
					"Usage: StreamlyKafkaMqtt <mqttBrokerUrl> <mqttTopic> <mqttClientID> <mqttUsername> <mqttPassword> <kafkaBrokers> <kafkaTopics> <kafkaJaasPath>");
			System.exit(1);
		}

		// Get the arguments provided in the spark.properties file
		String mqttBrokerUrl = args[0];
		String mqttTopic = args[1];
		String mqttClientID = args[2];
		String mqttUsername = args[3];
		String mqttPassword = args[4];
		String kafkaBrokers = args[5];
		String kafkaTopics = args[6];

		MqttClient client;
		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		connOpt.setUserName(mqttUsername);
		connOpt.setPassword(mqttPassword.toCharArray());

		client = new MqttClient(mqttBrokerUrl, mqttClientID);
		client.connect(connOpt);

		log.info("Connected to {}", mqttBrokerUrl);

		// Setup the mqtt topic
		final MqttTopic topic = client.getTopic(mqttTopic);

		// Create context with a 2 seconds batch interval
		SparkConf sparkConf = new SparkConf().setAppName("StreamlyKafkaMqtt");

		JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, Durations.seconds(2));

		Set<String> topicsSet = new HashSet<>(Arrays.asList(kafkaTopics.split(",")));
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", kafkaBrokers);
		kafkaParams.put("group.id", "kafka-mqtt" + new SecureRandom().nextInt(100));

		if (args.length == 8) {
			String kafkaAdminJaasFile = args[7];
			System.setProperty("java.security.auth.login.config", kafkaAdminJaasFile);
			kafkaParams.put("security.protocol", "SASL_PLAINTEXT");
			kafkaParams.put("sasl.mechanism", "PLAIN");
		}

		kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		// Create direct kafka stream with brokers and topics
		final JavaInputDStream<ConsumerRecord<String, String>> messages = KafkaUtils.createDirectStream(jssc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, String>Subscribe(topicsSet, kafkaParams));

		// Get the lines, split them into words, count the words and print
		JavaPairDStream<String, String> results = messages
				.mapToPair(new PairFunction<ConsumerRecord<String, String>, String, String>() {
					@Override
					public Tuple2<String, String> call(ConsumerRecord<String, String> record) {
						return new Tuple2<>(record.key(), record.value());
					}
				});

		JavaDStream<String> lines = results.map(new Function<Tuple2<String, String>, String>() {
			@Override
			public String call(Tuple2<String, String> tuple2) {
				return tuple2._2();
			}
		});

		JavaDStream<String> transactionCounts = lines.window(Durations.seconds(60));
		transactionCounts.foreachRDD(new VoidFunction<JavaRDD<String>>() {

			@Override
			public void call(JavaRDD<String> t0) throws Exception {
				if (t0 != null) {
					int transactions = toIntExact(t0.count());
					seconds = seconds + 2;
					int pubQoS = 0;
					String pubMsg = new String("time in sec " + seconds + " : number of transactions " + transactions);
					MqttMessage message = new MqttMessage(pubMsg.getBytes());
					message.setQos(pubQoS);
					message.setRetained(false);

					// Publish the message
					log.info("Publishing to topic \"{}\" qos {}", topic, pubQoS);
					MqttDeliveryToken token = null;

					// publish message to broker
					token = topic.publish(message);
					// Wait until the message has been delivered to the broker
					token.waitForCompletion();
				}
			}

		});

		jssc.start();
		jssc.awaitTermination();
	}

	public static String nowDate() {
		return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date()).replace(" ", "T") + "Z";
	}

	public static void tieSystemOutAndErrToLog() {
		System.setOut(createLoggingProxy(System.out));
		System.setErr(createLoggingProxy(System.err));
	}

	public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
		return new PrintStream(realPrintStream) {

			public void print(final String string) {
				log.error(string);
			}

			public void println(final String string) {
				log.error(string);
			}
		};
	}
}
