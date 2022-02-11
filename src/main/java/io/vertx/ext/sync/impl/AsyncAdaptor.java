package io.vertx.ext.sync.impl;

import co.paralleluniverse.fibers.FiberAsync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * 异步操作封装器
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class AsyncAdaptor<T> extends FiberAsync<T, Throwable> implements Handler<AsyncResult<T>>  {

  /**
   * 处理异步操作的结果
   *
   * @param res 结果
   */
  @Override
  public void handle(AsyncResult<T> res) {
    if (res.succeeded()) {
      asyncCompleted(res.result());
    } else {
      asyncFailed(res.cause());
    }
  }
}
