package net.zsy.indexcreator.mq.accepter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

import net.zsy.indexcreator.mq.Accepter;
import net.zsy.indexcreator.mq.MessageQueue;

public class RabbitMQAccepter implements Accepter {

	private Map<String, String> configurations;

	private List<ConnectionFactory> connectionFactories;

	private Boolean inited = false;
	private Boolean started = false;

	BlockingQueue<String> messages = new LinkedBlockingQueue<String>();

	private ExecutorService exec;

	private Map<String, AcceptThread> threads = new HashMap<String, AcceptThread>();

	private String queuename;

	@Override
	public void init() {
		if (!inited) {
			synchronized (inited) {
				if (!inited) {
					String hostports = configurations.get(MessageQueue.HOSTPORT);
					if (StringUtils.isBlank(hostports)) {
						throw new RuntimeException("HOSTPORT not configured.");
					}
					String[] hps = hostports.split(",");
					connectionFactories = new ArrayList<ConnectionFactory>(hps.length);
					String username = configurations.get(MessageQueue.USERNAME);
					String password = configurations.get(MessageQueue.PASSWORD);
					queuename = configurations.get(MessageQueue.QUEUENAME);
					for (String hp : hps) {
						ConnectionFactory connectionFactory = new ConnectionFactory();
						connectionFactory.setHost(hp.split(":")[0]);
						connectionFactory.setPort(Integer.valueOf(hp.split(":")[1]));
						connectionFactory.setUsername(username);
						connectionFactory.setPassword(password);

						connectionFactories.add(connectionFactory);
					}
					exec = Executors.newFixedThreadPool(connectionFactories.size());
					inited = true;
				}
			}
		}
	}

	@Override
	public synchronized void start() {
		if (!started) {
			for (ConnectionFactory connectionFactory : connectionFactories) {
				Runnable r = new AcceptThread(connectionFactory);
				exec.submit(r);
			}
			started = true;
		}
	}

	@Override
	public synchronized void stop() {
		if (!exec.isShutdown())
			exec.shutdown();
	}

	@Override
	public String accept() {
		if (started) {
			try {
				return messages.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void setConfiguration(Map<String, String> configurations) {
		this.configurations = configurations;
	}

	private class AcceptThread implements Runnable {

		private final ConnectionFactory connectionFactory;

		private final AtomicBoolean shutdown = new AtomicBoolean(false);

		public AcceptThread(ConnectionFactory connectionFactory) {
			this.connectionFactory = connectionFactory;
		}

		@Override
		public void run() {
			Connection connection = null;
			Channel channel = null;
			try {
				connection = connectionFactory.newConnection();
				channel = connection.createChannel();
				channel.queueDeclare(queuename, false, false, false, null);

				QueueingConsumer consumer = new QueueingConsumer(channel);
				channel.basicConsume(queuename, true, consumer);
				while (!shutdown.get() && !Thread.currentThread().isInterrupted()) {
					Delivery delivery = consumer.nextDelivery();
					String message = new String(delivery.getBody());
					while (messages.size() >= 1000) {
						TimeUnit.SECONDS.sleep(3);
					}
					messages.put(message);
				}
				Thread.currentThread().interrupt();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (ShutdownSignalException e) {
				e.printStackTrace();
			} catch (ConsumerCancelledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				if (channel != null) {
					try {
						channel.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					}
				}
				if (connection != null) {
					try {
						connection.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private synchronized void shutdown() {
			if (!shutdown.get()) {
				shutdown.set(true);
			}
		}
	}

	@Override
	public void kill(String host) {
		if (threads.containsKey(host))
			threads.get(host).shutdown();
	}
	public static void main(String[] args) {
		Map<String, String> map=new HashMap<>();
		map.put("id", "100000");
		map.put("type", "product");
		map.put("objTypeId", "1");
		map.put("objId", "263080");
		
		ObjectMapper mapper=new ObjectMapper();
		try {
			String jsonstring=mapper.writeValueAsString(map);
			System.out.println(jsonstring);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
