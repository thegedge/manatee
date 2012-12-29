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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.manatee.util.Pair;
import ca.gedge.manatee.util.WeakList;

/**
 * The class used to register any messages sent by a {@link MessageSender}
 * and register messages accepted by a {@link MessageReceiver}. The general
 * flow goes as follows:
 * <ol>
 *   <li>Message sending classes register themselves</li>
 *   <li>Message receiving classes register themselves</li>
 *   <li>Instances of message receiving classes register themselves as
 *       accepting messages.</li>
 * </ol>
 * A note to anyone using this class is that instances are stored using
 * weak references. In other words, there is no need to worry about instances
 * not being garbage collected due to the message delivery system holding on 
 * to a strong reference.
 * 
 * TODO document unchecked exceptions thrown
 * TODO automate registration
 * TODO have catchall methods receive message name as first argument?
 */
public class MessageDeliverySystem {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(MessageDeliverySystem.class.getName()); 

	/** Key for the default messaging system */
	private static final String DEFAULT_SYS_KEY = "<<default>>";

	/** Mapping from key to system */
	private static Map<String, MessageDeliverySystem> systems = new TreeMap<String, MessageDeliverySystem>();

	/** Set of sending classes that have been registered */
	private HashSet<Class<? extends MessageSender>> registeredSenders;

	/** Set of receiving classes that have been registered */
	private HashSet<Class<? extends MessageReceiver>> registeredReceivers;

	/**
	 * Gets the default message delivery system.
	 * 
	 * @return  a {@link MessageDeliverySystem} instance
	 */
	public static MessageDeliverySystem getInstace() {
		return getInstance(DEFAULT_SYS_KEY);
	}

	/**
	 * Gets the message delivery system for a class. This is just a shortcut
	 * for <code>getDeliverySystem(clz.getName())</code>.
	 * 
	 * @param clz  the class
	 * 
	 * @return  a {@link MessageDeliverySystem} instance
	 * @see #getInstance(String)
	 */
	public static MessageDeliverySystem getInstance(Class<?> clz) {
		return getInstance(clz.getName());
	}

	/**
	 * Gets the message delivery system specified by a given name.
	 * 
	 * @param name  the name 
	 * 
	 * @return  a {@link MessageDeliverySystem} instance
	 */
	public static synchronized MessageDeliverySystem getInstance(String name) {
		if(!systems.containsKey(name))
			systems.put(name, new MessageDeliverySystem());
		return systems.get(name);
	}

	/** Map from sender classes to corresponding messaging data */
	private HashMap<Class<? extends MessageSender>, MessageData> messageMap;

	/** Receiver instances accepting messages */
	private List<MessageReceiver> receivers;

	/** Whether or not messages are sent */
	private boolean enabled = true;

	/**
	 * Default constructor
	 */
	public MessageDeliverySystem() {
		this.messageMap = new HashMap<Class<? extends MessageSender>, MessageData>();
		this.receivers = new WeakList<MessageReceiver>();
		this.registeredSenders = new HashSet<Class<? extends MessageSender>>();
		this.registeredReceivers = new HashSet<Class<? extends MessageReceiver>>();
	}

	/**
	 * Sets the enabled state of this delivery system.
	 *
	 * @param enabled  if <code>true</code>, this system will emit messages.
	 *                 Otherwise, this system will emit no messages.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Registers a message that will be sent by a class.
	 * 
	 * @param sender  the sending class
	 */
	public void registerSender(Class<? extends MessageSender> sender) {
		if(!registeredSenders.contains(sender)) {
			getData(sender); // creation of the MessageData instance registers messages
			registeredSenders.add(sender);
		}
	}

