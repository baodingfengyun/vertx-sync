package io.vertx.ext.sync;

import io.vertx.core.Handler;

/**
 *
 * Represents an object that is both a handler of a particular event and also a receiver of that event.
 * <p>
 * In other words it converts an asynchronous stream of events into a synchronous receiver of events
 *
 * 一个既是某个事件处理器又是此事件接收器的接口
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface HandlerReceiverAdaptor<T> extends Handler<T>, Receiver<T> {
}
