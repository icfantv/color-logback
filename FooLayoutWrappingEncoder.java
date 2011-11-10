package com.foo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import org.apache.commons.lang3.CharUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FooLayoutWrappingEncoder extends LayoutWrappingEncoder<ILoggingEvent> {

  public static final long PRINT_TIME_INTERVAL = 1000L;

  // Attribute codes:
  // 00=none 01=bold 04=underscore 05=blink 07=reverse 08=concealed
  //
  // Text color codes:
  // 30=black 31=red 32=green 33=yellow 34=blue 35=magenta 36=cyan 37=white

  public static final String COLOR_YELLOW   = "\033[1;33m";
  public static final String COLOR_NEUTRAL  = "\033[0m";
  public static final String COLOR_ALL      = "\033[0m";
  public static final String COLOR_ERROR    = "\033[01;31m";
  public static final String COLOR_WARN     = "\033[01;35m";
  public static final String COLOR_INFO     = "\033[01;34m";
  public static final String COLOR_DEBUG    = "\033[01;36m";
  public static final String COLOR_TRACE    = "\033[0;37m";
  public static final String COLOR_DEFAULT  = "\033[0m";

  public FooLayoutWrappingEncoder() {
    super();
    this.setCharset(Charset.forName("UTF-8"));
    this.clientImpl = new ClientImpl();
  }

  @Override
  public void doEncode(ILoggingEvent event) throws IOException {

    try {
      this.maybeLogTime(this.clientImpl);
    }
    catch (IOException ex) {
      // throw new RuntimeException(ex);
    }

    byte[] bytes = null;
    String message = this.format(event);
    try {
      bytes = message.toString().getBytes(this.getCharset().name());
    }
    catch (UnsupportedEncodingException ex) {
      throw new IOException(ex);
    }

    this.outputStream.write(bytes);
    this.outputStream.flush();
  }

  private static String getTime() {
    return FORMAT.format(Calendar.getInstance().getTime());
  }

  private static String format(ILoggingEvent event) {

    StringBuilder sb = new StringBuilder(128);
    sb.append(CharUtils.LF);

    Level level = event.getLevel();
    switch (level.toInt()) {
      case Level.ALL_INT:
        sb.append(COLOR_ALL);
        break;
      case Level.ERROR_INT:
        sb.append(COLOR_ERROR);
        break;
      case Level.WARN_INT:
        sb.append(COLOR_WARN);
        break;
      case Level.INFO_INT:
        sb.append(COLOR_INFO);
        break;
      case Level.DEBUG_INT:
        sb.append(COLOR_DEBUG);
        break;
      case Level.TRACE_INT:
        sb.append(COLOR_TRACE);
        break;
    }

    sb.append("[");
    sb.append(level.toString());
    sb.append("] - ");
    sb.append(event.getFormattedMessage());
    sb.append(COLOR_DEFAULT);

    return sb.toString();
  }

  private static final DateFormat FORMAT =
    new SimpleDateFormat("yyyy.MM.dd, hh:mm:ss a");

  private static long lastLogTime = 0L;

  private void maybeLogTime(ClientImpl client) throws IOException {

    long now = System.currentTimeMillis();
    if ((now - client.getLastLogTime()) >= PRINT_TIME_INTERVAL) {
      logTime();
      client.setLastLogTime(now);
    }
  }

  private void logTime() throws IOException {

    StringBuilder sb = new StringBuilder();
    sb.append(COLOR_YELLOW);
    sb.append(getTime()).append(":");
    sb.append(COLOR_NEUTRAL);

    byte[] bytes = null;
    try {
      bytes = sb.toString().getBytes(this.getCharset().name());
    }
    catch (UnsupportedEncodingException ex) {
      throw new IOException(ex);
    }

    this.outputStream.write(bytes);
    this.outputStream.flush();
  }

  private ClientImpl clientImpl;

  public interface Client {
    public long getLastLogTime();
    public void setLastLogTime(long time);
  }

  private static class ClientImpl implements Client {

    public long getLastLogTime() {
      return lastLogTime;
    }

    public void setLastLogTime(long time) {
      lastLogTime = time;
    }
  }
}
