package com.carrotsearch.sizeof;

import static com.carrotsearch.sizeof.RamUsageEstimator.*;
import java.util.Random;

import org.junit.*;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.junit.Assert.*;

public class TestSanity {
  private Random rnd;

  @Rule
  public TestRule randomness = new TestRule() {
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        public void evaluate() throws Throwable {
          final long seed = new Random().nextLong(); 
          rnd = new Random(seed);
          try {
            base.evaluate();
          } catch (Throwable t) {
            if (!(t instanceof AssumptionViolatedException)) {
              Throwable t2 = new Throwable("Test failed, seed=" + seed + ": " + t.getMessage(), t);
              t2.setStackTrace(new StackTraceElement[0]);
              throw t2;
            }
          }
          rnd = null;
        }
      };
    }
  }; 

  @Test
  public void testSanity() {
    assertTrue(sizeOf(new String("test string")) > shallowSizeOfInstance(String.class));

    Holder holder = new Holder();
    holder.holder = new Holder("string2", 5000L);
    assertTrue(sizeOf(holder) > shallowSizeOfInstance(Holder.class));
    assertTrue(sizeOf(holder) > sizeOf(holder.holder));
    
    assertTrue(
        shallowSizeOfInstance(HolderSubclass.class) >= shallowSizeOfInstance(Holder.class));
    assertTrue(
        shallowSizeOfInstance(Holder.class)         == shallowSizeOfInstance(HolderSubclass2.class));

    String[] strings = new String[] {
        new String("test string"),
        new String("hollow"), 
        new String("catchmaster")
    };
    assertTrue(sizeOf(strings) > shallowSizeOf(strings));
  }

  @Test
  public void testStaticOverloads() {
    {
      byte[] array = new byte[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      boolean[] array = new boolean[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      char[] array = new char[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      short[] array = new short[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      int[] array = new int[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      float[] array = new float[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      long[] array = new long[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
    
    {
      double[] array = new double[rnd.nextInt(1024)];
      assertEquals(sizeOf(array), sizeOf((Object) array));
    }
  }
  
  @Test
  public void testReferenceSize() {
    if (!isSupportedJVM()) {
      System.err.println("WARN: Your JVM does not support certain Oracle/Sun extensions.");
      System.err.println("      Memory estimates may be inaccurate.");
      System.err.println("      Please report this to the Lucene mailing list. JVM version: " + RamUsageEstimator.JVM_INFO_STRING);
      for (JvmFeature f : RamUsageEstimator.getUnsupportedFeatures()) {
        System.err.println("      - " + f.toString());
      }
    }

    assertTrue(NUM_BYTES_OBJECT_REF == 4 || NUM_BYTES_OBJECT_REF == 8);
    if (!Constants.JRE_IS_64BIT) {
      assertEquals("For 32bit JVMs, reference size must always be 4?", 4, NUM_BYTES_OBJECT_REF);
    }
  }

  @SuppressWarnings("unused")
  private static class Holder {
    long field1 = 5000L;
    String name = "name";
    Holder holder;
    long field2, field3, field4;
    
    Holder() {}
    
    Holder(String name, long field1) {
      this.name = name;
      this.field1 = field1;
    }
  }
  
  @SuppressWarnings("unused")
  private static class HolderSubclass extends Holder {
    byte foo;
    int bar;
  }
  
  private static class HolderSubclass2 extends Holder {
    // empty, only inherits all fields -> size should be identical to superclass
  }
}
