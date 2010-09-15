package edu.umass.nlp.trees;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.functional.Functional;
import edu.umass.nlp.functional.PredFn;
import edu.umass.nlp.io.IOUtils;
import edu.umass.nlp.utils.*;
import edu.umass.nlp.utils.Collections;

import java.io.File;
import java.util.*;

/**
 * Methods that can be performed
 * on an <code >ITree</code>.
 */
public class Trees {

  public static <L> boolean isLeaf(ITree<L> tree) {
    return tree.getChildren().isEmpty();
  }

  public static <L> boolean isPreLeaf(ITree<L> tree) {
    return tree.getChildren().size() == 1 &&
           isLeaf(tree.getChildren().get(0));
  }

  public static <L> String toString(ITree<L> tree) {
    StringBuilder sb = new StringBuilder();
    if (isLeaf(tree)) {
      sb.append(tree.getLabel().toString());
    } else {
      sb.append("(");
      sb.append(tree.getLabel().toString());
      List<ITree<L>> childs = tree.getChildren();
      for (int i = 0; i < childs.size(); i++) {
        ITree<L> child = childs.get(i);
        sb.append(" ");
        sb.append(toString(child));
      }
      sb.append(")");
    }
    return sb.toString();
  }

  public static <L> List<ITree<L>> getNodes(ITree<L> tree) {
    List<ITree<L>> res = new ArrayList<ITree<L>>();
    res.add(tree);
    for (ITree<L> c : tree.getChildren()) {
      res.addAll(getNodes(c));
    }
    return res;
  }

  public static <L> List<ITree<L>> getLeaves(ITree<L> tree) {
    return Functional.filter(getNodes(tree), new PredFn<ITree<L>>() {
      public boolean holdsAt(ITree<L> elem) {
        return isLeaf(elem);
      }});
  }

  public static <L> List<ITree<L>> getPreLeaves(ITree<L> tree) {
    return Functional.filter(getNodes(tree), new PredFn<ITree<L>>() {
      public boolean holdsAt(ITree<L> elem) {
        return isPreLeaf(elem);
      }});
  }

  public static <L> List<L> getPreLeafYield(ITree<L> tree) {
    return Functional.map(getPreLeaves(tree), new Fn<ITree<L>, L>() {
      public L apply(ITree<L> input) {
        return input.getLabel();
      }});    
  }

  public static <L> List<L> getLeafYield(ITree<L> t) {
    return Functional.map(getLeaves(t), new Fn<ITree<L>, L>() {
      public L apply(ITree<L> input) {
        return input.getLabel();
      }});      
  }

  public static <L>IdentityHashMap<ITree<L>, Span> getSpanMap(ITree<L> root) {
    return getSpanMap(root,0);
  }

  public static <L>IdentityHashMap<ITree<L>, Span> getSpanMap(ITree<L> root, int start) {
    IdentityHashMap<ITree<L>,Span> res = new IdentityHashMap<ITree<L>,Span>();
    int newStart = start;
    for (ITree<L> child : root.getChildren()) {
      res.putAll(getSpanMap(child, newStart));
      newStart += Trees.getLeafYield(child).size();
    }
    res.put(root, new Span(start,newStart));
    return res;
  }

  private static class TreeReader {
      static IPair<ITree<String>,List<Character>> nodeFromString(List<Character> chars) {
        if (chars.get(0) != '(') {
          throw new RuntimeException("Error");
        }
        chars = dropInitWhiteSpace(Collections.subList(chars,1));
        IPair<String, List<Character>> labelPair = labelFromString(chars);
        String label = labelPair.getFirst();
        List<Character> rest = labelPair.getSecond();
        final List<ITree<String>> children = new ArrayList<ITree<String>>();
        while (!rest.isEmpty() && rest.get(0) != ')') {
          IPair<ITree<String>,List<Character>> childPair = treeFromString(rest);
          children.add(childPair.getFirst());
          rest = dropInitWhiteSpace(childPair.getSecond());
        }
        rest = Collections.subList(rest,1);
        return BasicPair.<ITree<String>,List<Character>>make(new BasicTree(label,children),rest);
      }

      final static Set<Character> parens = Collections.<Character>set('(',')');

      static boolean isLabel(Character ch) {
        return !Character.isWhitespace(ch.charValue()) &&
               !parens.contains(ch);
      }

      static List<Character> dropInitWhiteSpace(List<Character> chars) {
        for (int i=0; i < chars.size(); ++i) {
          if (!Character.isWhitespace(chars.get(i))) {
            return Collections.subList(chars,i);
          }
        }
        return java.util.Collections.emptyList();
      }

      static IPair<String, List<Character>> labelFromString(List<Character> chars) {
        List<Character> labelChars = Functional.takeWhile(chars, new PredFn<Character>() {
          public boolean holdsAt(Character elem) {
            return isLabel(elem);
          }});
        List<Character> rest = Collections.subList(chars,labelChars.size());
        return BasicPair.make(StringUtils.toString(labelChars), dropInitWhiteSpace(rest));
      }

      static IPair<ITree<String>,List<Character>> treeFromString(List<Character> chars) {
        chars = dropInitWhiteSpace(chars);
        if (chars.isEmpty()) return null;
        try {
          if (chars.get(0) == '(') return nodeFromString(chars);
          else {
            IPair<String, List<Character>> labelPair = labelFromString(chars);
            ITree<String> leaf = new BasicTree<String>(labelPair.getFirst());
            return BasicPair.make(leaf, labelPair.getSecond());
          }
        } catch (Exception e) {
          throw new RuntimeException("Error parsing String: " + StringUtils.toString(chars));
        }
      }
    }

  public static ITree<String> readTree(String s) {
    return TreeReader.treeFromString(StringUtils.getCharacters(s)).getFirst();
  }

  public static Iterable<ITree<String>> readTrees(final String s) {
    return new Iterable<ITree<String>>() {
      List<Character> chars = TreeReader.dropInitWhiteSpace(StringUtils.getCharacters(s));
      public Iterator<ITree<String>> iterator() {
        return new Iterator<ITree<String>>() {
          public boolean hasNext() {
            return !chars.isEmpty();
          }

          public ITree<String> next() {
            IPair<ITree<String>,List<Character>> pair = TreeReader.treeFromString(chars);
            ITree<String> t =  pair.getFirst();
            chars = TreeReader.dropInitWhiteSpace(pair.getSecond());
            return t;
          }

          public void remove() {
            throw new RuntimeException("remove() not accepted");
          }
        };}};
  }

  public static void main(String[] args) {
//    ITree<String> c = new BasicTree("c",new ArrayList());
//    ITree<String> p = new BasicTree("p", Collections.makeList(c,c));
    ITree<String> t = readTree("(S (NP (DT the) (NN man)) (VP (VBD ran)))");
    System.out.println("spanMap: " + getSpanMap(t));
//    List<ITree<String>> tags = Functional.filter(getNodes(t), new PredFn<ITree<String>>() {
//      public boolean holdsAt(ITree<String> elem) {
//        return isPreLeaf(elem);
//      }});
//    Iterable<ITree<String>> trees =
//      readTrees(IOUtils.text(new File("/Users/aria42/Dropbox/projs/umass-nlp/trees.mrg")));
//    for (ITree<String> tree : trees) {
//      System.out.println(tree);
//    }
  }
  
}
