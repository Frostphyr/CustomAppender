/*
 * Copyright 2019 Frostphyr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frostphyr.custom;

import java.lang.reflect.Method;

/**
 * An {@code AppendInvoker} that caches the instance to invoke the append 
 * method on. Alternatively can be used if the append method is {@code static} 
 * and no instance exists.
 * 
 * @author Jon Mannerberg
 * @since 1.0
 */
public class CachedAppendInvoker implements AppendInvoker {
	
	private Object instance;
	private Method method;
	
	/**
	 * Constructs a new {@code CachedAppendInstance}.
	 * 
	 * @param instance The instance to invoke the append method on. If 
	 * {@code null}, the method will instead be invoked statically.
	 * @param method The append method that should only accept one 
	 * {@code String} parameter.
	 * @throws IllegalArgumentException If {@code method} is {@code null}.
	 */
	public CachedAppendInvoker(Object instance, Method method) {
		if (method == null) {
			throw new IllegalArgumentException("method cannot be null");
		}
		this.instance = instance;
		this.method = method;
	}

	@Override
	public void append(String text) throws ReflectiveOperationException {
		method.invoke(instance, text);
	}

}