	/**
	 * Registers a receiving class. Currently the only way of registering a receiver.
	 * All methods having a {@link ReceiverMethod} annotation will be registered.
	 * 
	 * @param receiver  the receiver class
	 */
	public void registerReceiver(Class<? extends MessageReceiver> receiver) {
		if(!registeredReceivers.contains(receiver)) {
			for(Method method : receiver.getDeclaredMethods()) {
				if(method.isAnnotationPresent(ReceiverMethod.class)) {
					final ReceiverMethod meth = method.getAnnotation(ReceiverMethod.class);
					final MessageData msgData = getData(meth.senderClass());
					if(meth.isCatchall())
						msgData.addCatchallMethod(method);
					else
						msgData.addReceiverMethod(meth.message(), method);
				}

				if(method.isAnnotationPresent(ReceiverMethods.class)) {
					for(ReceiverMethod meth : method.getAnnotation(ReceiverMethods.class).messages()) {
						final MessageData msgData = getData(meth.senderClass());
						if(meth.isCatchall())
							msgData.addCatchallMethod(method);
						else
							msgData.addReceiverMethod(meth.message(), method);
					}
				}
			}

			registeredReceivers.add(receiver);
		}
	}

	/**
	 * Registers a new receiver.
	 * 
	 * @param receiver  the receiver
	 */
	public void addReceiver(MessageReceiver receiver) {
		if(receiver != null) {
			registerReceiver(receiver.getClass());
			receivers.add(receiver);
		}
	}

	/**
	 * Unregisters a receiver.
	 * 
	 * @param receiver  the receiver
	 * @return  <code>true</code> if the receiver is actually registered,
	 *          <code>false</code> otherwise.
	 */
	public boolean removeReceiver(MessageReceiver receiver) {
		return receivers.remove(receiver);
	}

	/**
	 * Sends a message to all accepting receivers.
	 * 
	 * @param sender  the {@link MessageSender} instance sending the message
	 * @param msg     the message name
	 * @param data    the data accompanying the message
	 */
	public void sendMessage(MessageSender sender, String msg, Object... data) {
		if(!enabled) return;

		// Check superclasses, if necessary, to find a registered class that handles
		// the given message.
		Class<?> current = sender.getClass();
		MessageData msgData = null;
		while(current != null && msgData == null) {
			// Make sure this one understands the message
			msgData = messageMap.get(current);
			if(msgData != null && msgData.handlesMessage(msg))
				break;

			current = current.getSuperclass();
		}

		//
		if(msgData != null) {
			for(MessageReceiver receiver : receivers) {
				// Make sure this receiver accepts this sender+message combo
				final Pair<Method, ReceiverMethod> pair = msgData.getReceiverMethod(msg, receiver.getClass());
				if(pair != null) {
					final Method m = pair.getFirst();
					final ReceiverMethod annotation = pair.getSecond();
					try {
						if(annotation.isCatchall()) {
							if(m.isVarArgs()) {
								m.invoke(receiver, new Object[] { msg, data });
							} else {
								m.invoke(receiver, msg, data);
							}
						} else {
							if(m.isVarArgs()) {
								m.invoke(receiver, new Object[] { data });
							} else {
								m.invoke(receiver, data);
							}
						}
					} catch(IllegalArgumentException e) {
						String method = m.getDeclaringClass().getName() + " : " + m.getName();
						LOGGER.log(Level.WARNING, "In " + method, e);
					} catch(IllegalAccessException e) {
						String method = m.getDeclaringClass().getName() + " : " + m.getName();
						LOGGER.log(Level.WARNING, "No access to " + method, e);
					} catch(InvocationTargetException e) {
						LOGGER.log(Level.WARNING, "Exception during method call", e);
					}
				}
			}
		}
	}

	/**
	 * Gets the message data associated with a class.
	 * 
	 * @return the message data instance (never <code>null</code>)
	 */
	MessageData getData(Class<? extends MessageSender> clz) {
		MessageData msgData = messageMap.get(clz);
		if(msgData == null) {
			msgData = new MessageData(clz);
			messageMap.put(clz, msgData);
		}
		return msgData;
	}
}
