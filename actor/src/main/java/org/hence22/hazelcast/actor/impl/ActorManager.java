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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hence22.hazelcast.actor.api.ActorWorkerFactory;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.api.QueueNamingStrategy;
import org.hence22.hazelcast.actor.api.Stoppable;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;

/**
 * A manager for running actors in a thread pool.
 * 
 * Usage: <code>
 * ActorManager&lt;String, String&gt; myActorManager = new ActorManager&lt;String,
 *     String&gt;(new DefaultNamingStrategy(), new MyActorFactory());
 * 
 * Thread manager = new Thread(echoManager);
 * 
 * manager.start();
 * </code>
 * 
 * @param <X> The type of the actor's parameter message.
 * @param <Y> The type of the actor"s response message.
 *
 * @author truemped@googlemail.com
 */
public class ActorManager<X extends Serializable, Y extends Serializable>
		implements Runnable, Stoppable {

	/**
	 * The executor service for this underlying actor.
	 */
	private final ThreadPoolExecutor actorExecutor;

	/**
	 * The name of this actor's input queue.
	 */
	private final String inputQueueName;

	/**
	 * The input queue of this actor.
	 */
	private final BlockingQueue<InputMessage<X>> inputQueue;

	/**
	 * The output topic of this actor.
	 */
	private final ITopic<OutputMessage<Y>> outputTopic;

	/**
	 * The actor class.
	 */
	private final ActorWorkerFactory<X, Y> actorFactory;

	/**
	 * Indicate a shutdown request.
	 */
	private volatile boolean shutdownRequested = false;

	/**
	 * Constructor setting the Hazelcast names and creating a
	 * {@link ThreadPoolExecutor} with maxNumberOfActorThreads.
	 * 
	 * @param strategy
     * @param factory 
     * @param maxNumberOfActorThreads
	 */
	public ActorManager(final QueueNamingStrategy strategy,
			final ActorWorkerFactory<X, Y> factory,
			final int maxNumberOfActorThreads) {
		this(strategy.getInputQueueNameForActor(factory.getClazz()), strategy
				.getOutputTopicNameForActor(factory.getClazz()), factory,
				maxNumberOfActorThreads);
	}

	/**
	 * Constructor setting the Hazelcast names and creating a caching
	 * {@link ThreadPoolExecutor} where the maximum capacity of worker threads
	 * equals the maximum integer number.
	 * 
	 * This will allow recursive calls to actors since there may be
	 * <b>Integer.MAX_VALUE</b> number of threads.
	 * 
     * @param strategy
     * @param factory
	 */
	public ActorManager(final QueueNamingStrategy strategy,
			final ActorWorkerFactory<X, Y> factory) {
		this(strategy.getInputQueueNameForActor(factory.getClazz()), strategy
				.getOutputTopicNameForActor(factory.getClazz()), factory,
				Integer.MAX_VALUE);
	}

	/**
	 * Constructor setting the Hazelcast names and creating a
	 * {@link ThreadPoolExecutor} with maxNumberOfActorThreads.
	 * 
	 * @param inputQueueName
	 * @param outputTopicName
     * @param factory
     * @param maxNumberOfActorThreads
	 */
	public ActorManager(final String inputQueueName,
			final String outputTopicName,
			final ActorWorkerFactory<X, Y> factory,
			final int maxNumberOfActorThreads) {
		this.inputQueueName = inputQueueName;
		this.inputQueue = Hazelcast.getQueue(this.inputQueueName);
		this.outputTopic = Hazelcast.getTopic(outputTopicName);
		this.actorExecutor = new ThreadPoolExecutor(0, maxNumberOfActorThreads,
				60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		this.actorFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		Thread me = Thread.currentThread();
		me.setName("hz-actor-manager: " + this.getClass().getName());

		while (!this.shutdownRequested) {
			// check if the threadpool can handle another thread
			if (this.actorExecutor.getActiveCount() < this.actorExecutor
					.getMaximumPoolSize()) {
				try {
					InputMessage<X> input = this.inputQueue.poll(10L,
							TimeUnit.MILLISECONDS);
					if (input != null) {
						AbstractActorWorker<X, Y> worker = this.actorFactory
								.newInstance(input, this.outputTopic);
						// add the worker to the pool
						this.actorExecutor.submit(worker);
					}
				} catch (InterruptedException e) {
					// ups. we should probably stop here.
					e.printStackTrace();
					this.shutdownRequested = true;
				}
			}
		}

	}

	@Override
	public void shutdown() {
		this.shutdownRequested = true;
	}

}
