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
import java.util.concurrent.ExecutionException;

import org.hence22.hazelcast.actor.api.Director;
import org.hence22.hazelcast.actor.api.InputMessage;
import org.hence22.hazelcast.actor.api.OutputMessage;
import org.hence22.hazelcast.actor.impl.AbstractActorWorker;
import org.hence22.hazelcast.actor.impl.DirectorImpl;

import com.hazelcast.core.ITopic;

/**
 * @author truemped@googlemail.com
 * 
 */
public class FibonacciAdvancedActor extends
		AbstractActorWorker<FibonacciAdvancedActorCallParams, BigInteger> {

	protected FibonacciAdvancedActor(
			InputMessage<FibonacciAdvancedActorCallParams> inputMsg,
			ITopic<OutputMessage<BigInteger>> topic) {
		super(inputMsg, topic);
	}

	private final static Director<FibonacciAdvancedActorCallParams, BigInteger> FIBONACCI_ACTOR = new DirectorImpl<FibonacciAdvancedActorCallParams, BigInteger>(
			FibonacciAdvancedActor.class);

	@Override
	public BigInteger call(FibonacciAdvancedActorCallParams input) {
		if (input.getIter().equals(BigInteger.ZERO)) {
			return input.getResult();
		}

		try {
			return FIBONACCI_ACTOR.call(
					new FibonacciAdvancedActorCallParams(input.getIter()
							.subtract(BigInteger.ONE), input.getNext(), input
							.getResult().add(input.getNext()))).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return BigInteger.valueOf(-1L);
	}

}
