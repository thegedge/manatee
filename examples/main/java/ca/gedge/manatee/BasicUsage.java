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

/**
 * An example program to show the basic usage of Manatee.
 * 
 * @see ReceiverA
 * @see ReceiverB
 * @see ReceiverAll
 * @see Sender
 */
public class BasicUsage {
	/**
	 * Entry point.
	 * 
	 * @param args program arguments
	 */
	public static void main(String [] args) {
		final ReceiverA testA = new ReceiverA();
		final ReceiverB testB = new ReceiverB();
		final ReceiverAll testAll = new ReceiverAll();
		final Sender testSender = new Sender();

		// We add instances of receivers whom we wish to receive messages 
		Sender.MESSENGER.addReceiver(testA);
		Sender.MESSENGER.addReceiver(testB);
		Sender.MESSENGER.addReceiver(testAll);

		testSender.sendMessageA();
		testSender.sendMessageB();
		testSender.sendMessageAB("First Param", 28);
	}
}
