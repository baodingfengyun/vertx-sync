package io.vertx.ext.sync;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * A `Verticle` which runs its `start` and `stop` methods using fibers.
 *
 * You should subclass this class instead of `AbstractVerticle` to create any verticles that use vertx-sync.
 *
 * 一个`Verticle`通过纤程来启动和停止操作. 你应该继承此类来实现vertx 同步代码风格.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public abstract class SyncVerticle extends AbstractVerticle {

  /**
   * 纤程调度器
   */
  protected FiberScheduler instanceScheduler;

  /**
   * 采用纤程方式实现 Verticle 启动
   *
   * @param startFuture 异步操作的结果(用来观察是否完成)
   * @throws Exception
   */
  @Override
  public void start(Promise<Void> startFuture) throws Exception {
    // 从上下文获取纤程调度器
    instanceScheduler = Sync.getContextScheduler();
    // 启动一个新的纤程(由此调度器)来执行 Verticle 的启动代码
    new Fiber<Void>(instanceScheduler, () -> {
      try {
        // 调用此 Verticle 的启动方法(封装在纤程里面)
        SyncVerticle.this.start();
        // 异步任务正常结束
        startFuture.complete();
      } catch (Throwable t) {
        // 异步任务异常结束
        startFuture.fail(t);
      }
    }).start();
  }

  /**
   * 采用纤程方式实现 Verticle 结束
   *
   * @param stopFuture
   * @throws Exception
   */
  @Override
  public void stop(Promise<Void> stopFuture) throws Exception {
    new Fiber<Void>(instanceScheduler, () -> {
      try {
        SyncVerticle.this.stop();
        stopFuture.complete();
      } catch (Throwable t) {
        stopFuture.fail(t);
      } finally {
        Sync.removeContextScheduler();
      }
    }).start();
  }

  /**
   * Override this method in your verticle
   * 在子类中重写,将 start 设置为可中止的方法
   */
  @Override
  @Suspendable
  public void start() throws Exception {
  }

  /**
   * Optionally override this method in your verticle if you have cleanup to do
   * 在子类中重写, 将 stop 设置为可中止的方法
   */
  @Override
  @Suspendable
  public void stop() throws Exception {
  }

}
