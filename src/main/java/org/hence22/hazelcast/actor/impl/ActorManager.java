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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hence22.hazelcast.actor.api.AbstractActorWorker;
import org.hence22.hazelcast.actor.api.ActorWorkerFactory;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.api.QueueNamingStrategy;
import org.hence22.hazelcast.actor.api.Stoppable;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ITopic;

/**
 * @author truemped@googlemail.com
 */
public class ActorManager<X extends Serializable, Y extends Serializable>
		implements Runnable, Stoppable {

	/**
	 * The executor service for this underlying actor.
	 */
	private ThreadPoolExecutor actorExecutor;

	/**
	 * The name of this actor's input queue.
	 */
	private String inputQueueName;

	/**
	 * The input queue of this actor.
	 */
	private BlockingQueue<InputMessage<X>> inputQueue;

	/**
	 * The output topic of this actor.
	 */
	private ITopic<OutputMessage<Y>> outputTopic;

	/**
	 * The actor class.
	 */
	private ActorWorkerFactory<X, Y> actorFactory;

	/**
	 * Indicate a shutdown request.
	 */
	private volatile boolean shutdownRequested = false;

	/**
	 * A blocking queue of outgoing messages.
	 */
	private BlockingQueue<MessageToBePublished> newOutputMessages = new LinkedBlockingQueue<MessageToBePublished>();

	/**
	 * Constructor setting the Hazelcast names and creating a
	 * {@link ThreadPoolExecutor} with maxNumberOfActorThreads.
	 * 
	 * @param strategy
	 * @param clazz
	 * @param maxNumberOfActorThreads
	 */
	protected ActorManager(final QueueNamingStrategy strategy,
			final ActorWorkerFactory<X, Y> factory,
			final int maxNumberOfActorThreads) {
		this(strategy.getInputQueueNameForActor(factory.getClazz()), strategy
				.getOutputTopicNameForActor(factory.getClazz()), factory,
				maxNumberOfActorThreads);
	}

	/**
	 * Constructor setting the Hazelcast names and creating a
	 * {@link ThreadPoolExecutor} where the maximum capacity of worker threads
	 * equals the number of processors.
	 * 
	 * @param strategy
	 * @param clazz
	 */
	protected ActorManager(final QueueNamingStrategy strategy,
			final ActorWorkerFactory<X, Y> factory) {
		this(strategy.getInputQueueNameForActor(factory.getClazz()), strategy
				.getOutputTopicNameForActor(factory.getClazz()), factory,
				Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Constructor setting the Hazelcast names and creating a
	 * {@link ThreadPoolExecutor} with maxNumberOfActorThreads.
	 * 
	 * @param inputQueueName
	 * @param outputTopicName
	 * @param clazz
	 * @param maxNumberOfActorThreads
	 */
	protected ActorManager(String inputQueueName, String outputTopicName,
			final ActorWorkerFactory<X, Y> factory, int maxNumberOfActorThreads) {
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
		me.setName("hz-actor: " + this.getClass().getName());

		PublishingScheduler scheduler = new PublishingScheduler(
				this.newOutputMessages, this.outputTopic);
		new Thread(scheduler).start();

		while (!this.shutdownRequested) {
			// check if the threadpool can handle another thread
			if (this.actorExecutor.getActiveCount() < this.actorExecutor
					.getMaximumPoolSize()) {
				try {
					InputMessage<X> input = this.inputQueue.poll(100L,
							TimeUnit.MILLISECONDS);
					if (input != null) {
						AbstractActorWorker<X, Y> worker = this.actorFactory.newInstance();
						worker.setInput(input.getMsg());
						// add the worker to the pool and to the
						// newOutputMessages
						this.newOutputMessages.put(new MessageToBePublished(
								input.getMessageId(), this.actorExecutor
										.submit(worker)));
					}
				} catch (InterruptedException e) {
					// ups. we should probably stop here.
					e.printStackTrace();
					this.shutdownRequested = true;
				}
			}
		}

		scheduler.shutdown();

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

	/**
	 * A simple container for messages that have to be delivered as soon as a
	 * worker thread finished.
	 * 
	 * @author truemped@googlemail.com
	 */
	private class MessageToBePublished {

		/**
		 * The msg id.
		 */
		private int msgId;

		/**
		 * The {@link Future} representing a call.
		 */
		private Future<Y> val;

		/**
		 * Constructor setting the fields.
		 * 
		 * @param msgId
		 *            The message id.
		 * @param val
		 *            The {@link Future} representing a call.
		 */
		public MessageToBePublished(int msgId, Future<Y> val) {
			this.msgId = msgId;
			this.val = val;
		}

		/**
		 * @return The message without the value.
		 */
		public int getMsgId() {
			return this.msgId;
		}

		/**
		 * @return The future representing this call.
		 */
		public Future<Y> getFuture() {
			return this.val;
		}
	}

	/**
	 * The thread sending the messages back to the caller.
	 * 
	 * This scheduler implicity assumes linear time in all workers, i.e. the
	 * messages are published in the same order as they have been received.
	 * 
	 * TODO remove linear time constraint
	 * 
	 * @author truemped@googlemail.com
	 */
	private class PublishingScheduler implements Runnable, Stoppable {

		/**
		 * The queue for new futures.
		 */
		private BlockingQueue<MessageToBePublished> queue;

		/**
		 * The topic to publish the messages.
		 */
		private ITopic<OutputMessage<Y>> topic;

		/**
		 * Indicate a shutdown request.
		 */
		private volatile boolean shutdownRequested = false;

		/**
		 * @param queue
		 * @param topic
		 */
		public PublishingScheduler(BlockingQueue<MessageToBePublished> queue,
				ITopic<OutputMessage<Y>> topic) {
			this.queue = queue;
			this.topic = topic;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (!this.shutdownRequested) {
				try {
					MessageToBePublished m = this.queue.poll(100L,
							TimeUnit.MILLISECONDS);
					if (m != null) {
						OutputMessage<Y> outMsg = new OutputMessage<Y>(m
								.getMsgId(), m.getFuture().get());
						this.topic.publish(outMsg);
					}
				} catch (InterruptedException e) {
					// ups. we should probably stop here.
					e.printStackTrace();
					this.shutdownRequested = true;
				} catch (ExecutionException e) {
					// ups. we should probably stop here.
					e.printStackTrace();
					this.shutdownRequested = true;
				}
			}
			ActorManager.this.shutdown();
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
}
