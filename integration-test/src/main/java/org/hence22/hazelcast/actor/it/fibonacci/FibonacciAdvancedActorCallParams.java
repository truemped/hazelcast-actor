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

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author truemped@googlemail.com
 * 
 */
public class FibonacciAdvancedActorCallParams implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7694164731575694402L;

	private BigInteger iter;

	private BigInteger result;

	private BigInteger next;

	public FibonacciAdvancedActorCallParams(BigInteger iter, BigInteger result,
			BigInteger next) {
		this.setIter(iter);
		this.setResult(result);
		this.setNext(next);
	}

	public void setIter(BigInteger iter) {
		this.iter = iter;
	}

	public BigInteger getIter() {
		return iter;
	}

	public void setResult(BigInteger result) {
		this.result = result;
	}

	public BigInteger getResult() {
		return result;
	}

	public void setNext(BigInteger next) {
		this.next = next;
	}

	public BigInteger getNext() {
		return next;
	}
	
}
