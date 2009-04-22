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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.hence22.hazelcast.actor.api.Director;
import org.hence22.hazelcast.actor.impl.ActorManager;
import org.hence22.hazelcast.actor.impl.DefaultNamingStrategy;
import org.hence22.hazelcast.actor.impl.DirectorImpl;

import com.hazelcast.core.Hazelcast;

/**
 * @author truemped@googlemail.com
 * 
 */
public class FibonacciTest {

	static HashMap<Integer, Long> DURATION_SIMPLE = new HashMap<Integer, Long>();
	static HashMap<Integer, Long> DURATION_ADVANCED = new HashMap<Integer, Long>();
	static ActorManager<FibonacciAdvancedActorCallParams, BigInteger> ADVANCED_ACTOR_MANAGER;
	static Director<FibonacciAdvancedActorCallParams, BigInteger> ADVANCED_FIBONACCI_ACTOR = new DirectorImpl<FibonacciAdvancedActorCallParams, BigInteger>(
			FibonacciAdvancedActor.class);

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			ExecutionException, IOException {

		if (args.length > 0 && args[0].toLowerCase().equals("-simple")) {

			// the simple tests
			testSimpleActor();
			testAdvancedActor();

		} else if (args.length > 0
				&& args[0].toLowerCase().equals("-distributed")) {

			// the distributed tests

			// start the advanced actor manager
			startAdvancedActorManager();

			Lock master = Hazelcast.getLock("actorMasterTestingLock");
			if (master.tryLock(100L, TimeUnit.MILLISECONDS)) {
				// I am the master, start the fibonacci tests:
				System.out.println("Press ENTER to start");
				System.in.read();
				callAdvancedFibonacciActor();
			} else {
				// wait to stop the slave manually
				System.out.println("Press ENTER to stop this slave");
				System.in.read();
			}
		} else {
			System.err.println("Usage:\n"
					+ "\t-simple-tUse the simple testings\n"
					+ "\t-distributed\tUse the distributed tests\n");
			System.exit(1);
		}

		FileChannel outChannel = new FileOutputStream(new File("simple.csv"))
				.getChannel();
		for (Integer i : DURATION_SIMPLE.keySet()) {
			outChannel.write(ByteBuffer.wrap(MessageFormat.format("{0};{1}\n",
					Math.pow(2, i), DURATION_SIMPLE.get(i)).getBytes()));
		}
		outChannel.close();

		outChannel = new FileOutputStream(new File("advanced.csv"))
				.getChannel();
		for (Integer i : DURATION_ADVANCED.keySet()) {
			outChannel.write(ByteBuffer.wrap(MessageFormat.format("{0};{1}\n",
					Math.pow(2, i), DURATION_ADVANCED.get(i)).getBytes()));
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

		Director<BigInteger, BigInteger> fibonacci = new DirectorImpl<BigInteger, BigInteger>(
				FibonacciSimpleActor.class);

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
	 * 
	 */
	static void startAdvancedActorManager() {
		if (ADVANCED_ACTOR_MANAGER == null) {
			ADVANCED_ACTOR_MANAGER = new ActorManager<FibonacciAdvancedActorCallParams, BigInteger>(
					new DefaultNamingStrategy(),
					new FibonacciAdvancedActorFactory());
			new Thread(ADVANCED_ACTOR_MANAGER).start();
		}
	}

	/**
	 * 
	 */
	static void stopAdvancedActorManager() {
		if (ADVANCED_ACTOR_MANAGER != null) {
			ADVANCED_ACTOR_MANAGER.shutdown();
		}
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	static void callAdvancedFibonacciActor() throws InterruptedException,
			ExecutionException {

		// warm up call
		ADVANCED_FIBONACCI_ACTOR.call(
				new FibonacciAdvancedActorCallParams(BigInteger.ONE,
						BigInteger.ZERO, BigInteger.ONE)).get();

		long now, duration, num;
		for (int i = 0; i < 8; i++) {
			num = new Double(Math.pow(2, i)).longValue();
			now = new Date().getTime();
			ADVANCED_FIBONACCI_ACTOR.call(
					new FibonacciAdvancedActorCallParams(BigInteger
							.valueOf(num), BigInteger.ZERO, BigInteger.ONE))
					.get();
			duration = new Date().getTime() - now;
			DURATION_ADVANCED.put(i, duration);
		}

	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	static void testAdvancedActor() throws InterruptedException,
			ExecutionException {

		startAdvancedActorManager();
		callAdvancedFibonacciActor();
		stopAdvancedActorManager();

	}
}
