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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.hence22.hazelcast.actor.impl.ActorManager;
import org.hence22.hazelcast.actor.impl.ActorProxy;
import org.hence22.hazelcast.actor.impl.DefaultNamingStrategy;

/**
 * @author truemped@googlemail.com
 * 
 */
public class FibonacciTest {

	static HashMap<Integer, Long> DURATION_SIMPLE = new HashMap<Integer, Long>();
	static HashMap<Integer, Long> DURATION_ADVANCED = new HashMap<Integer, Long>();

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			ExecutionException, IOException {

		testSimpleActor();
		testAdvancedActor();

		FileChannel outChannel = new FileOutputStream(new File("simple.csv"))
				.getChannel();
		for (Integer i : DURATION_SIMPLE.keySet()) {
			outChannel.write(ByteBuffer.wrap(MessageFormat.format("{0};{1}\n", Math.pow(2, i),
					DURATION_SIMPLE.get(i)).getBytes()));
		}
		outChannel.close();

		outChannel = new FileOutputStream(new File("advanced.csv"))
				.getChannel();
		for (Integer i : DURATION_ADVANCED.keySet()) {
			outChannel.write(ByteBuffer.wrap(MessageFormat.format("{0};{1}\n", Math.pow(2, i),
					DURATION_ADVANCED.get(i)).getBytes()));
		}

		System.exit(0);
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	static void testSimpleActor() throws InterruptedException,
			ExecutionException {
		ActorManager<BigInteger, BigInteger> fibonacciActorManager = new ActorManager<BigInteger, BigInteger>(
				new DefaultNamingStrategy(), new FibonacciSimpleActorFactory());
		new Thread(fibonacciActorManager).start();

		ActorProxy<BigInteger, BigInteger> fibonacci = new ActorProxy<BigInteger, BigInteger>(
				new DefaultNamingStrategy(), FibonacciSimpleActor.class);

		// warm up call
		fibonacci.call(BigInteger.valueOf(3L)).get();

		long now, duration, num;
		for (int i = 0; i < 5; i++) {
			num = new Double(Math.pow(2, i)).longValue();
			now = new Date().getTime();
			fibonacci.call(BigInteger.valueOf(num)).get();
			duration = new Date().getTime() - now;
			DURATION_SIMPLE.put(i, duration);
		}

		fibonacciActorManager.shutdown();
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	static void testAdvancedActor() throws InterruptedException,
			ExecutionException {
		ActorManager<FibonacciAdvancedActorCallParams, BigInteger> fibonacciActorManager = new ActorManager<FibonacciAdvancedActorCallParams, BigInteger>(
				new DefaultNamingStrategy(),
				new FibonacciAdvancedActorFactory());
		new Thread(fibonacciActorManager).start();

		ActorProxy<FibonacciAdvancedActorCallParams, BigInteger> fibonacci = new ActorProxy<FibonacciAdvancedActorCallParams, BigInteger>(
				new DefaultNamingStrategy(), FibonacciAdvancedActor.class);

		// warm up call
		fibonacci.call(
				new FibonacciAdvancedActorCallParams(BigInteger.ONE,
						BigInteger.ZERO, BigInteger.ONE)).get();

		long now, duration, num;
		for (int i = 0; i < 8; i++) {
			num = new Double(Math.pow(2, i)).longValue();
			now = new Date().getTime();
			fibonacci.call(
					new FibonacciAdvancedActorCallParams(BigInteger
							.valueOf(num), BigInteger.ZERO, BigInteger.ONE))
					.get();
			duration = new Date().getTime() - now;
			DURATION_ADVANCED.put(i, duration);
		}

		fibonacciActorManager.shutdown();
	}
}
