package edu.umass.nlp.utils;

public final class AtomicDouble implements java.io.Serializable {

    double x = 0.0;

    public AtomicDouble(double initialValue) {
      this.x = initialValue;
    }

    public AtomicDouble() {
      this.x = 0.0;
    }

    public double get() {
        synchronized (this) {
          return x;
        }
    }

    public void set(double newValue) {
        synchronized (this) {
          x = newValue;
        }
    }

    public double getAndSet(double newValue) {
        set(newValue);
        return get();
    }


    public double increment(double inc) {
        synchronized (this) {
          x += inc;
        }
        return get();
    }

}