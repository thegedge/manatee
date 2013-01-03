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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the {@link MessageData} class.
 */
public class TestMessageData {
	private MessageData data;

	@Before
	public void initialize() {
		Logger.getLogger(MessageData.class.getName()).setLevel(Level.OFF);
		data = new MessageData(TestSender.class);
	}

	@Test
	public void testConstruction() {
		assertTrue(data.handlesMessage(TestSender.MESSAGE_A));
		assertTrue(data.handlesMessage(TestSender.MESSAGE_B));
		assertTrue(data.handlesMessage(TestSender.MESSAGE_AB));
		assertFalse(data.handlesMessage("abc123"));
	}

	@Test
	public void testAddReceiverMethods() {
		try {
			final Method messageAMethod = TestReceiverA.class.getDeclaredMethod("messageA");
			data.addReceiverMethod(TestSender.MESSAGE_A, messageAMethod);
			assertEquals(messageAMethod, data.getReceiverMethod(TestSender.MESSAGE_A, TestReceiverA.class).getFirst());

			final Method catchallMethod = TestReceiverAll.class.getDeclaredMethod("messageCatchall", String.class, Object[].class);
			data.addCatchallMethod(catchallMethod);
			assertEquals(catchallMethod, data.getReceiverMethod(TestSender.MESSAGE_A, TestReceiverAll.class).getFirst());
			assertEquals(catchallMethod, data.getReceiverMethod(TestSender.MESSAGE_B, TestReceiverAll.class).getFirst());
			assertEquals(catchallMethod, data.getReceiverMethod(TestSender.MESSAGE_AB, TestReceiverAll.class).getFirst());
		} catch(Exception exc) {
			exc.printStackTrace();
			fail(exc.getMessage());
		}
	}

	@Test
	public void testAddMethodWithWrongSignature() {
		try {
			final Method messageAMethod = TestReceiverA.class.getDeclaredMethod("messageA");
			assertFalse(data.addReceiverMethod(TestSender.MESSAGE_AB, messageAMethod));
		} catch(Exception exc) {
			fail(exc.getMessage());
		}
	}
}
