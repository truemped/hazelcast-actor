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
package org.hence22.hazelcast.actor.impl;

import org.hence22.hazelcast.actor.api.QueueNamingStrategy;

/**
 * Very simple naming strategy that simply concatenates a namespaceprefix with
 * the classname.
 * 
 * @author truemped@googlemail.com
 */
public class DefaultNamingStrategy implements QueueNamingStrategy {

	/**
	 * Namespace prefix for this naming strategy.
	 */
	private final String namespacePrefix;

	/**
	 * Appendix for the result queue;
	 */
	private final String resultQueueAppendix;

	/**
	 * Constructor setting the name space Prefix to the empty string.
	 */
	public DefaultNamingStrategy() {
		this("", ".results");
	}

	/**
	 * @param namespacePrefix
	 */
	public DefaultNamingStrategy(final String namespacePrefix) {
		this(namespacePrefix, ".results");
	}

	/**
	 * @param namespacePrefix
	 * @param resultQueueAppendix
	 */
	public DefaultNamingStrategy(final String namespacePrefix,
			final String resultQueueAppendix) {
		this.namespacePrefix = namespacePrefix;
		this.resultQueueAppendix = resultQueueAppendix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hence22.hazelcast.actor.api.QueueNamingStrategy#getJobQueueNameForActor
	 * (java.lang.Class)
	 */
	@Override
	public String getInputQueueNameForActor(final Class<?> actor) {
		return this.namespacePrefix + actor.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hence22.hazelcast.actor.api.QueueNamingStrategy#
	 * getResultQueueNameForActor(java.lang.Class)
	 */
	@Override
	public String getOutputTopicNameForActor(final Class<?> actor) {
		return this.namespacePrefix + actor.getName()
				+ this.resultQueueAppendix;
	}

}