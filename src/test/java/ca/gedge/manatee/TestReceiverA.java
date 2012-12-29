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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class TestReceiverA implements MessageReceiver {
	final Map<String, List<Object[]>> calls = new Hashtable<String, List<Object[]>>();

	public TestReceiverA() {
		calls.put(TestSender.MESSAGE_A, new ArrayList<Object[]>());
		calls.put(TestSender.MESSAGE_B, new ArrayList<Object[]>());
		calls.put(TestSender.MESSAGE_AB, new ArrayList<Object[]>());
	}

	@ReceiverMethod(senderClass=TestSender.class, message=TestSender.MESSAGE_A)
	public void messageA() {
		calls.get(TestSender.MESSAGE_A).add(new Object[0]);
	}

	@ReceiverMethod(senderClass=TestSender.class, message=TestSender.MESSAGE_AB)
	public void messageAB(String a, Integer b) {
		calls.get(TestSender.MESSAGE_AB).add(new Object[]{a, b});
	}
}
