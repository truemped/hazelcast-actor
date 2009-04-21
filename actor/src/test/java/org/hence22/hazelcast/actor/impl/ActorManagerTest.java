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
package org.hence22.hazelcast.actor.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.hence22.hazelcast.actor.api.ActorWorkerFactory;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.junit.Test;

import com.hazelcast.core.ITopic;

/**
 * @author truemped@googlemail.com
 * 
 */
public class ActorManagerTest {

	/**
	 * A very simple echo actor for use within an executor service.
	 * 
	 * @author truemped@googlemail.com
	 */
	private class EchoActor extends AbstractActorWorker<String, String> {

		/**
		 * @param inputMsg
		 * @param topic
		 */
		protected EchoActor(InputMessage<String> inputMsg,
				ITopic<OutputMessage<String>> topic) {
			super(inputMsg, topic);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.hence22.hazelcast.actor.api.Actor#call(java.io.Serializable)
		 */
		@Override
		public String call(String input) {
			return input;
		}

	}

	/**
	 * The {@link EchoActor} factory.
	 * 
	 * @author truemped@googlemail.com
	 */
	private class EchoActorFactory implements
			ActorWorkerFactory<String, String> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.hence22.hazelcast.actor.api.ActorWorkerFactory#getClazz()
		 */
		@Override
		public Class<? extends AbstractActorWorker<String, String>> getClazz() {
			return EchoActor.class;
		}

		@Override
		public AbstractActorWorker<String, String> newInstance(
				InputMessage<String> input, ITopic<OutputMessage<String>> topic) {
			return new EchoActor(input, topic);
		}

	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testSingleCall() throws InterruptedException,
			ExecutionException {
		ActorManager<String, String> echoManager = new ActorManager<String, String>(
				new DefaultNamingStrategy(), new EchoActorFactory());
		Thread manager = new Thread(echoManager);
		manager.start();

		DirectorImpl<String, String> echoProxy = new DirectorImpl<String, String>(
				new DefaultNamingStrategy(), EchoActor.class);

		Future<String> result = echoProxy.call("Test");
		assertEquals("Test", result.get());
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testBulkCall() throws InterruptedException, ExecutionException {
		ActorManager<String, String> echoManager = new ActorManager<String, String>(
				new DefaultNamingStrategy(), new EchoActorFactory());
		Thread manager = new Thread(echoManager);
		manager.start();

		DirectorImpl<String, String> echoProxy = new DirectorImpl<String, String>(
				new DefaultNamingStrategy(), EchoActor.class);

		List<Future<String>> futures;
		List<String> strings = new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1778595120345529621L;

			{
				this.add("Test1");
				this.add("Test2");
				this.add("Test3");
				this.add("Test4");
			}
		};

		futures = echoProxy.call(strings);

		assertEquals("Test1", futures.get(0).get());
		assertEquals("Test2", futures.get(1).get());
		assertEquals("Test3", futures.get(2).get());
		assertEquals("Test4", futures.get(3).get());

	}
}
