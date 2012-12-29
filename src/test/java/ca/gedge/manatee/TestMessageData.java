/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
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
			assertFalse(data.addReceiverMethod(Sender.MESSAGE_AB, messageAMethod));
		} catch(Exception exc) {
			fail(exc.getMessage());
		}
	}
}
