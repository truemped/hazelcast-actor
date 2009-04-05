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

/**
 * Interface for naming strategies of queue names.
 * 
 * @author truemped@googlemail.com
 */
public interface QueueNamingStrategy {

	/**
	 * Return the name for the actor's job queue.
	 * 
	 * @param actor
	 *            The actor for which the name should be created.
	 * @return The namespace of the actor's queue.
	 */
	String getInputQueueNameForActor(Class<?> actor);

	/**
	 * Generate the name for the actor's result topic.
	 * 
	 * @param actor
	 *            The actor for which the name should be created.
	 * @return The namespace of the actor's queue.
	 */
	String getOutputTopicNameForActor(Class<?> actor);

}
