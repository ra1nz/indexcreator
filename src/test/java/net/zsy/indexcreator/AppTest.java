package net.zsy.indexcreator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public static void main(String[] args) {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setUsername("root");
		connectionFactory.setPassword("123456");
		connectionFactory.setHost("192.168.100.101");
		connectionFactory.setPort(5672);

		Connection connection = null;
		Channel channel = null;

		try {
			connection = connectionFactory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare("accept", false, false, false, null);

			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> data = new HashMap<String, String>();
			data.put("type", "product");
			data.put("objTypeId", "1");
			for (int i = 0; i < 10000; i++) {
				data.put("id", 200000 + i + "");
				data.put("objId", 200000 + i + "");
				String message = mapper.writeValueAsString(data);
				channel.basicPublish("", "accept", null, message.getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} finally {
			if (channel != null && channel.isOpen()) {
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
			if (connection != null && channel.isOpen()) {
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
