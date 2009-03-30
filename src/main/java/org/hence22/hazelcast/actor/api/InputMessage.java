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
 * A simple serializable class containing the input message for the {@link Actor}.
 * 
 * @author truemped@googlemail.com
 */
public class InputMessage<X extends Serializable> implements Serializable {

	/**
	 * The serial version uid.
	 */
	private static final long serialVersionUID = -8759149759173504594L;

	/**
	 * The message.
	 */
	private X msg;

	/**
	 * @param msg
	 */
	public InputMessage(X msg) {
		this.msg = msg;
	}

	/**
	 * @return
	 */
	public X getMsg() {
		return msg;
	}

	/**
	 * @return The id of this message.
	 */
	public int getMessageId() {
		return this.msg.hashCode();
	}
}
