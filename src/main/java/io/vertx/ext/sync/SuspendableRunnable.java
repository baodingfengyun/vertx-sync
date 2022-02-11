package io.vertx.ext.sync;

import co.paralleluniverse.fibers.SuspendExecution;

/**
 * 可被中止的可执行任务接口
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface SuspendableRunnable {

  /**
   * 执行方法
   *
   * @throws SuspendExecution     可被中止的异常
   * @throws InterruptedException 线程状态中断的异常
   */
  void run() throws SuspendExecution, InterruptedException;

}
