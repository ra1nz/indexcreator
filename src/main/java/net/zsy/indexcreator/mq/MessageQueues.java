package net.zsy.indexcreator.mq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class MessageQueues {

	private MessageQueues() {
	}

	private static Accepter accepter = null;
	private static Sender sender = null;

	private static Object accepterLock = new Object();
	private static Object senderLock = new Object();

	public static Accepter getAccepter() {
		if (accepter == null) {
			synchronized (accepterLock) {
				if (accepter == null) {
					initAccepter();
				}
			}
		}
		return accepter;
	}

	public static Sender getSender() {
		if (sender == null) {
			synchronized (senderLock) {
				if (sender == null) {
					initSender();
				}
			}
		}
		return sender;
	}

	private static final String PROPERTY_ACCEPTER = "acceptmq.properties";
	private static final String PROPERTY_SENDER = "sendmq.properties";

	private static final String PACKAGE_ACCEPTER = "net.zsy.indexcreator.mq.accepter";
	private static final String PACKAGE_SENDER = "net.zsy.indexcreator.mq.sender";
	private static final String SUFFIX_ACCEPTER = "Accepter";
	private static final String SUFFIX_SENDER = "Sender";

	private static void initAccepter() {
		Map<String, String> configurations = loadProperty(PROPERTY_ACCEPTER);
		accepter = getAccepterInstance(configurations.get(MessageQueue.NAME).toString());
		accepter.setConfiguration(configurations);
		accepter.init();
	}

	private static void initSender() {
		Map<String, String> configurations = loadProperty(PROPERTY_SENDER);
		sender = getSenderInstance(configurations.get(MessageQueue.NAME).toString());
		sender.setConfiguration(configurations);
		sender.init();
	}

	private static Map<String, String> loadProperty(String propertyName) {
		InputStream in = MessageQueues.class.getResourceAsStream("/" + propertyName);
		Properties properties = new Properties();
		Map<String, String> configurations = new HashMap<String, String>();
		try {
			properties.load(in);
			Enumeration<Object> keys = properties.keys();

			while (keys.hasMoreElements()) {
				Object object = (Object) keys.nextElement();
				configurations.put(object.toString(), properties.getProperty(object.toString()));
			}
			System.out.println(propertyName + "properties loaded:");
			System.out.println(configurations);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return configurations;
	}

	private static Accepter getAccepterInstance(String name) {
		String className = PACKAGE_ACCEPTER + "." + name + SUFFIX_ACCEPTER;
		try {
			Class<?> c = Class.forName(className);
			return (Accepter) c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Accepter class not found:" + className);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Sender getSenderInstance(String name) {
		String className = PACKAGE_SENDER + "." + name + SUFFIX_SENDER;
		try {
			Class<?> c = Class.forName(className);
			return (Sender) c.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Sender class not found:" + className);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
}
