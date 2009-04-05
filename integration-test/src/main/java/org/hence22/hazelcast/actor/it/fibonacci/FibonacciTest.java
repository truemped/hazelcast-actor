/* 
 * Copyright (c) 2009, Daniel Truemper. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.hence22.hazelcast.actor.it.fibonacci;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.hence22.hazelcast.actor.impl.ActorManager;
import org.hence22.hazelcast.actor.impl.ActorProxy;
import org.hence22.hazelcast.actor.impl.DefaultNamingStrategy;

/**
 * @author truemped@googlemail.com
 * 
 */
public class FibonacciTest {

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void main(String[] args) throws InterruptedException,
			ExecutionException {
		ActorManager<BigInteger, BigInteger> fibonacciActorManager = new ActorManager<BigInteger, BigInteger>(
				new DefaultNamingStrategy(), new FibonacciActorFactory());
		new Thread(fibonacciActorManager).start();

		ActorProxy<BigInteger, BigInteger> fibonacci = new ActorProxy<BigInteger, BigInteger>(
				new DefaultNamingStrategy(), FibonacciActor.class);

		// warm up call
		fibonacci.call(BigInteger.valueOf(3L)).get();
		
		long now = new Date().getTime();
		BigInteger fib3 = fibonacci.call(BigInteger.valueOf(3L)).get();
		long duration = new Date().getTime() - now;
		System.out.println(MessageFormat.format(
				"The fibonacci number of {0} is {1}", 3, fib3));
		System.out.println(MessageFormat
				.format("Computation of the 3rd Fibonacci number took {0} ms",
						duration));

		now = new Date().getTime();
		BigInteger fib8 = fibonacci.call(BigInteger.valueOf(8L)).get();
		duration = new Date().getTime() - now;
		System.out.println(MessageFormat.format(
				"The fibonacci number of {0} is {1}", 8, fib8));
		System.out.println(MessageFormat
				.format("Computation of the 8th Fibonacci number took {0} ms",
						duration));

		now = new Date().getTime();
		BigInteger fib10 = fibonacci.call(BigInteger.valueOf(10L)).get();
		duration = new Date().getTime() - now;
		System.out.println(MessageFormat.format(
				"The fibonacci number of {0} is {1}", 10, fib10));
		System.out.println(MessageFormat.format(
				"Computation of the 10th Fibonacci number took {0} ms",
				duration));

		fibonacciActorManager.shutdown();
		System.exit(0);
	}
}
