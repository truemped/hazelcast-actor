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
package org.hence22.hazelcast.actor.it.fibonacci2;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.impl.AbstractActorWorker;
import org.hence22.hazelcast.actor.impl.ActorProxy;
import org.hence22.hazelcast.actor.impl.DefaultNamingStrategy;

import com.hazelcast.core.ITopic;

/**
 * @author truemped@googlemail.com
 * 
 */
public class FibonacciActor2 extends
		AbstractActorWorker<FibonacciCallParams, BigInteger> {

	protected FibonacciActor2(InputMessage<FibonacciCallParams> inputMsg,
			ITopic<OutputMessage<BigInteger>> topic) {
		super(inputMsg, topic);
	}

	private final static ActorProxy<FibonacciCallParams, BigInteger> FIBONACCI_ACTOR = new ActorProxy<FibonacciCallParams, BigInteger>(
			new DefaultNamingStrategy(), FibonacciActor2.class);

	@Override
	public BigInteger call(FibonacciCallParams input) {
		if (input.getIter().equals(BigInteger.ZERO)) {
			return input.getResult();
		}

		try {
			return FIBONACCI_ACTOR.call(
					new FibonacciCallParams(input.getIter().subtract(
							BigInteger.ONE), input.getNext(), input.getResult()
							.add(input.getNext()))).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return BigInteger.valueOf(-1L);
	}

}
