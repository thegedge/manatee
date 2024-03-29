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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the {@link MessageDeliverySystem} class.
 */
public class TestMessageDeliverySystem {
	private MessageDeliverySystem system;

	private TestSender sender;
	private TestReceiverA receiverA;
	private TestReceiverB receiverB;
	private TestReceiverAll receiverAll;
	private TestReceiverMethods receiverMethods;

	@Before
	public void initialize() {
		Logger.getLogger(MessageDeliverySystem.class.getName()).setLevel(Level.OFF);
		system = MessageDeliverySystem.getInstace();

		sender = new TestSender();
		receiverA = new TestReceiverA();
		receiverB = new TestReceiverB();
		receiverAll = new TestReceiverAll();
		receiverMethods = new TestReceiverMethods();

		system.addReceiver(receiverA);
		system.addReceiver(receiverB);
		system.addReceiver(receiverAll);
		system.addReceiver(receiverMethods);
	}

	@Test
	public void testRegisterSender() {
		final MessageData data = system.getData(TestSender.class);
		assertTrue(data.handlesMessage(TestSender.MESSAGE_A));
		assertTrue(data.handlesMessage(TestSender.MESSAGE_B));
		assertTrue(data.handlesMessage(TestSender.MESSAGE_AB));
	}

	@Test
	public void testRegisterReceiver() {
		final MessageData data = system.getData(TestSender.class);
		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_A, TestReceiverA.class));
		assertNull(data.getReceiverMethod(TestSender.MESSAGE_B, TestReceiverA.class));
		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_AB, TestReceiverA.class));

		assertNull(data.getReceiverMethod(TestSender.MESSAGE_A, TestReceiverB.class));
		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_B, TestReceiverB.class));
		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_AB, TestReceiverB.class));

		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_A, TestReceiverAll.class));
		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_B, TestReceiverAll.class));
		assertNotNull(data.getReceiverMethod(TestSender.MESSAGE_AB, TestReceiverAll.class));
	}

	@Test
	public void testSendMessageA() {
		system.sendMessage(sender, TestSender.MESSAGE_A);
		assertEquals(1, receiverA.calls.get(TestSender.MESSAGE_A).size());
		assertEquals(0, receiverB.calls.get(TestSender.MESSAGE_A).size());
		assertEquals(1, receiverAll.calls.get(TestSender.MESSAGE_A).size());
	}

	@Test
	public void testSendMessageB() {
		system.sendMessage(sender, TestSender.MESSAGE_B);
		assertEquals(0, receiverA.calls.get(TestSender.MESSAGE_B).size());
		assertEquals(1, receiverB.calls.get(TestSender.MESSAGE_B).size());
		assertEquals(1, receiverAll.calls.get(TestSender.MESSAGE_B).size());
	}

	@Test
	public void testSendMessageAB() {
		system.sendMessage(sender, TestSender.MESSAGE_AB, "Foo", 12345);

		assertEquals(1, receiverA.calls.get(TestSender.MESSAGE_AB).size());
		assertEquals(1, receiverB.calls.get(TestSender.MESSAGE_AB).size());
		assertEquals(1, receiverAll.calls.get(TestSender.MESSAGE_AB).size());

		final Object[] expected = new Object[]{"Foo", 12345};
		assertArrayEquals(expected, receiverA.calls.get(TestSender.MESSAGE_AB).get(0));
		assertArrayEquals(expected, receiverB.calls.get(TestSender.MESSAGE_AB).get(0));
		assertArrayEquals(expected, receiverAll.calls.get(TestSender.MESSAGE_AB).get(0));
	}

	@Test
	public void testSendMultipleMessages() {
		final Object[][] messageParams = new Object[][] {
				new Object[]{"Foo", 12345},
				new Object[]{"Bar", 54321},
				new Object[]{"Messaging", 12759812},
		};

		for(Object[] params : messageParams)
			system.sendMessage(sender, TestSender.MESSAGE_AB, params);

		assertEquals(messageParams.length, receiverA.calls.get(TestSender.MESSAGE_AB).size());
		assertEquals(messageParams.length, receiverB.calls.get(TestSender.MESSAGE_AB).size());
		assertEquals(messageParams.length, receiverAll.calls.get(TestSender.MESSAGE_AB).size());

		for(int index = 0; index < messageParams.length; ++index) {
			assertArrayEquals(messageParams[index], receiverA.calls.get(TestSender.MESSAGE_AB).get(index));
			assertArrayEquals(messageParams[index], receiverB.calls.get(TestSender.MESSAGE_AB).get(index));
			assertArrayEquals(messageParams[index], receiverAll.calls.get(TestSender.MESSAGE_AB).get(index));
		}
	}

	@Test
	public void testReceiverMethods() {
		final String[] messages = new String[] {
				TestSender.MESSAGE_A,
				TestSender.MESSAGE_B,
				TestSender.MESSAGE_AB,
				TestSender.MESSAGE_A,
				TestSender.MESSAGE_A,
				TestSender.MESSAGE_AB,
		};

		final Object[][] messageParams = new Object[][] {
				new Object[0],
				new Object[0],
				new Object[]{"Foo", 12345},
				new Object[0],
				new Object[0],
				new Object[]{"Bar", 54321},
		};

		for(int index = 0; index < messageParams.length; ++index)
			system.sendMessage(sender, messages[index], messageParams[index]);

		assertEquals(5, receiverMethods.calls.size()); // expected = messageParams.length - (# of MESSAGE_B)
		for(int index = 0, index2 = 0; index < messageParams.length; ++index) {
			if(!TestSender.MESSAGE_B.equals(messages[index2])) {
				assertArrayEquals(messageParams[index], receiverMethods.calls.get(index));
				++index2;
			}
		}
	}

	@Test
	public void testEnabled() {
		system.setEnabled(false);
		system.sendMessage(sender, TestSender.MESSAGE_A);
		system.sendMessage(sender, TestSender.MESSAGE_A);
		assertEquals(0, receiverA.calls.get(TestSender.MESSAGE_A).size());
		assertEquals(0, receiverB.calls.get(TestSender.MESSAGE_A).size());
		assertEquals(0, receiverAll.calls.get(TestSender.MESSAGE_A).size());
	}
}
