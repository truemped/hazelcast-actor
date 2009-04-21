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
package org.hence22.hazelcast.actor.it.fibonacci;

import java.math.BigInteger;

import org.hence22.hazelcast.actor.api.ActorWorkerFactory;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.impl.AbstractActorWorker;

import com.hazelcast.core.ITopic;

/**
 * @author truemped@googlemail.com
 *
 */
public class FibonacciSimpleActorFactory implements ActorWorkerFactory<BigInteger, BigInteger> {

	@Override
	public Class<? extends AbstractActorWorker<BigInteger, BigInteger>> getClazz() {
		return FibonacciSimpleActor.class;
	}

	@Override
	public AbstractActorWorker<BigInteger, BigInteger> newInstance(
			final InputMessage<BigInteger> input,
			final ITopic<OutputMessage<BigInteger>> topic) {
		return new FibonacciSimpleActor(input, topic);
	}

}
