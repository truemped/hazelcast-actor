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
package org.hence22.hazelcast.actor.api;

import java.io.Serializable;

import org.hence22.hazelcast.actor.impl.AbstractActorWorker;

import com.hazelcast.core.ITopic;

/**
 * A factory for {@link AbstractActorWorker}s.
 * 
 * @author truemped@googlemail.com
 */
public interface ActorWorkerFactory<X extends Serializable, Y extends Serializable> {

	/**
	 * Get the actor's class.
	 * 
	 * Used for the naming of Hazelcast names.
	 * 
	 * @return The actor worker's class.
	 */
	Class<? extends AbstractActorWorker<X, Y>> getClazz();

	/**
	 * The factory method for a new {@link Actor}.
	 * 
	 * @param input The input message for the new actor.
	 * @param topic The Hazelcast output topic for publishing the results.
	 * @return A new instance of the actor worker.
	 */
	AbstractActorWorker<X, Y> newInstance(InputMessage<X> input, ITopic<OutputMessage<Y>> topic);
	
}
