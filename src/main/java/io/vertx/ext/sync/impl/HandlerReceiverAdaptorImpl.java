package io.vertx.ext.sync.impl;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.ReceivePort;
import io.vertx.core.VertxException;
import io.vertx.ext.sync.HandlerReceiverAdaptor;

import java.util.concurrent.TimeUnit;

/**
 * 对 处理器-接收器 封装器接口的实现
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HandlerReceiverAdaptorImpl<T> implements HandlerReceiverAdaptor<T> {

  /**
   * 纤程调度器
   */
  private final FiberScheduler fiberScheduler;
  /**
   * 消息传递管道
   */
  private final Channel<T> channel;

  public HandlerReceiverAdaptorImpl(FiberScheduler fiberScheduler) {
    this.fiberScheduler = fiberScheduler;
    // 创建一个无线容量的管道,单生产者,单消费者
    channel = Channels.newChannel(-1, Channels.OverflowPolicy.DROP, true, true);
  }

  public HandlerReceiverAdaptorImpl(FiberScheduler fiberScheduler, Channel<T> channel) {
    this.fiberScheduler = fiberScheduler;
    this.channel = channel;
  }

  /**
   * 处理消息
   *
   * @param t 消息
   */
  @Override
  @Suspendable
  public void handle(T t) {
    // 创建一个纤程
    new Fiber<Void>(fiberScheduler, () -> {
      try {
        channel.send(t);
      } catch (Exception e) {
        throw new VertxException(e);
      }
    }).start();
  }

  // Access to the underlying Quasar receivePort
  public ReceivePort<T> receivePort() {
    return channel;
  }

  /**
   * 接收消息
   *
   * @return
   */
  @Suspendable
  public T receive() {
    try {
      return channel.receive();
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }

  /**
   * 带超时时间的接收消息
   *
   * @param timeout  the max amount of time in ms to wait for an event to be available 等待事件超时的毫秒数
   * @return
   */
  @Suspendable
  public T receive(long timeout) {
    try {
      return channel.receive(timeout, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      throw new VertxException(e);
    }
  }
}
