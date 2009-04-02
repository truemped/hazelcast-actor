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
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * An abstract class for actors living in a {@link ThreadPoolExecutor}.
 * 
 * The actor's input is a field that will be set when a new call receives.
 * 
 * @author truemped@googlemail.com
 */
public abstract class AbstractActorWorker<X extends Serializable, Y extends Serializable>
		implements Actor<X, Y>, Callable<Y> {

	/**
	 * The input of this actor's run.
	 */
	private X input;

	/**
	 * @param input
	 */
	public void setInput(X input) {
		this.input = input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Y call() throws Exception {
		return this.call(this.input);
	}

}
