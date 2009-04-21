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
import java.util.concurrent.ThreadPoolExecutor;

import org.hence22.hazelcast.actor.api.Actor;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;

import com.hazelcast.core.ITopic;

/**
 * An abstract class for actors living in a {@link ThreadPoolExecutor}.
 * 
 * The actor's input is a field that will be set when a new call receives.
 * 
 * @author truemped@googlemail.com
 */
public abstract class AbstractActorWorker<X extends Serializable, Y extends Serializable>
		implements Actor<X, Y>, Runnable {

	/**
	 * The input message for this actor instance.
	 */
	private final InputMessage<X> inputMsg;

	/**
	 * The Hazelcast topic to send the result to.
	 */
	private final ITopic<OutputMessage<Y>> topic;

	/**
	 * Ctor setting the input message and the output topic.
	 * 
	 * @param inputMsg The input message.
	 * @param topic The output topic.
	 */
	protected AbstractActorWorker(final InputMessage<X> inputMsg,
			final ITopic<OutputMessage<Y>> topic) {
		this.inputMsg = inputMsg;
		this.topic = topic;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public void run() {
		this.topic.publish(new OutputMessage<Y>(this.inputMsg.getMessageId(),
				this.call(this.inputMsg.getMsg())));
	}

}
