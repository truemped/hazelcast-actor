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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

/**
 * @author truemped@googlemail.com
 * 
 */
public class AbstractActorTest {

	/**
	 * @author truemped@googlemail.com
	 * 
	 */
	final class EchoActor extends AbstractActor<String, String> {

		/**
		 * Constructor for the echo actor.
		 */
		public EchoActor() {
			super(new DefaultNamingStrategy(), EchoActor.class, true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.hence22.hazelcast.actor.api.Actor#call(org.hence22.hazelcast.
		 * actor.api.InputMessage)
		 */
		@Override
		public String call(String input) {
			return input;
		}
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testCommunicationViaEchoActor() throws InterruptedException,
			ExecutionException {
		new EchoActor();

		ActorProxy<String, String> echoProxy = new ActorProxy<String, String>(
				new DefaultNamingStrategy(), EchoActor.class);

		Future<String> future = echoProxy.call("Test");
		assertEquals("Test", future.get());
	}

	/**
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testBulkLoadingOfTasks() throws InterruptedException, ExecutionException {

		new EchoActor();

		ActorProxy<String, String> echoProxy = new ActorProxy<String, String>(
				new DefaultNamingStrategy(), EchoActor.class);

		List<Future<String>> futures;
		List<String> strings = new ArrayList<String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7328631044879958719L;

			{
				this.add("Test1");
				this.add("Test2");
			}
		};
		
		futures = echoProxy.call(strings);
		
		assertEquals("Test1", futures.get(0).get());
		assertEquals("Test2", futures.get(1).get());

	}
}
