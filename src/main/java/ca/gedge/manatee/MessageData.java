/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ca.gedge.manatee;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import ca.gedge.manatee.util.Pair;

/**
 * A class that describes messages sent by a message-sending class.
 */
class MessageData {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(MessageData.class.getName()); 

	/** The class that is sending the message */
	private Class<? extends MessageSender> senderClass;

	/** Mapping from message names to the classes of the arguments */
	private Map<String, Class<?>[]> senderMessages;

	/**
	 * Mapping from message name to a mapping of receivers and their
	 * receiving methods.
	 *
	 * TODO ArrayList of methods so that multiple methods in the same
	 *      receiver could receive
	 */
	private Map<String, Map<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>>> receiverMethods;

	/** Mapping from message receiver to a catchall method. */
	private Map<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>> receiverCatchallMethods;

	/**
	 * Default constructor.
	 *
	 * @param senderClass  the class which will send messages
	 */
	MessageData(Class<? extends MessageSender> senderClass) {
		this.senderClass = senderClass;
		this.senderMessages = new Hashtable<String, Class<?>[]>();
		this.receiverMethods = new Hashtable<String, Map<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>>>();
		this.receiverCatchallMethods = new Hashtable<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>>();

		// Add all messages from the receiver class
		for(Field field : senderClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(Message.class)) {
				if((field.getModifiers() & Modifier.STATIC) == 0) {
					LOGGER.warning(String.format("Message field `%s` in `%s` must be static", field.getName(), senderClass));
					continue;
				}

				Message msg = field.getAnnotation(Message.class);
				try {
					senderMessages.put(field.get(null).toString(), msg.signature());
				} catch (IllegalArgumentException e) {
					// should never happen 
				} catch (IllegalAccessException e) {
					LOGGER.warning(String.format("Unable to access message field `%s` in `%s`", field.getName(), senderClass)); 
				}
			}
		}
	}

	/**
	 * Adds a receiving method for a specified message.
	 * 
	 * @param receiver  the method which will be receiving this message
	 * 
	 * @return <code>true</code> if the given method can be successfully added as
	 *         a receiver for the given message, <code>false</code> otherwise.
	 */
	boolean addReceiverMethod(String msg, Method receiver) {
		final Class<? extends MessageReceiver> clz = receiver.getDeclaringClass().asSubclass(MessageReceiver.class);

		// Warnings
		if(!senderMessages.containsKey(msg)) {
			// Issue a warning if we don't yet know of this message
			LOGGER.warning(String.format(
				"Registering message \"%s:%s\" from \"%s:%s\" but this message is not yet known",
				senderClass.getName(), msg, clz.getName(), receiver.getName()
			));

			return false;
		} else if(!receiver.isVarArgs()) {
			// Issue a warning if we know the message, but the signature of
			// receiving method doesn't match that of the message
			Class<?>[] clazzes1 = senderMessages.get(msg);
			Class<?>[] clazzes2 = receiver.getParameterTypes();

			boolean match = (clazzes1.length == clazzes2.length);
			if(clazzes1.length == clazzes2.length) {
				for(int i = 0; i < clazzes1.length; ++i) {
					if(!clazzes2[i].isAssignableFrom(clazzes1[i])) {
						match = false;
						break;
					}
				}
			}

			if(!match) {
				LOGGER.warning(String.format(
					"Registering message \"%s:%s\" from \"%s:%s\" but its signature does not match",
					senderClass.getName(), msg, clz.getName(), receiver.getName()
				));
				return false;
			}
		}

		// Create a new map for the given message, if one doesn't already exist
		Map<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>> map = receiverMethods.get(msg);
		if(map == null) {
			map = new Hashtable<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>>();
			receiverMethods.put(msg, map);
		}

		//LOGGER.info(String.format("Adding \"%s:%s\" as a receiver for message \"%s:%s\"%n", clz.getName(), receiver.getName(), senderClass.getName(), msg));

		map.put(clz, createPair(receiver, msg));
		return true;
	}

	/**
	 * Adds a catchall method.
	 * 
	 * @param receiver  the method which will be receiving this message
	 */
	void addCatchallMethod(Method receiver) {
		final Class<? extends MessageReceiver> clz = receiver.getDeclaringClass().asSubclass(MessageReceiver.class);
		receiverCatchallMethods.put(clz, createPair(receiver, null));
	}

	/**
	 * Creates a pair between a receiver and its associated {@link ReceiverMethod}
	 * annotation.
	 * 
	 * @param receiver  the receiver method
	 * 
	 * @return the {@link Pair}
	 */
	private Pair<Method, ReceiverMethod> createPair(Method receiver, String message) {
		ReceiverMethod annotation = receiver.getAnnotation(ReceiverMethod.class);
		if(annotation == null) {
			// Potentially multiple registrations, so find the right one
			final ReceiverMethods methods = receiver.getAnnotation(ReceiverMethods.class);
			for(ReceiverMethod receiverMethod : methods.messages()) {
				if(senderClass.equals(receiverMethod.senderClass())) {
					if(message == null && receiverMethod.isCatchall()
					   || receiverMethod.message().equals(message)) 
					{
						annotation = receiverMethod;
						break;
					}
				}
			}
		}

		return new Pair<Method, ReceiverMethod>(receiver, annotation);
	}

	/**
	 * Fetches a {@link Method} for a given message name and receiver class. 
	 * 
	 * @param msg  the message name
	 * @param clz  the {@link MessageReceiver} class
	 * @return  the {@link Method} associated with the given message name
	 *          and receiver class, if one exists. <code>null</code>
	 *          otherwise.
	 */
	Pair<Method, ReceiverMethod> getReceiverMethod(String msg, Class<? extends MessageReceiver> clz) {
		if(receiverMethods.containsKey(msg)) {
			final Map<Class<? extends MessageReceiver>, Pair<Method, ReceiverMethod>> methods = receiverMethods.get(msg);
			if(methods.containsKey(clz))
				return methods.get(clz);
		}
		return receiverCatchallMethods.get(clz);
	}

	/**
	 * Gets whether or not this instance handles the given message.
	 * 
	 *  @return  <code>true</code> if the message can be handled by this
	 *           instance, <code>false</code> otherwise
	 */
	boolean handlesMessage(String msg) {
		return senderMessages.containsKey(msg);
	}
}
