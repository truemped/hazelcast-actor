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

import org.hence22.hazelcast.actor.api.Actor;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.api.QueueNamingStrategy;

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
 *           Now you can start using the actor via the {@link ActorProxy}.
 * 
 * @author truemped@googlemail.com
 */
public abstract class AbstractActor<X extends Serializable, Y extends Serializable>
		implements Actor<X, Y> {

	/**
	 * This actor's input queue.
	 */
	private BlockingQueue<InputMessage<X>> inputQueue;

	/**
	 * This actor's output queue.
	 */
	private ITopic<OutputMessage<Y>> outputTopic;

	/**
	 * The implementation has to set this field so that the {@link #strategy} is
	 * correct.
	 */
	protected Class<?> myClass;

	/**
	 * If true we should shutdown this actor.
	 */
	private volatile boolean shutdownRequested = false;

	/**
	 * Constructor setting the names of the queue/topic via a
	 * {@link QueueNamingStrategy}.
	 * 
	 * @param strategy
	 *            The {@link QueueNamingStrategy} to use with this actor.
	 * @param clazz
	 *            The class of the actor.
	 */
	protected AbstractActor(QueueNamingStrategy strategy, Class<?> clazz) {
		this(strategy.getInputQueueNameForActor(clazz), strategy
				.getOutputTopicNameForActor(clazz));
	}

	/**
	 * Constructor setting the names of the queue/topic directly from strings.
	 * 
	 * After the {@link #inputQueue} and the {@link #outputTopic} have been
	 * initialized a {@link Thread} for this actor is started.
	 * 
	 * @param inputQueueName
	 *            The name of this actor's input queue.
	 * @param outputTopicName
	 *            The name of this actor's output topic.
	 */
	protected AbstractActor(String inputQueueName, String outputTopicName) {
		this.inputQueue = Hazelcast.getQueue(inputQueueName);
		this.outputTopic = Hazelcast.getTopic(outputTopicName);

		Thread t = new Thread(this);
		t.setName("hz-actor: " + inputQueueName);
		t.start();
	}

	/**
	 * The inner run method.
	 * 
	 * This method simply cycles until {@link #shutdownRequested} is
	 * <b>true</b>.
	 */
	public final void run() {
		while (!this.shutdownRequested) {
			try {
				InputMessage<X> input = this.inputQueue.poll(100,
						TimeUnit.MILLISECONDS);
				if (input != null) {
					Y output = this.call(input.getMsg());
					OutputMessage<Y> result = new OutputMessage<Y>(input
							.getMessageId(), output);
					this.outputTopic.publish(result);
				}
			} catch (InterruptedException e) {
				// ups. We should probably stop now!
				this.shutdownRequested = true;
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hence22.hazelcast.actor.api.Actor#shutdown()
	 */
	@Override
	public void shutdown() {
		this.shutdownRequested = true;
	}
}
