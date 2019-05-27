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
 * An {@code AppendInvoker} that reacquires the instance to get and invoke the 
 * append method on each time a message is to be logged.
 * 
 * @author Jon Mannerberg
 * @since 1.0
 */
public class ReacquireAppendInvoker implements AppendInvoker {

	private Method instanceMethod;
	private String append;
	
	/**
	 * Constructs a new {@code ReacquireAppendInvoker}.
	 * 
	 * @param instanceMethod The {@code Method} that is called to acquire the 
	 * instance to get and invoke the append method on every time a message 
	 * needs to be logged. This method should be static, return an object of 
	 * any type, and have no parameters.
	 * @param append The name of the method declared in 
	 * {@code instanceMethod}'s returned object that will be invoked when a 
	 * message needs to be logged. The method should be non-static and have 
	 * only one {@code String} parameter.
	 * @throws IllegalArgumentException If {@code instanceMethod} or 
	 * {@code append} are {@code null}.
	 */
	public ReacquireAppendInvoker(Method instanceMethod, String append) {
		if (instanceMethod == null) {
			throw new IllegalArgumentException("instanceMethod cannot be null");
		} else if (append == null) {
			throw new IllegalArgumentException("append cannot be null");
		}
		this.instanceMethod = instanceMethod;
		this.append = append;
	}

	@Override
	public void append(String text) throws ReflectiveOperationException {
		Object instance = instanceMethod.invoke(null);
		instance.getClass().getDeclaredMethod(append, String.class).invoke(instance, text);
	}

}
