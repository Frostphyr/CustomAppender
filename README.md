# CustomAppender

CustomAppender is a Log4j2 appender that allows you to log however you want.

## License

CustomAppender is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

## Dependencies

CustomAppender requires at least Java SE 6 or 7, depending on which version of [Log4j2](https://logging.apache.org/log4j/2.x/) is used. Version 2.4 and greater require 7 and versions less than 2.4 require 6. The minimum supported version of Log4j2 is 2.0.1.

## Download

It can be downloaded with one of the following methods or directly from the website [here](http://frostphyr.com/custom-appender).

### Maven

```xml
<dependency>
  <groupId>com.frostphyr</groupId>
  <artifactId>customappender</artifactId>
  <version>1.1.0</version>
</dependency>
```

### Gradle

```gradle
repositories {
  mavenCentral()
}
```

```gradle
implementation 'com.frostphyr:customappender:1.1.0'
```

## Manual

A manual on how to use Log4j2 can be found [here](https://logging.apache.org/log4j/log4j-2.0/manual/index.html).

CustomAppender works by simply invoking a specified method with the formatted log message to be handled however you want.

| Attribute | Required | Type | Description |
| --- | --- | --- | --- |
| `name` | x | `String` | The name of the appender. |
| `class` | x | `String` | The full name of the class where either the `appendInstance`, if specified, or the `append` is located. |
| `append` | | `String` | The name of the method that will be invoked when a message should be logged. The return type should be `void` and should accept 1 `String` parameter. If an `appendInstance` is not specified, this method should be static. If not specified, this will default to `"append"`. |
| `appendInstance` | | `String` | The name of the static method that will return the instance where `append` will be invoked. The method should return an object of any type and have no parameters. |
| `cacheInstance` | | `boolean` | If `false`, it will reacquire the instance from `appendInstance` every time `append` is invoked. If `appendInstance` is not specified, this has no effect. It defaults to `true`. |
| `ignoreExceptions` | | `boolean` | If `false`, exceptions while appending events will be propagated to the caller. If `true`, exceptions will instead be internally logged and then ignored. It defaults to `true`. |

You can also include [Filters](https://logging.apache.org/log4j/2.0/manual/filters.html) and [Layouts](https://logging.apache.org/log4j/2.x/manual/layouts.html).

## Example

### Without `appendInstance`

This example will simply output the log message to the console.

`log4j2.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" packages="com.frostphyr.custom">
	<Appenders>
		<Custom name="Console" class="com.frostphyr.custom.test.ConsoleAppender">
			<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
		</Custom>
	</Appenders>
	<Loggers>
		<Root level="ALL">
			<AppenderRef ref="Console"/>
		</Root>
	</Loggers>
</Configuration>
```

`ConsoleAppender.java`:

```java
package com.frostphyr.custom.test;

public class ConsoleAppender {
	
	public static void append(String message) {
		System.out.println(message);
	}

}
```

### With `appendInstance`

This example will use Swing and insert the log message into a JTextArea.
`log4j2.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" packages="com.frostphyr.custom">
	<Appenders>
		<Custom name="Swing" class="com.frostphyr.custom.test.SwingAppender" append="appendTextArea" appendInstance="getInstance">
			<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
		</Custom>
	</Appenders>
	<Loggers>
		<Root level="ALL">
			<AppenderRef ref="Swing"/>
		</Root>
	</Loggers>
</Configuration>
```

`SwingAppender.java`:
```java
package com.frostphyr.custom.test;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class SwingAppender {
	
	private static SwingAppender instance = new SwingAppender();
	
	public static SwingAppender getInstance() {
		return instance;
	}
	
	private JTextArea textArea = new JTextArea();
	
	public void appendTextArea(String text) {
		SwingUtilities.invokeLater(() -> {
			Document doc = textArea.getDocument();
			try {
				doc.insertString(doc.getLength(), text, null);
			} catch (BadLocationException e) {
			}
		});
	}

}

```

Alternatively, instead of having a singleton in `SwingAppender`, the instance can be retrieved from another class. For example, in the main class:

```java
package com.frostphyr.custom.test;

public class LoggerTest {
	
	private static SwingAppender swingAppender = new SwingAppender();

	public static void main(String[] args) {
		//Do stuff
	}
	
	public static SwingAppender getSwingAppender() {
		return swingAppender;
	}

}
```

`log4j2.xml` would then look like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" packages="com.frostphyr.custom">
	<Appenders>
		<Custom name="Swing" class="com.frostphyr.custom.test.LoggerTest" append="appendTextArea" appendInstance="getSwingAppender">
			<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
		</Custom>
	</Appenders>
	<Loggers>
		<Root level="ALL">
			<AppenderRef ref="Swing"/>
		</Root>
	</Loggers>
</Configuration>
```