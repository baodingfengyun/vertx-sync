package io.vertx.ext.sync;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.ReceivePort;

/**
 * Represents a synchronous receiver of events.
 * 表现为一个同步的事件接收器
 * <p>
 * Note that the `receive` methods may block the calling fiber but will not block an underlying kernel thread.
 *
 * 注意: `receive`方法可能阻塞调用方纤程fiber,但是不会阻塞下层的内核线程.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface Receiver<T> {

  /**
   * 返回一个下层的 Quasar 接收端口
   * @return the underlying Quasar receivePort
   */
  ReceivePort<T> receivePort();

  /**
   * Return an event when one is available. This method will block the fiber until one is available.
   * No kernel thread is blocked.
   * 当准备好了就返回一个事件. 这个方法会阻塞纤程,直到事件来到. 没有任何内核线程会被阻塞.
   *
   * 注解 Suspendable 表示某个方法可以被中止.
   *
   * @return  the event 事件
   */
  @Suspendable
  T receive();

  /**
   * Return an event when one is available. This method will block the fiber until one is available, or timeout occurs.
   * No kernel thread is blocked.
   *
   * @param timeout  the max amount of time in ms to wait for an event to be available 等待事件超时的毫秒数
   * @return  the event
   */
  @Suspendable
  T receive(long timeout);
}
