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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hence22.hazelcast.actor.api.Actor;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.api.QueueNamingStrategy;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;

/**
 * A simple proxy class for actors.
 * 
 * In order to use the actor in the caller use this {@link ActorProxy}. An
 * example usage for the EchoActor described in {@link Actor} could be:
 * 
 * <code>
 * ActorProxy&lt:String, String&gt: echoProxy = new ActorProxy&lt:String, String&gt:( new DefaultNamingStrategy(), EchoActor.class );
 * Future&lt:String&gt: echoFuture = echoProxy.call( "Test" );
 * System.out.println( echoFuture.get() );
 * </code>
 * 
 * @author truemped@googlemail.com
 */
public class ActorProxy<X extends Serializable, Y extends Serializable>
		implements MessageListener<OutputMessage<Y>> {

	/**
	 * The actor's input queue.
	 */
	private final BlockingQueue<InputMessage<X>> inputQueue;

	/**
	 * The actor's output topic.
	 */
	private final ITopic<OutputMessage<Y>> outputTopic;

	/**
	 * The hashmap containing the actor's results.
	 */
	private final ConcurrentHashMap<Long, Y> resultMap = new ConcurrentHashMap<Long, Y>();

	/**
	 * The list of cancelled calls to actors.
	 */
	private final ConcurrentSkipListSet<Long> myCalls = new ConcurrentSkipListSet<Long>();

	/**
	 * @param strategy
	 * @param actor
	 */
	public ActorProxy(QueueNamingStrategy strategy, Class<?> actor) {
		this(strategy.getInputQueueNameForActor(actor), strategy
				.getOutputTopicNameForActor(actor));
	}

	/**
	 * @param inputQueueName
	 * @param outputTopicName
	 */
	public ActorProxy(String inputQueueName, String outputTopicName) {
		this.inputQueue = Hazelcast.getQueue(inputQueueName);
		this.outputTopic = Hazelcast.getTopic(outputTopicName);
		this.outputTopic.addMessageListener(this);
	}

	/**
	 * @param input
	 * @return
	 */
	public Future<Y> call(X input) {
		InputMessage<X> msg = new InputMessage<X>(input);
		this.inputQueue.offer(msg);
		this.myCalls.add(msg.getMessageId());
		return new ActorFuture(msg.getMessageId(), this.resultMap,
				this.myCalls);
	}

	/**
	 * Submit a list of input messages to the actor.
	 * 
	 * @param inputs
	 *            The list of input messages.
	 * @return The list of futures.
	 */
	public List<Future<Y>> call(List<X> inputs) {
		List<Future<Y>> futures = new ArrayList<Future<Y>>();
		for (X input : inputs) {
			futures.add(this.call(input));
		}
		return futures;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hazelcast.core.MessageListener#onMessage(java.lang.Object)
	 */
	@Override
	public void onMessage(OutputMessage<Y> msg) {
		if (this.myCalls.remove(msg.getMessageId())) {
			this.resultMap.put(msg.getMessageId(), msg.getMessage());
			synchronized (this.resultMap) {
				this.resultMap.notifyAll();
			}
		}
	}

	/**
	 * The {@link Future} representing a call to an {@link Actor}.
	 * 
	 * @author truemped@googlemail.com
	 * @param <Y>
	 */
	public final class ActorFuture implements Future<Y> {

		/**
		 * The msgId of the underlying call.
		 */
		private long msgId;

		/**
		 * The actor's result.
		 */
		private Y result;

		/**
		 * <b>True</b> if the call has been canceled.
		 */
		private boolean cancelled;

		/**
		 * The result map.
		 */
		private ConcurrentHashMap<Long, Y> resultMap;

		/**
		 * The list of canceled calls.
		 */
		private ConcurrentSkipListSet<Long> myCalls;

		/**
		 * @param msgId
		 */
		public ActorFuture(long msgId, ConcurrentHashMap<Long, Y> resultMap,
				ConcurrentSkipListSet<Long> myCalls) {
			this.msgId = msgId;
			this.resultMap = resultMap;
			this.myCalls = myCalls;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Future#cancel(boolean)
		 */
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if (this.result != null || !this.myCalls.remove(this.msgId)) {
				return false;
			}
			return this.cancelled;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Future#get()
		 */
		@Override
		public Y get() throws InterruptedException, ExecutionException {
			if (this.result != null) {
				return this.result;
			}
			while (this.result == null) {
				try {
					this.result = get(30, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					// this might happen while we wait for the result.
				}
			}
			return this.result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Future#get(long,
		 * java.util.concurrent.TimeUnit)
		 */
		@Override
		public Y get(long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			if (!this.resultMap.containsKey(this.msgId)) {
				synchronized (this.resultMap) {
					this.resultMap.wait(unit.toMillis(timeout));
				}
				if (!this.resultMap.containsKey(this.msgId)) {
					throw new TimeoutException("Waiting for the result for "
							+ unit.toMillis(timeout)
							+ " milliseconds timed out");
				}
			}

			if (this.resultMap.containsKey(this.msgId)) {
				this.result = this.resultMap.remove(this.msgId);
			}

			return this.result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Future#isCancelled()
		 */
		@Override
		public boolean isCancelled() {
			return this.myCalls.contains(this.msgId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Future#isDone()
		 */
		@Override
		public boolean isDone() {
			return this.result != null;
		}

	}
}
