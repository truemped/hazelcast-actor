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
 * @author truemped@googlemail.com
 * 
 */
public class OutputMessage<Y extends Serializable> implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5458787418053229255L;

	/**
	 * The id of this message.
	 */
	private int msgId;

	/**
	 * The message itself.
	 */
	private Y msg;

	/**
	 * @param msgId
	 * @param output
	 */
	public OutputMessage(int msgId, Y output) {
		this.msgId = msgId;
		this.msg = output;
	}
	
	/**
	 * @return The message id.
	 */
	public int getMessageId() {
		return msgId;
	}
	
	/**
	 * @return The message.
	 */
	public Y getMessage() {
		return this.msg;
	}
}
