package net.zsy.indexcreator.mq.sender;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import net.zsy.indexcreator.mq.MessageQueue;
import net.zsy.indexcreator.mq.Sender;

public class RabbitMQSender implements Sender {

	private Map<String, String> configurations;

	private ConnectionFactory connectionFactory;

	private String queuename;

	private Thread sendThread;

	private Boolean inited = false;
	private Boolean started = false;

	private BlockingQueue<String> queue;

	@Override
	public void init() {
		if (!inited) {
			synchronized (inited) {
				if (!inited) {
					String hostport = configurations.get(MessageQueue.HOSTPORT);
					if (StringUtils.isBlank(hostport)) {
						throw new RuntimeException("hostport not configured.");
					}
					String[] hps = hostport.split(":");

					String username = configurations.get(MessageQueue.USERNAME);
					String password = configurations.get(MessageQueue.PASSWORD);

					queuename = configurations.get(MessageQueue.QUEUENAME);

					connectionFactory = new ConnectionFactory();
					connectionFactory.setHost(hps[0]);
					connectionFactory.setPort(Integer.valueOf(hps[1]));
					connectionFactory.setUsername(username);
					connectionFactory.setPassword(password);

					queue = new LinkedBlockingQueue<String>();

					inited = true;
				}
			}
		}
	}

	@Override
	public synchronized void start() {
		if (inited && !started) {
			sendThread = new Thread(new SendThread());
			sendThread.start();
			started = true;
		}
	}

	@Override
	public synchronized void stop() {
		if (started) {
			sendThread.interrupt();
			started = false;
		}
	}

	@Override
	public void send(String message) {
		try {
			queue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(List<String> message) {
		queue.addAll(message);
	}

	@Override
	public void setConfiguration(Map<String, String> configurations) {
		this.configurations = configurations;
	}

	private class SendThread implements Runnable {

		@Override
		public void run() {
			Connection connection = null;
			Channel channel = null;

			try {
				connection = connectionFactory.newConnection();
				channel = connection.createChannel();
				channel.queueDeclare(queuename, false, false, false, null);

				while (started && !Thread.currentThread().isInterrupted()) {
					String message = queue.take();
					System.out.println("take:" + message);
					channel.basicPublish("", queuename, null, message.getBytes());
					System.out.println("done to " + queuename);
				}
				Thread.currentThread().interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
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

	@Override
	public BlockingQueue<String> getQueue() {
		return queue;
	}

}
