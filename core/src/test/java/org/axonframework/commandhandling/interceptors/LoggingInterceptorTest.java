/*
 * Copyright (c) 2010. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.commandhandling.interceptors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.axonframework.commandhandling.CommandContext;
import org.axonframework.commandhandling.CommandHandler;
import org.junit.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

/**
 * @author Allard Buijze
 */
@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
public class LoggingInterceptorTest {

    private LoggingInterceptor testSubject;
    private org.apache.log4j.Logger mockLogger;

    @Before
    public void setUp() throws Exception {
        testSubject = new LoggingInterceptor();
        Log4jLoggerAdapter logger = (Log4jLoggerAdapter) LoggerFactory.getLogger(LoggingInterceptor.class);
        Field loggerField = logger.getClass().getDeclaredField("logger");
        ReflectionUtils.makeAccessible(loggerField);
        mockLogger = mock(Logger.class);
        loggerField.set(logger, mockLogger);
    }

    @Test
    public void testIncomingLogging() {
        CommandContext mockCommandContext = mock(CommandContext.class);
        when(mockCommandContext.getCommand()).thenReturn(new StubCommand());
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        testSubject.beforeCommandHandling(mockCommandContext, mock(CommandHandler.class));

        verify(mockLogger, atLeast(1)).isInfoEnabled();
        verify(mockLogger).log(any(String.class), any(Priority.class), contains("[StubCommand]"), any(Throwable.class));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testSuccessfulExecution_NullReturnValue() {
        CommandContext mockCommandContext = mock(CommandContext.class);
        when(mockCommandContext.getCommand()).thenReturn(new StubCommand());
        when(mockCommandContext.getResult()).thenReturn(null);
        when(mockCommandContext.isSuccessful()).thenReturn(Boolean.TRUE);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        testSubject.afterCommandHandling(mockCommandContext, mock(CommandHandler.class));

        verify(mockLogger, atLeast(1)).isInfoEnabled();
        verify(mockLogger).log(any(String.class), any(Priority.class), and(contains("[StubCommand]"),
                                                                           contains("[null]")), any(Throwable.class));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testSuccessfulExecution_VoidReturnValue() {
        CommandContext mockCommandContext = mock(CommandContext.class);
        when(mockCommandContext.getCommand()).thenReturn(new StubCommand());
        when(mockCommandContext.getResult()).thenReturn(Void.TYPE);
        when(mockCommandContext.isSuccessful()).thenReturn(Boolean.TRUE);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        testSubject.afterCommandHandling(mockCommandContext, mock(CommandHandler.class));

        verify(mockLogger, atLeast(1)).isInfoEnabled();
        verify(mockLogger).log(any(String.class), any(Priority.class), and(contains("[StubCommand]"),
                                                                           contains("[void]")), any(Throwable.class));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testSuccessfulExecution_CustomReturnValue() {
        CommandContext mockCommandContext = mock(CommandContext.class);
        when(mockCommandContext.getCommand()).thenReturn(new StubCommand());
        when(mockCommandContext.getResult()).thenReturn(new StubResponse());
        when(mockCommandContext.isSuccessful()).thenReturn(Boolean.TRUE);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        testSubject.afterCommandHandling(mockCommandContext, mock(CommandHandler.class));

        verify(mockLogger, atLeast(1)).isInfoEnabled();
        verify(mockLogger).log(any(String.class), eq(Level.INFO), and(contains("[StubCommand]"),
                                                                      contains("[StubResponse]")),
                               any(Throwable.class));
        verifyNoMoreInteractions(mockLogger);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Test
    public void testFailedExecution() {
        CommandContext mockCommandContext = mock(CommandContext.class);
        when(mockCommandContext.getCommand()).thenReturn(new StubCommand());
        when(mockCommandContext.isSuccessful()).thenReturn(Boolean.FALSE);
        RuntimeException exception = new RuntimeException("Mock");
        when(mockCommandContext.getException()).thenReturn(exception);
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        testSubject.afterCommandHandling(mockCommandContext, mock(CommandHandler.class));

        verify(mockLogger).log(any(String.class), eq(Level.WARN), and(contains("[StubCommand]"),
                                                                      contains("failed")), eq(exception));
        verifyNoMoreInteractions(mockLogger);
    }

    @Test
    public void testConstructorWithCustomLogger() throws Exception {
        testSubject = new LoggingInterceptor("my.custom.logger");
        Field field = testSubject.getClass().getDeclaredField("logger");
        field.setAccessible(true);
        Log4jLoggerAdapter logger = (Log4jLoggerAdapter) field.get(testSubject);
        assertEquals("my.custom.logger", logger.getName());
    }

    private static class StubCommand {

    }

    private static class StubResponse {

    }
}