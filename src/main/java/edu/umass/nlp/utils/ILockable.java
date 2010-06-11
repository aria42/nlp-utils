package edu.umass.nlp.utils;

/**
 * Abstraction for an object with a locking state (boo!).
 * The behaviour of an object may change when it is locked.
 * A cannonical example is a collection you only want to be allowed
 * to be added to for a particular time.
 *
 * All implementations should issue a Logger.warn or crash if you try
 * to lock an already locked object. Safety first.  
 * @author aria42
 */
public interface ILockable {
  public boolean isLocked();
  public void lock();
}
