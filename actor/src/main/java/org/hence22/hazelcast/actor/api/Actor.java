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

/**
 * Interface for actors.
 * 
 * @param <X> The type of the actor's parameter message.
 * @param <Y> The type of the actor"s response message.
 *
 * @author truemped@googlemail.com
 */
public interface Actor<X extends Serializable, Y extends Serializable> {

	/**
	 * The <it>business logic</it> associated with the actor.
	 * 
	 * @param input The actor's input.
	 * @return The result.
	 */
	Y call(X input);

}