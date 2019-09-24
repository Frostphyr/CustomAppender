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

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * An {@code Appender} that invokes a method and passes a {@code String} 
 * parameter containing the log message. The method that is invoked is 
 * specified by a few attributes. The following table contains the possible 
 * attributes.
 * 
 * <table>
 *   <caption>Attributes</caption>
 *   <tr>
 *     <th>Attribute</th>
 *     <th>Required</th>
 *     <th>Type</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>name</td>
 *     <td>x</td>
 *     <td>String</td>
 *     <td>The name of the appender.</td>
 *   </tr>
 *   <tr>
 *     <td>class</td>
 *     <td>x</td>
 *     <td>String</td>
 *     <td>The full name of the class where either the {@code appendInstance}, 
 *     if specified, or the {@code append} is located.</td>
 *   </tr>
 *   <tr>
 *     <td>append</td>
 *     <td></td>
 *     <td>String</td>
 *     <td>The name of the method that will be invoked when a message should 
 *     be logged. The return type should be {@code void} and should accept 1 
 *     {@code String} parameter. If an {@code appendInstance} is not 
 *     specified, this method should be static. If not specified, this will 
 *     default to {@code "append"}.</td>
 *   </tr>
 *   <tr>
 *     <td>appendInstance</td>
 *     <td></td>
 *     <td>String</td>
 *     <td>The name of the static method that will return the instance where 
 *     {@code append} will be invoked. The method should return an object of 
 *     any type and have no parameters.</td>
 *   </tr>
 *   <tr>
 *     <td>cacheInstance</td>
 *     <td></td>
 *     <td>boolean</td>
 *     <td>If {@code false}, it will reacquire the instance from 
 *     {@code appendInstance} every time {@code append} is invoked. If 
 *     {@code appendInstance} is not specified, this has no effect. It 
 *     defaults to {@code true}.</td>
 *   </tr>
 *   <tr>
 *     <td>ignoreExceptions</td>
 *     <td></td>
 *     <td>boolean</td>
 *     <td>If {@code false}, exceptions while appending events will be 
 *     propagated to the caller. If {@code true}, exceptions will instead be 
 *     internally logged and then ignored. It defaults to {@code true}.</td>
 *   </tr>
 * </table>
 * 
 * It also supports Log4j {@link Filter}s and {@link Layout}s.
 * 
 * @author Jon Mannerberg
 * @since 1.0
 */
@Plugin(name = "Custom", category = "Core", elementType = "appender", printObject = true)
public class CustomAppender extends AbstractAppender {
	
	private AppendInvoker invoker;
	
	private CustomAppender(String name, boolean ignoreExceptions, Filter filter, Layout<? extends Serializable> layout, AppendInvoker invoker) {
		super(name, filter, layout, ignoreExceptions);
		
		this.invoker = invoker;
	}
	
	/**
	 * A {@link PluginFactory} that creates and returns a 
	 * {@code CustomAppender}.
	 * 
	 * @param name The name of the {@code Appender}
	 * @param className The full name of the class where either the 
	 * {@code appendInstance}, if specified, or the {@code append} is located.
	 * @param append The name of the method that will be invoked when a 
	 * message should be logged. The return type should be {@code void} and 
	 * should accept 1 {@code String} parameter. If an {@code appendInstance} 
	 * is not specified, this method should be static. If not specified, this 
	 * will default to {@code "append"}.
	 * @param appendInstance The name of the static method that will return 
	 * the instance where {@code append} will be invoked. The method should 
	 * return an object of any type and have no parameters.
	 * @param cacheInstance If {@code false}, it will reacquire the instance 
	 * from {@code appendInstance} every time {@code append} is invoked. If 
	 * {@code appendInstance} is not specified, this has no effect. It 
	 * defaults to {@code true}.
	 * @param ignoreExceptions If {@code false}, exceptions while appending 
	 * events will be propagated to the caller. If {@code true}, exceptions 
	 * will instead be internally logged and then ignored. It defaults to 
	 * {@code true}.
	 * @param filter The {@code Filter} that determines which messages to log. 
	 * If {@code null}, all messages will be logged.
	 * @param layout The {@code Layout} that formats the log messages. If 
	 * {@code null}, {@link PatternLayout#createDefaultLayout()} will be used.
	 * @return The {@code CustomAppender} for the specified parameters, or 
	 * {@code null} if {@code name} or {@code className} is {@code null} or if 
	 * a {@code ReflectiveOperationException} was thrown acquiring the 
	 * {@code append Method}.
	 */
	@PluginFactory
	public static CustomAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginAttribute("class") String className,
			@PluginAttribute(value = "append", defaultString = "append") String append,
			@PluginAttribute("appendInstance") String appendInstance,
			@PluginAttribute(value = "cacheInstance", defaultBoolean = true) boolean cacheInstance,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions,
			@PluginElement("Filter") Filter filter,
			@PluginElement("Layout") Layout<? extends Serializable> layout) {
		if (name == null) {
			LOGGER.error("CustomAppender must specify a name");
			return null;
		} else if (className == null) {
			LOGGER.error("CustomAppender must specify a class");
			return null;
		} else if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		
		try {
			Class<?> clazz = Class.forName(className);
			AppendInvoker invoker = null;
			if (appendInstance != null) {
				if (cacheInstance) {
					Object instance = clazz.getDeclaredMethod(appendInstance).invoke(null);
					if (instance == null) {
						LOGGER.error("appendInstance cannot return null");
						return null;
					}
					
					invoker = new CachedAppendInvoker(instance, instance.getClass().getDeclaredMethod(append, String.class));
				} else {
					invoker = new ReacquireAppendInvoker(clazz.getDeclaredMethod(appendInstance), append);
				}
			} else {
				invoker = new CachedAppendInvoker(null, clazz.getDeclaredMethod(append, String.class));
			}
			return new CustomAppender(name, ignoreExceptions, filter, layout, invoker);
		} catch (Exception e) {
			LOGGER.error("Error creating CustomAppender", e);
			return null;
		}
	}

	@Override
	public void append(LogEvent event) {
		try {
			invoker.append(new String(getLayout().toByteArray(event)));
		} catch (Exception e) {
			LOGGER.error("Error invoking append method", e);
			throw new AppenderLoggingException(e);
		}
	}

}
