package edu.umass.nlp.functional;

import java.io.Serializable;

public interface Fn<I,O> extends Serializable {
	public O apply(I input);

  public static class ConstantFn<I,O> implements Fn<I,O>
  {

    private O c;

    public ConstantFn(O c) {
      this.c = c;
    }

    public O apply(I input) {
      return  c;
    }
  }

	public static class IdentityFn<I> implements Fn<I, I>
	{

		public I apply(I input)
		{
			return input;
		}
	}

}