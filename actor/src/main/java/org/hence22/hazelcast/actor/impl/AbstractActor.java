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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hence22.hazelcast.actor.api.Actor;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.api.QueueNamingStrategy;
import org.hence22.hazelcast.actor.api.Stoppable;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;

/**
 * The base implementation of an actor.
 * 
 * In order to create your own actor that does some work, extend this class.
 * 
 * <code>
 * public class EchoActor extends AbstractActor&lt;String, String&gt; {
 *     public EchoActor() {
 *         super(new DefaultNamingStrategy(), EchoActor.class);
 *     }
 * 
 * @Override public String call(String input) { return input; } } </code>
 * 
 *           Now you can start using the actor via the {@link DirectorImpl}.
 * 
 * @param <X> The type of the actor's parameter message.
 * @param <Y> The type of the actor"s response message.
 *
 * @author truemped@googlemail.com
 */
public abstract class AbstractActor<X extends Serializable, Y extends Serializable>
		implements Actor<X, Y>, Runnable, Stoppable {

	/**
	 * The name of the actor's input queue.
	 */
	private final String inputQueueName;

	/**
	 * This actor's input queue.
	 */
	private final BlockingQueue<InputMessage<X>> inputQueue;

	/**
	 * This actor's output queue.
	 */
	private final ITopic<OutputMessage<Y>> outputTopic;

	/**
	 * If true we should shutdown this actor.
	 */
	private volatile boolean shutdownRequested = false;

	/**
	 * The thread count of threads running this actor.
	 */
	private final AtomicInteger threadCount = new AtomicInteger();

	/**
	 * Constructor setting the names of the queue/topic via the
	 * {@link DefaultNamingStrategy} and automatically starting a thread.
	 * 
	 * @param clazz
	 *            The class of the actor.
	 */
	protected AbstractActor(final Class<?> clazz) {
		this(new DefaultNamingStrategy(), clazz, true);
	}

	/**
	 * Constructor setting the names of the queue/topic via the
	 * {@link DefaultNamingStrategy}.
	 * 
	 * @param clazz
	 *            The class of the actor.
	 * @param startThread
	 *            if <b>true</b> start a thread
	 */
	protected AbstractActor(final Class<?> clazz, final boolean startThread) {
		this(new DefaultNamingStrategy(), clazz, startThread);
	}

	/**
	 * Constructor setting the names of the queue/topic via a
	 * {@link QueueNamingStrategy} and automatically starting a thread.
	 * 
	 * @param strategy
	 *            The {@link QueueNamingStrategy} to use with this actor.
	 * @param clazz
	 *            The class of the actor.
	 */
	protected AbstractActor(final QueueNamingStrategy strategy,
			final Class<?> clazz) {
		this(strategy.getInputQueueNameForActor(clazz), strategy
				.getOutputTopicNameForActor(clazz));
	}

	/**
	 * Constructor setting the names of the queue/topic via a
	 * {@link QueueNamingStrategy} and automatically starting a thread.
	 * 
	 * @param strategy
	 *            The {@link QueueNamingStrategy} to use with this actor.
	 * @param clazz
	 *            The class of the actor.
	 * @param startThread
	 *            if <b>true</b> start a thread
	 */
	protected AbstractActor(final QueueNamingStrategy strategy,
			final Class<?> clazz, final boolean startThread) {
		this(strategy.getInputQueueNameForActor(clazz), strategy
				.getOutputTopicNameForActor(clazz), startThread);
	}

	/**
	 * Constructor setting the names of the queue/topic directly from strings
	 * and automatically starting a thread.
	 * 
	 * @param inputQueueName
	 *            The name of this actor's input queue.
	 * @param outputTopicName
	 *            The name of this actor's output topic.
	 */
	protected AbstractActor(final String inputQueueName,
			final String outputTopicName) {
		this(inputQueueName, outputTopicName, true);
	}

	/**
	 * Constructor setting the names of the queue/topic from strings.
	 * 
	 * After the {@link #inputQueue} and the {@link #outputTopic} have been
	 * initialized a {@link Thread} for this actor is started if startThread is
	 * <b>true</b>.
	 * 
	 * @param inputQueueName
	 *            The name of this actor's input queue.
	 * @param outputTopicName
	 *            The name of this actor's output topic.
	 * @param startThread
	 *            if <b>true</b> start a thread
	 */
	protected AbstractActor(final String inputQueueName,
			final String outputTopicName, boolean startThread) {
		this.inputQueueName = inputQueueName;
		this.inputQueue = Hazelcast.getQueue(inputQueueName);
		this.outputTopic = Hazelcast.getTopic(outputTopicName);

		if (startThread) {
			this.start();
		}
	}

	/**
	 * Start the actor in a separate thread.
	 */
	public final void start() {
		Thread t = new Thread(this);
		t.setName("hz-actor [" + Integer.toString(this.threadCount.incrementAndGet()) + "]: "
				+ this.inputQueueName);
		t.start();
	}

	/**
	 * The inner run method.
	 * 
	 * This method simply cycles until {@link #shutdown()} is
	 * called.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		while (!this.shutdownRequested) {
			try {
				InputMessage<X> input = this.inputQueue.poll(100,
						TimeUnit.MILLISECONDS);
				if (input != null) {
					Y output = this.call(input.getMsg());
					this.outputTopic.publish(new OutputMessage<Y>(input
							.getMessageId(), output));
				}
			} catch (InterruptedException e) {
				// ups. We should probably stop now!
				this.shutdownRequested = true;
				e.printStackTrace();
			}
		}
		this.threadCount.decrementAndGet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hence22.hazelcast.actor.api.Stoppable#shutdown()
	 */
	@Override
	public void shutdown() {
		this.shutdownRequested = true;
	}
}
