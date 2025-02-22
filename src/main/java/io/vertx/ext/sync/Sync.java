package io.vertx.ext.sync;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import io.vertx.core.*;
import io.vertx.ext.sync.impl.AsyncAdaptor;
import io.vertx.ext.sync.impl.HandlerAdaptor;
import io.vertx.ext.sync.impl.HandlerReceiverAdaptorImpl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * This class contains various static methods to allowing events and asynchronous results to be accessed
 * in a synchronous way.
 *
 * 这个类包含了不同的静态方法,允许事件和异步结果以同步方式访问.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Sync {

  /**
   * 纤程调用器上下文
   */
  private static final String FIBER_SCHEDULER_CONTEXT_KEY = "__vertx-sync.fiberScheduler";

  /**
   * Invoke an asynchronous operation and obtain the result synchronous.
   * The fiber will be blocked until the result is available. No kernel thread is blocked.
   *
   * 调用一个异步操作,同步等待结果. 纤程会被阻塞直到得到结果. 没有任何内核线程会被阻塞.
   *
   * @param consumer  this should encapsulate the asynchronous operation. The handler is passed to it.
   *                  消费者消费: 被包装成异步操作.
   * @param <T>  the type of the result 结果类型
   * @return  the result 结果
   */
  @Suspendable
  public static <T> T awaitResult(Consumer<Handler<AsyncResult<T>>> consumer) {
    try {
      return new AsyncAdaptor<T>() {
        @Override
        @Suspendable
        protected void requestAsync() {
          try {
            consumer.accept(this);
          } catch (Exception e) {
            throw new VertxException(e);
          }
        }
      }.run();
    } catch (Throwable t) {
      throw new VertxException(t);
    }
  }

  /**
   * Invoke an asynchronous operation and obtain the result synchronous.
   * The fiber will be blocked until the result is available. No kernel thread is blocked.
   *
   * @param consumer  this should encapsulate the asynchronous operation. The handler is passed to it.
   * @param timeout  In milliseconds when to cancel the awaited result
   * @param <T>  the type of the result
   * @return  the result or null in case of a time out
   */
  @Suspendable
  public static <T> T awaitResult(Consumer<Handler<AsyncResult<T>>> consumer, long timeout) {
    try {
      return new AsyncAdaptor<T>() {
        @Override
        @Suspendable
        protected void requestAsync() {
          try {
            consumer.accept(this);
          } catch (Exception e) {
            throw new VertxException(e);
          }
        }
      }.run(timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException to) {
      return null;
    } catch (Throwable t) {
      throw new VertxException(t);
    }
  }

  /**
   * Receive a single event from a handler synchronously.
   * The fiber will be blocked until the event occurs. No kernel thread is blocked.
   *
   * @param consumer  this should encapsulate the setting of the handler to receive the event. The handler is passed to it.
   * @param <T>  the type of the event
   * @return  the event
   */
  @Suspendable
  public static <T> T awaitEvent(Consumer<Handler<T>> consumer) {
    try {
      return new HandlerAdaptor<T>() {
        @Override
        @Suspendable
        protected void requestAsync() {
          try {
            consumer.accept(this);
          } catch (Exception e) {
            throw new VertxException(e);
          }
        }
      }.run();
    } catch (Throwable t) {
      throw new VertxException(t);
    }
  }

  /**
   * Receive a single event from a handler synchronously.
   * The fiber will be blocked until the event occurs. No kernel thread is blocked.
   *
   * @param consumer  this should encapsulate the setting of the handler to receive the event. The handler is passed to it.
   * @param timeout  In milliseconds when to cancel the awaited event
   * @param <T>  the type of the event
   * @return  the event
   */
  @Suspendable
  public static <T> T awaitEvent(Consumer<Handler<T>> consumer, long timeout) {
    try {
      return new HandlerAdaptor<T>() {
        @Suspendable
        @Override
        protected void requestAsync() {
          try {
            consumer.accept(this);
          } catch (Exception e) {
            throw new VertxException(e);
          }
        }
      }.run(timeout, TimeUnit.MILLISECONDS);
    } catch (TimeoutException to) {
        return null;
    } catch (Throwable t) {
      throw new VertxException(t);
    }
  }

  /**
   * Convert a standard handler to a handler which runs on a fiber. This is necessary if you want to do fiber blocking
   * synchronous operations in your handler.
   *
   * @param handler  the standard handler
   * @param <T>  the event type of the handler
   * @return  a wrapped handler that runs the handler on a fiber
   */
  @Suspendable
  public static <T> Handler<T> fiberHandler(Handler<T> handler) {
    FiberScheduler scheduler = getContextScheduler();
    return p -> new Fiber<Void>(scheduler, () -> handler.handle(p)).start();
  }

  /**
   * Create an adaptor that converts a stream of events from a handler into a receiver which allows the events to be
   * received synchronously.
   *
   * @param <T>  the type of the event
   * @return  the adaptor
   */
  @Suspendable
  public static <T> HandlerReceiverAdaptor<T> streamAdaptor() {
    return new HandlerReceiverAdaptorImpl<>(getContextScheduler());
  }

  /**
   * Like {@link #streamAdaptor()} but using the specified Quasar `Channel` instance. This is useful if you want to
   * fine-tune the behaviour of the adaptor.
   *
   * @param channel  the Quasar channel
   * @param <T>  the type of the event
   * @return  the adaptor
   */
  @Suspendable
  public static <T> HandlerReceiverAdaptor<T> streamAdaptor(Channel<T> channel) {
    return new HandlerReceiverAdaptorImpl<>(getContextScheduler(), channel);
  }

  /**
   * Get the `FiberScheduler` for the current context. There should be only one instance per context.
   * @return  the scheduler
   */
  @Suspendable
  public static FiberScheduler getContextScheduler() {
    Context context = Vertx.currentContext();
    if (context == null) {
      throw new IllegalStateException("Not in context");
    }
    if (!context.isEventLoopContext()) {
      throw new IllegalStateException("Not on event loop");
    }
    // We maintain one scheduler per context
    FiberScheduler scheduler = context.get(FIBER_SCHEDULER_CONTEXT_KEY);
    if (scheduler == null) {
      Thread eventLoop = Thread.currentThread();
      scheduler = new FiberExecutorScheduler("vertx.contextScheduler", command -> {
        if (Thread.currentThread() != eventLoop) {
          context.runOnContext(v -> command.run());
        } else {
          // Just run directly
          command.run();
        }
      });
      context.put(FIBER_SCHEDULER_CONTEXT_KEY, scheduler);
    }
    return scheduler;
  }

  /**
   * Remove the scheduler for the current context
   */
  @Suspendable
  public static void removeContextScheduler() {
    Context context = Vertx.currentContext();
    if (context != null) {
      context.remove(FIBER_SCHEDULER_CONTEXT_KEY);
    }
  }


}
