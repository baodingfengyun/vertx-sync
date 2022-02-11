package io.vertx.ext.sync.impl;

import co.paralleluniverse.fibers.FiberAsync;
import io.vertx.core.Handler;

/**
 * 处理封装器
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class HandlerAdaptor<T> extends FiberAsync<T, Throwable> implements Handler<T>  {

  /**
   * 处理某个类型的结果
   *
   * @param res 结果
   */
  @Override
  public void handle(T res) {
    asyncCompleted(res);
  }
}
