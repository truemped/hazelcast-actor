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

import com.hazelcast.core.IdGenerator;
import com.hazelcast.impl.IdGeneratorImpl;

/**
 * A simple serializable class containing the input message for the
 * {@link Actor}.
 * 
 * @author truemped@googlemail.com
 */
public class InputMessage<X extends Serializable> implements Serializable {

	/**
	 * The generator for uids.
	 */
	private static final IdGenerator UID_GENERATOR = new IdGeneratorImpl("org.hence22.hazelcast-actor.message-uids");
	
	/**
	 * The serial version uid.
	 */
	private static final long serialVersionUID = -8759149759173504594L;

	/**
	 * The message.
	 */
	private X msg;

	/**
	 * This message's ID.
	 */
	private long msgId;

	/**
	 * Constructor setting the value for this input message.
	 * 
	 * At this point the message id is generated. With this id the calls are
	 * identified and the result can be identified by the caller.
	 * 
	 * @param msg
	 */
	public InputMessage(X msg) {
		this.msg = msg;

		this.msgId = UID_GENERATOR.newId();
	}

	/**
	 * @return
	 */
	public X getMsg() {
		return msg;
	}

	/**
	 * Currently the ID of the call is composed of the parameter's hash code
	 * added by the current unix time. The value of the ID is the result's hash
	 * code.
	 * 
	 * @return The id of this message.
	 */
	public long getMessageId() {
		return this.msgId;
	}
}
