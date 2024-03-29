package com.github.yvescheung.whisper

/**
 * The class to which this annotation is applied is not thread-safe. This
 * annotation primarily exists for clarifying the non-thread-safety of a class
 * that might otherwise be assumed to be thread-safe, despite the fact that it
 * is a bad idea to assume a class is thread-safe without good reason.
 *
 * @see ThreadSafe
 *
 * @author YvesCheung
 * 2020/11/30
 */
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class NotThreadSafe