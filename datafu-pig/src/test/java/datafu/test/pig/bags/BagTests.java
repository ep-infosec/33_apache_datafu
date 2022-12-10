/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package datafu.test.pig.bags;

import datafu.pig.bags.CountDistinctUpTo;
import datafu.pig.bags.CountEach;
import datafu.pig.bags.DistinctBy;
import datafu.pig.bags.Enumerate;
import datafu.pig.bags.FirstTupleFromBag;
import datafu.pig.bags.TupleFromBag;
import datafu.test.pig.PigTests;
import junit.framework.Assert;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.pig.Accumulator;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.SortedDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.pigunit.PigTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

import org.apache.pig.impl.util.Utils;
import org.apache.pig.builtin.Utf8StorageConverter;
import org.apache.pig.ResourceSchema.ResourceFieldSchema;

public class BagTests extends PigTests
{
  /**


  define NullToEmptyBag datafu.pig.bags.NullToEmptyBag();

  data = LOAD 'input' AS (B: bag {T: tuple(v:INT)});

  dump data;

  data2 = FOREACH data GENERATE NullToEmptyBag(B) as P;

  dump data2;

  STORE data2 INTO 'output';
   */
  @Multiline
  private String nullToEmptyBag;

  @Test
  public void nullToEmptyBagTest() throws Exception
  {
    PigTest test = createPigTestFromString(nullToEmptyBag);

    writeLinesToFile("input",
                     "({(1),(2),(3),(4),(5)})",
                     "()",
                     "{(4),(5)})");

    test.runScript();

    assertOutput(test, "data2",
                 "({(1),(2),(3),(4),(5)})",
                 "({})",
                 "({(4),(5)})");
  }

  /**


  define EmptyBagToNull datafu.pig.bags.EmptyBagToNull();

  data = LOAD 'input' AS (B: bag {T: tuple(v:INT)});

  dump data;

  data2 = FOREACH data GENERATE EmptyBagToNull(B) as P;

  dump data2;

  STORE data2 INTO 'output';
   */
  @Multiline
  private String emptyBagToNullTest;

  @Test
  public void emptyBagToNullTest() throws Exception
  {
    PigTest test = createPigTestFromString(emptyBagToNullTest);

    writeLinesToFile("input",
                     "({(1),(2),(3),(4),(5)})",
                     "()",
                     "({})",
                     "{(4),(5)})");

    test.runScript();

    assertOutput(test, "data2",
                 "({(1),(2),(3),(4),(5)})",
                 "()",
                 "()",
                 "({(4),(5)})");
  }

  /**


  define EmptyBagToNullFields datafu.pig.bags.EmptyBagToNullFields();

  data = LOAD 'input' AS (B: bag {T: tuple(v1:INT,v2:INT)});

  dump data;

  data2 = FOREACH data GENERATE EmptyBagToNullFields(B) as P;

  dump data2;

  STORE data2 INTO 'output';
   */
  @Multiline
  private String emptyBagToNullFieldsTest;

  @Test
  public void emptyBagToNullFieldsTest() throws Exception
  {
    PigTest test = createPigTestFromString(emptyBagToNullFieldsTest);

    writeLinesToFile("input",
                     "({(1,1),(2,2),(3,3),(4,4),(5,5)})",
                     "({})",
                     "{(4,4),(5,5)})");

    test.runScript();

    assertOutput(test, "data2",
                 "({(1,1),(2,2),(3,3),(4,4),(5,5)})",
                 "({(,)})",
                 "({(4,4),(5,5)})");
  }

  /**


  define AppendToBag datafu.pig.bags.AppendToBag();

  data = LOAD 'input' AS (key:INT, B: bag{T: tuple(v:INT)}, T: tuple(v:INT));

  data2 = FOREACH data GENERATE key, AppendToBag(B,T) as B;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String appendToBagTest;

  @Test
  public void appendToBagTest() throws Exception
  {
    PigTest test = createPigTestFromString(appendToBagTest);

    writeLinesToFile("input",
                     "1\t{(1),(2),(3)}\t(4)",
                     "2\t{(10),(20),(30),(40),(50)}\t(60)");

    test.runScript();

    assertOutput(test, "data2",
                 "(1,{(1),(2),(3),(4)})",
                 "(2,{(10),(20),(30),(40),(50),(60)})");
  }
  
  /**

define TupleFromBag datafu.pig.bags.TupleFromBag();

%declare emptyTuple TOTUPLE(0,'NO_NUMBER')

data = LOAD 'input' using PigStorage(',') AS (a:INT,b:CHARARRAY);

grouped = GROUP data BY a;

result1 = FOREACH grouped GENERATE group AS a, TupleFromBag(data, 0);

result2 = FOREACH grouped GENERATE group AS a, TupleFromBag(data,0).b as first_b, TupleFromBag(data,1).b as second_b;

result3 = FOREACH grouped GENERATE group AS a, TupleFromBag(data,0).b as first_b, TupleFromBag(data,3).b as forth_b;

result4 = FOREACH grouped GENERATE group AS a,TupleFromBag(data,0,$emptyTuple).b as first_b, TupleFromBag(data,3,$emptyTuple).b as forth_b;

   **/

  @Multiline
  private String tupleFromBagTest;

  @Test
  public void tupleFromBagTest() throws Exception
  {
	  PigTest test = createPigTestFromString(tupleFromBagTest);

	  writeLinesToFile("input",
              "1,a",
              "1,b",
              "1,c",
              "2,d",
              "2,e",
              "2,f",
              "3,g",
              "3,h",
              "3,i");

	  test.runScript();

	  assertOutput(test, "result1",
              "(1,(1,c))",
              "(2,(2,f))",
              "(3,(3,i))");

	  assertOutput(test, "result2",
              "(1,c,b)",
              "(2,f,e)",
              "(3,i,h)");

	  assertOutput(test, "result3",
              "(1,c,)",
              "(2,f,)",
              "(3,i,)");

	  assertOutput(test, "result4",
              "(1,c,NO_NUMBER)",
              "(2,f,NO_NUMBER)",
              "(3,i,NO_NUMBER)");

  }

  @Test
  public void tupleFromBagAccumulateTest() throws Exception
  {
    TupleFactory tf = TupleFactory.getInstance();
    BagFactory bf = BagFactory.getInstance();
 
    TupleFromBag op = new TupleFromBag();
    
    Tuple defaultValue = tf.newTuple(1000);
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(4))), 0, defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(9))), 0, defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(16))), 0, defaultValue)));
    assertEquals(op.getValue(), tf.newTuple(4));
    op.cleanup();
    
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(11))), 1, defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(17))), 1, defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(5))), 1, defaultValue)));
    assertEquals(op.getValue(), tf.newTuple(17));
    op.cleanup();
    
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(), 2, defaultValue)));
    assertEquals(op.getValue(), defaultValue);
    op.cleanup();
  }


 /**


  define FirstTupleFromBag datafu.pig.bags.FirstTupleFromBag();

  data = LOAD 'input' AS (key:INT, B: bag{T: tuple(v:INT)});

  data2 = FOREACH data GENERATE key, FirstTupleFromBag(B, null) as B;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String firstTupleFromBagTest;

   @Test
  public void firstTupleFromBagTest() throws Exception
  {
    PigTest test = createPigTestFromString(firstTupleFromBagTest);

    writeLinesToFile("input", "1\t{(4),(9),(16)}");

    test.runScript();

    assertOutput(test, "data2", "(1,(4))");
  }
   
  @Test
  public void firstTupleFromBagAccumulateTest() throws Exception
  {
    TupleFactory tf = TupleFactory.getInstance();
    BagFactory bf = BagFactory.getInstance();
 
    FirstTupleFromBag op = new FirstTupleFromBag();
    
    Tuple defaultValue = tf.newTuple(1000);
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(4))), defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(9))), defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(16))), defaultValue)));
    assertEquals(op.getValue(), tf.newTuple(4));
    op.cleanup();
    
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(11))), defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(17))), defaultValue)));
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(Arrays.asList(tf.newTuple(5))), defaultValue)));
    assertEquals(op.getValue(), tf.newTuple(11));
    op.cleanup();
    
    op.accumulate(tf.newTuple(Arrays.asList(bf.newDefaultBag(), defaultValue)));
    assertEquals(op.getValue(), defaultValue);
    op.cleanup();
  }

  /**


  define PrependToBag datafu.pig.bags.PrependToBag();

  data = LOAD 'input' AS (key:INT, B: bag{T: tuple(v:INT)}, T: tuple(v:INT));

  data2 = FOREACH data GENERATE key, PrependToBag(B,T) as B;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String prependToBagTest;

  @Test
  public void prependToBagTest() throws Exception
  {
    PigTest test = createPigTestFromString(prependToBagTest);

    writeLinesToFile("input",
                     "1\t{(1),(2),(3)}\t(4)",
                     "2\t{(10),(20),(30),(40),(50)}\t(60)");

    test.runScript();

    assertOutput(test, "data2",
                 "(1,{(4),(1),(2),(3)})",
                 "(2,{(60),(10),(20),(30),(40),(50)})");
  }

  /**


  define BagConcat datafu.pig.bags.BagConcat();

  data = LOAD 'input' AS (A: bag{T: tuple(v:INT)}, B: bag{T: tuple(v:INT)}, C: bag{T: tuple(v:INT)});

  describe data;

  data2 = FOREACH data GENERATE BagConcat(A,B,C);

  describe data2;

  STORE data2 INTO 'output';
   */
  @Multiline
  private String bagConcatTest;

  @Test
  public void bagConcatTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagConcatTest);

    writeLinesToFile("input",
                     "({(1),(2),(3)}\t{(3),(5),(6)}\t{(10),(13)})",
                     "({(2),(3),(4)}\t{(5),(5)}\t{(20)})");

    test.runScript();

    assertOutput(test, "data2",
                 "({(1),(2),(3),(3),(5),(6),(10),(13)})",
                 "({(2),(3),(4),(5),(5),(20)})");
  }

  /**


  define UnorderedPairs datafu.pig.bags.UnorderedPairs();

  data = LOAD 'input' AS (B: bag {T: tuple(v:INT)});

  data2 = FOREACH data GENERATE UnorderedPairs(B) as P;

  data3 = FOREACH data2 GENERATE FLATTEN(P);

  data4 = FOREACH data3 GENERATE FLATTEN(elem1), FLATTEN(elem2);

  data5 = ORDER data4 BY $0, $1;

  STORE data5 INTO 'output';


   */
  @Multiline
  private String unorderedPairsTest;

  @Test
  public void unorderedPairsTest() throws Exception
  {
    PigTest test = createPigTestFromString(unorderedPairsTest);

    String[] input = {
      "{(1),(2),(3),(4),(5)}"
    };

    String[] output = {
        "(1,2)",
        "(1,3)",
        "(1,4)",
        "(1,5)",
        "(2,3)",
        "(2,4)",
        "(2,5)",
        "(3,4)",
        "(3,5)",
        "(4,5)"
      };

    test.assertOutput("data",input,"data4",output);
  }

  /**


  define UnorderedPairs datafu.pig.bags.UnorderedPairs();

  data = LOAD 'input' AS (A:int, B: bag {T: tuple(v:INT)});

  data2 = FOREACH data GENERATE A, UnorderedPairs(B) as P;

  data3 = FOREACH data2 GENERATE A, FLATTEN(P);

  STORE data3 INTO 'output';

   */
  @Multiline
  private String unorderedPairsTest2;

  @Test
  public void unorderedPairsTest2() throws Exception
  {
    PigTest test = createPigTestFromString(unorderedPairsTest2);

    this.writeLinesToFile("input", "1\t{(1),(2),(3),(4),(5)}");

    test.runScript();
    this.getLinesForAlias(test, "data3");

    this.assertOutput(test, "data3",
                      "(1,(1),(2))",
                      "(1,(1),(3))",
                      "(1,(1),(4))",
                      "(1,(1),(5))",
                      "(1,(2),(3))",
                      "(1,(2),(4))",
                      "(1,(2),(5))",
                      "(1,(3),(4))",
                      "(1,(3),(5))",
                      "(1,(4),(5))");
  }

  /**


  define BagSplit datafu.pig.bags.BagSplit();

  data = LOAD 'input' AS (B:bag{T:tuple(val1:INT,val2:INT)});

  data2 = FOREACH data GENERATE BagSplit($MAX,B);
  --describe data2;

  data3 = FOREACH data2 GENERATE FLATTEN($0);

  --describe data3

  STORE data3 INTO 'output';

   */
  @Multiline
  private String bagSplitTest;

  @Test
  public void bagSplitTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagSplitTest,
                                 "MAX=5");

    writeLinesToFile("input",
                     "{(1,11),(2,22),(3,33),(4,44),(5,55),(6,66),(7,77),(8,88),(9,99),(10,1010),(11,1111),(12,1212)}");

    test.runScript();

    assertOutput(test, "data3",
                 "({(1,11),(2,22),(3,33),(4,44),(5,55)})",
                 "({(6,66),(7,77),(8,88),(9,99),(10,1010)})",
                 "({(11,1111),(12,1212)})");
  }

  /**


  define BagSplit datafu.pig.bags.BagSplit('true');

  data = LOAD 'input' AS (B:bag{T:tuple(val1:INT,val2:INT)});

  data2 = FOREACH data GENERATE BagSplit($MAX,B);

  data3 = FOREACH data2 GENERATE FLATTEN($0);

  STORE data3 INTO 'output';
   */
  @Multiline
  private String bagSplitWithBagNumTest;

  @Test
  public void bagSplitWithBagNumTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagSplitWithBagNumTest,
                                 "MAX=10");

    writeLinesToFile("input",
                     "{(1,11),(2,22),(3,33),(4,44),(5,55),(6,66),(7,77),(8,88),(9,99),(10,1010),(11,1111),(12,1212)}");

    test.runScript();

    assertOutput(test, "data3",
                 "({(1,11),(2,22),(3,33),(4,44),(5,55),(6,66),(7,77),(8,88),(9,99),(10,1010)},0)",
                 "({(11,1111),(12,1212)},1)");
  }

  /**


  define Enumerate datafu.pig.bags.ReverseEnumerate('1');

  data = LOAD 'input' AS (data: bag {T: tuple(v1:INT,B: bag{T: tuple(v2:INT)})});

  data2 = FOREACH data GENERATE Enumerate(data);
  --describe data2;

  data3 = FOREACH data2 GENERATE FLATTEN($0);
  --describe data3;

  data4 = FOREACH data3 GENERATE $0 as v1, $1 as B, $2 as i;
  --describe data4;

  STORE data4 INTO 'output';

   */
  @Multiline
  private String enumerateWithReverseTest;

  @Test
  public void enumerateWithReverseTest() throws Exception
  {
    PigTest test = createPigTestFromString(enumerateWithReverseTest);

    writeLinesToFile("input",
                     "({(10,{(1),(2),(3)}),(20,{(4),(5),(6)}),(30,{(7),(8)}),(40,{(9),(10),(11)}),(50,{(12),(13),(14),(15)})})");

    test.runScript();

    assertOutput(test, "data4",
                 "(10,{(1),(2),(3)},5)",
                 "(20,{(4),(5),(6)},4)",
                 "(30,{(7),(8)},3)",
                 "(40,{(9),(10),(11)},2)",
                 "(50,{(12),(13),(14),(15)},1)");
  }

  /**


  define Enumerate datafu.pig.bags.Enumerate('1');

  data = LOAD 'input' AS (data: bag {T: tuple(v1:INT,B: bag{T: tuple(v2:INT)})});

  data2 = FOREACH data GENERATE Enumerate(data);
  --describe data2;

  data3 = FOREACH data2 GENERATE FLATTEN($0);
  --describe data3;

  data4 = FOREACH data3 GENERATE $0 as v1, $1 as B, $2 as i;
  --describe data4;

  STORE data4 INTO 'output';

   */
  @Multiline
  private String enumerateWithStartTest;

  @Test
  public void enumerateWithStartTest() throws Exception
  {
    PigTest test = createPigTestFromString(enumerateWithStartTest);

    writeLinesToFile("input",
                     "({(10,{(1),(2),(3)}),(20,{(4),(5),(6)}),(30,{(7),(8)}),(40,{(9),(10),(11)}),(50,{(12),(13),(14),(15)})})");

    test.runScript();

    assertOutput(test, "data4",
                 "(10,{(1),(2),(3)},1)",
                 "(20,{(4),(5),(6)},2)",
                 "(30,{(7),(8)},3)",
                 "(40,{(9),(10),(11)},4)",
                 "(50,{(12),(13),(14),(15)},5)");
  }

  /**


  define Enumerate datafu.pig.bags.Enumerate();

  data = LOAD 'input' AS (data: bag {T: tuple(v1:INT,B: bag{T: tuple(v2:INT)})});

  data2 = FOREACH data GENERATE Enumerate(data);
  --describe data2;

  data3 = FOREACH data2 GENERATE FLATTEN($0);
  --describe data3;

  data4 = FOREACH data3 GENERATE $0 as v1, $1 as B, $2 as i;
  --describe data4;

  STORE data4 INTO 'output';

   */
  @Multiline
  private String enumerateTest;

  @Test
  public void enumerateTest() throws Exception
  {
    PigTest test = createPigTestFromString(enumerateTest);

    writeLinesToFile("input",
                     "({(10,{(1),(2),(3)}),(20,{(4),(5),(6)}),(30,{(7),(8)}),(40,{(9),(10),(11)}),(50,{(12),(13),(14),(15)})})");

    test.runScript();

    assertOutput(test, "data4",
                 "(10,{(1),(2),(3)},0)",
                 "(20,{(4),(5),(6)},1)",
                 "(30,{(7),(8)},2)",
                 "(40,{(9),(10),(11)},3)",
                 "(50,{(12),(13),(14),(15)},4)");
  }

  @Test
  public void enumerateTest2() throws Exception
  {
    PigTest test = createPigTestFromString(enumerateTest);

    writeLinesToFile("input",
                     "({(10,{(1),(2),(3)}),(20,{(4),(5),(6)}),(30,{(7),(8)}),(40,{(9),(10),(11)}),(50,{(12),(13),(14),(15)})})",
                     "({(11,{(11),(12),(13),(14)}),(21,{(15),(16),(17),(18)}),(31,{(19),(20)}),(41,{(21),(22),(23),(24)}),(51,{(25),(26),(27)})})");

    test.runScript();

    assertOutput(test, "data4",
                 "(10,{(1),(2),(3)},0)",
                 "(20,{(4),(5),(6)},1)",
                 "(30,{(7),(8)},2)",
                 "(40,{(9),(10),(11)},3)",
                 "(50,{(12),(13),(14),(15)},4)",
                 "(11,{(11),(12),(13),(14)},0)",
                 "(21,{(15),(16),(17),(18)},1)",
                 "(31,{(19),(20)},2)",
                 "(41,{(21),(22),(23),(24)},3)",
                 "(51,{(25),(26),(27)},4)");
  }

  /*
   * Testing "Accumulator" part of Enumeration by manually invoking accumulate(), getValue() and cleanup()
   */
  @Test
  public void enumerateAccumulatorTest() throws Exception
  {
    Enumerate enumerate = new Enumerate();

    Tuple tuple1 = TupleFactory.getInstance().newTuple(1);
    tuple1.set(0, 10);

    Tuple tuple2 = TupleFactory.getInstance().newTuple(1);
    tuple2.set(0, 20);

    Tuple tuple3 = TupleFactory.getInstance().newTuple(1);
    tuple3.set(0, 30);

    Tuple tuple4 = TupleFactory.getInstance().newTuple(1);
    tuple4.set(0, 40);

    Tuple tuple5 = TupleFactory.getInstance().newTuple(1);
    tuple5.set(0, 50);

    DataBag bag1 = BagFactory.getInstance().newDefaultBag();
    bag1.add(tuple1);
    bag1.add(tuple2);
    bag1.add(tuple3);

    DataBag bag2 = BagFactory.getInstance().newDefaultBag();
    bag2.add(tuple4);
    bag2.add(tuple5);

    Tuple inputTuple1 = TupleFactory.getInstance().newTuple(1);
    inputTuple1.set(0,bag1);

    Tuple inputTuple2 = TupleFactory.getInstance().newTuple(1);
    inputTuple2.set(0,bag2);

    enumerate.accumulate(inputTuple1);
    enumerate.accumulate(inputTuple2);
    assertEquals(enumerate.getValue().toString(), "{(10,0),(20,1),(30,2),(40,3),(50,4)}");

    // Testing that cleanup code is correct by calling cleanup() and passing inputs back to Enumerate instance
    enumerate.cleanup();
    enumerate.accumulate(inputTuple1);
    enumerate.accumulate(inputTuple2);
    assertEquals(enumerate.getValue().toString(), "{(10,0),(20,1),(30,2),(40,3),(50,4)}");
  }

  /**


  define BagSplit datafu.pig.bags.BagSplit();
  define Enumerate datafu.pig.bags.Enumerate('1');

  data = LOAD 'input' AS (data: bag {T: tuple(name:CHARARRAY, score:double)});

  data2 = FOREACH data GENERATE BagSplit(3,data) as the_bags;

  --describe data2

  data3 = FOREACH data2 GENERATE Enumerate(the_bags) as enumerated_bags;

  --describe data3

  data4 = FOREACH data3 GENERATE FLATTEN(enumerated_bags) as (data,i);

  --describe data4

  data5 = FOREACH data4 GENERATE data as the_data, i as the_key;

  --describe data5

  data_out = FOREACH data5 GENERATE FLATTEN(the_data), the_key;

  --describe data_out
   */
  @Multiline
  private String comprehensiveBagSplitAndEnumerate;

  @Test
  public void comprehensiveBagSplitAndEnumerate() throws Exception
  {
    PigTest test = createPigTestFromString(comprehensiveBagSplitAndEnumerate);

    writeLinesToFile("input",
                     "({(A,1.0),(B,2.0),(C,3.0),(D,4.0),(E,5.0)})");

    test.runScript();

    assertOutput(test, "data_out",
                 // bag #1
                 "(A,1.0,1)",
                 "(B,2.0,1)",
                 "(C,3.0,1)",
                 // bag #2
                 "(D,4.0,2)",
                 "(E,5.0,2)");
  }

  /**


  define DistinctBy datafu.pig.bags.DistinctBy('0');

  data = LOAD 'input' AS (data: bag {T: tuple(a:CHARARRAY, b:INT, c:INT)});

  data2 = FOREACH data GENERATE DistinctBy(data);

  --describe data2;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String distinctByTest;

  @Test
  public void distinctByTest() throws Exception
  {
    PigTest test = createPigTestFromString(distinctByTest);

    writeLinesToFile("input",
                     "({(Z,1,0),(A,1,0),(A,1,0),(B,2,0),(B,22,1),(C,3,0),(D,4,0),(E,5,0)})",
                     "({(A,10,2),(M,50,3),(A,34,49), (A,24,42), (Z,49,22),(B,1,1)},(B,2,2))");

    test.runScript();

    assertOutput(test, "data2",
                 "({(Z,1,0),(A,1,0),(B,2,0),(C,3,0),(D,4,0),(E,5,0)})",
                 "({(A,10,2),(M,50,3),(Z,49,22),(B,1,1)})");
  }

  /**

  define DistinctBy datafu.pig.bags.DistinctBy('1', '2');

  data = LOAD 'input' AS (data: bag {T: tuple(a:CHARARRAY, b:map[INT], c:bag{t: tuple(c0:CHARARRAY, c1:INT)})});

  data2 = FOREACH data GENERATE DistinctBy(data);

  --describe data2;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String distinctByMultiComplexFieldTest;

  @Test
  public void distinctByMultiComplexFieldTest() throws Exception
  {
    PigTest test = createPigTestFromString(distinctByMultiComplexFieldTest);

    writeLinesToFile("input",
                     "({(a-b,[b#1],{(a-b,0),(a-b,1)}),(a-c,[b#1],{(a-b,0),(a-b,1)}),(a-d,[b#0],{(a-b,1),(a-b,2)})})");

    test.runScript();

    assertOutput(test, "data2",
                 "({(a-b,[b#1],{(a-b,0),(a-b,1)}),(a-d,[b#0],{(a-b,1),(a-b,2)})})");
  }

  /**

  define DistinctBy datafu.pig.bags.DistinctBy('1');

  data = LOAD 'input' AS (data: bag {T: tuple(a:CHARARRAY, b:CHARARRAY)});

  data2 = FOREACH data GENERATE DistinctBy(data);

  --describe data2;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String distinctByDelimTest;

  @Test
  public void distinctByDelimTest() throws Exception
  {
    PigTest test = createPigTestFromString(distinctByDelimTest);

    writeLinesToFile("input",
                     "({(a-b,c),(a-b,d)})");

    test.runScript();

    assertOutput(test, "data2",
                 "({(a-b,c),(a-b,d)})");
  }

  @Test
  public void distinctByExecTest() throws Exception
  {
    DistinctBy distinct = new DistinctBy("0");

    DataBag bag;
    Tuple input;
    Tuple data;

    bag = BagFactory.getInstance().newDefaultBag();
    input = TupleFactory.getInstance().newTuple(bag);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 10);
    data.set(1, 20);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 11);
    data.set(1, 50);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 10);
    data.set(1, 22);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 12);
    data.set(1, 40);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 11);
    data.set(1, 50);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 11);
    data.set(1, 51);

    DataBag result = distinct.exec(input);

    Assert.assertEquals(3, result.size());

    Iterator<Tuple> iter = result.iterator();
    Assert.assertEquals("(10,20)", iter.next().toString());
    Assert.assertEquals("(11,50)", iter.next().toString());
    Assert.assertEquals("(12,40)", iter.next().toString());

    // do it again to test cleanup
    bag = BagFactory.getInstance().newDefaultBag();
    input = TupleFactory.getInstance().newTuple(bag);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 12);
    data.set(1, 42);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 11);
    data.set(1, 51);

    data = TupleFactory.getInstance().newTuple(2);
    bag.add(data);
    data.set(0, 11);
    data.set(1, 50);

    result = distinct.exec(input);

    Assert.assertEquals(2, result.size());

    iter = result.iterator();
    Assert.assertEquals("(12,42)", iter.next().toString());
    Assert.assertEquals("(11,51)", iter.next().toString());
  }

  private void firstAccumulateForTests(Accumulator distinct) throws IOException {
	    DataBag bag;
	    Tuple input;
	    Tuple data;

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 10);
	    data.set(1, 20);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 11);
	    data.set(1, 50);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 10);
	    data.set(1, 22);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 12);
	    data.set(1, 40);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 11);
	    data.set(1, 50);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 11);
	    data.set(1, 51);
	    distinct.accumulate(input);
}

  private void secondAccumulateForTests(Accumulator distinct) throws IOException {
	    DataBag bag;
	    Tuple input;
	    Tuple data;

	    // do it again to test cleanup
	    distinct.cleanup();

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 12);
	    data.set(1, 42);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 11);
	    data.set(1, 51);
	    distinct.accumulate(input);

	    data = TupleFactory.getInstance().newTuple(2);
	    bag = BagFactory.getInstance().newDefaultBag();
	    bag.add(data);
	    input = TupleFactory.getInstance().newTuple(bag);
	    data.set(0, 11);
	    data.set(1, 50);
	    distinct.accumulate(input);
  }
  
  @Test
  public void distinctByAccumulateTest() throws Exception
  {
    DistinctBy distinct = new DistinctBy("0");

    firstAccumulateForTests(distinct);
    
    DataBag result = distinct.getValue();

    Assert.assertEquals(3, result.size());

    Iterator<Tuple> iter = result.iterator();
    Assert.assertEquals("(10,20)", iter.next().toString());
    Assert.assertEquals("(11,50)", iter.next().toString());
    Assert.assertEquals("(12,40)", iter.next().toString());

    secondAccumulateForTests(distinct);

    result = distinct.getValue();

    Assert.assertEquals(2, result.size());

    iter = result.iterator();
    Assert.assertEquals("(12,42)", iter.next().toString());
    Assert.assertEquals("(11,51)", iter.next().toString());
  }

  /**


  define CountEach datafu.pig.bags.CountEach();

  data = LOAD 'input' AS (data: bag {T: tuple(v1:chararray)});

  data2 = FOREACH data GENERATE CountEach(data) as counted;
  --describe data2;

  data3 = FOREACH data2 {
    ordered = ORDER counted BY count DESC;
    GENERATE ordered;
  }
  --describe data3

  STORE data3 INTO 'output';

   */
  @Multiline
  private String countEachTest;

  @Test
  public void countEachTest() throws Exception
  {
    PigTest test = createPigTestFromString(countEachTest);

    writeLinesToFile("input",
                     "({(A),(B),(A),(C),(A),(B)})");

    test.runScript();

    assertOutput(test, "data3",
        "({((A),3),((B),2),((C),1)})");
  }

  @Test
  public void countEachExecAndAccumulateTest() throws Exception
  {
    for (int c=0; c<2; c++)
    {
      CountEach countEach = new CountEach("flatten");

      DataBag bag = BagFactory.getInstance().newDefaultBag();
      {
        Tuple t = TupleFactory.getInstance().newTuple(1);
        t.set(0, "A");
        bag.add(t);
      }
      {
        Tuple t = TupleFactory.getInstance().newTuple(1);
        t.set(0, "B");
        bag.add(t);
      }
      {
        Tuple t = TupleFactory.getInstance().newTuple(1);
        t.set(0, "B");
        bag.add(t);
      }
      {
        Tuple t = TupleFactory.getInstance().newTuple(1);
        t.set(0, "C");
        bag.add(t);
      }
      {
        Tuple t = TupleFactory.getInstance().newTuple(1);
        t.set(0, "A");
        bag.add(t);
      }
      {
        Tuple t = TupleFactory.getInstance().newTuple(1);
        t.set(0, "D");
        bag.add(t);
      }

      DataBag output = null;

      if (c == 0)
      {
        Tuple input = TupleFactory.getInstance().newTuple(1);
        input.set(0, bag);

        System.out.println("Testing exec");
        output = countEach.exec(input);
      }
      else
      {
        System.out.println("Testing accumulate");
        for (Tuple t : bag)
        {
          DataBag tb = BagFactory.getInstance().newDefaultBag();
          tb.add(t);
          Tuple input = TupleFactory.getInstance().newTuple(1);
          input.set(0, tb);
          countEach.accumulate(input);
        }

        output = countEach.getValue();

        countEach.cleanup();
        Assert.assertEquals(0, countEach.getValue().size());
      }

      System.out.println(output.toString());

      Assert.assertEquals(4, output.size());
      Set<String> found = new HashSet<String>();
      for (Tuple t : output)
      {
        String key = (String)t.get(0);
        found.add(key);
        if (key == "A")
        {
          Assert.assertEquals(2, t.get(1));
        }
        else if (key == "B")
        {
          Assert.assertEquals(2, t.get(1));
        }
        else if (key == "C")
        {
          Assert.assertEquals(1, t.get(1));
        }
        else if (key == "D")
        {
          Assert.assertEquals(1, t.get(1));
        }
        else
        {
          Assert.fail("Unexpected: " + key);
        }
      }
      Assert.assertEquals(4, found.size());
    }
  }

  /**


  define CountEach datafu.pig.bags.CountEach('flatten');

  data = LOAD 'input' AS (data: bag {T: tuple(v1:chararray)});

  data2 = FOREACH data GENERATE CountEach(data) as counted;
  --describe data2;

  data3 = FOREACH data2 {
    ordered = ORDER counted BY count DESC;
    GENERATE ordered;
  }
  --describe data3

  STORE data3 INTO 'output';

   */
  @Multiline
  private String countEachFlattenTest;

  @Test
  public void countEachFlattenTest() throws Exception
  {
    PigTest test = createPigTestFromString(countEachFlattenTest);

    writeLinesToFile("input",
                     "({(A),(B),(A),(C),(A),(B)})");

    test.runScript();

    assertOutput(test, "data3",
        "({(A,3),(B,2),(C,1)})");
  }

  /**

  define CountDistinctUpTo3 datafu.pig.bags.CountDistinctUpTo('3');
  define CountDistinctUpTo10 datafu.pig.bags.CountDistinctUpTo('10');

  data = LOAD 'input' AS (bag1: bag {T: tuple(t1:chararray, t2:int)});

  data2 = FOREACH data GENERATE CountDistinctUpTo3(bag1) as counted;

  data3 = FOREACH data GENERATE CountDistinctUpTo10(bag1) as counted;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String countDistinctUpToTest;

  @Test
  public void countDistinctUpToTest() throws Exception {
    PigTest test = createPigTestFromString(countDistinctUpToTest);

    writeLinesToFile("input", "({(A,0),(B,0),(D,0),(A,0),(C,0),(E,0),(A,0),(B,0),(A,0),(B,0)})");
    test.runScript();

    assertOutput(test, "data2", "(3)");
    assertOutput(test, "data3", "(5)");
    
    writeLinesToFile("input", "({(A,0),(B,2),(D,0),(A,1),(C,3),(E,2),(A,1),(B,2),(A,0),(B,2),(E,0)})");
    test.runScript();

    assertOutput(test, "data2", "(3)");
    assertOutput(test, "data3", "(7)");

  }

  @Test
  public void countDistinctUpToAccumulatorTest() throws IOException
  {
    CountDistinctUpTo distinct = new CountDistinctUpTo("2");

    firstAccumulateForTests(distinct);
    
    int result = distinct.getValue();

    Assert.assertEquals(2, result);

    secondAccumulateForTests(distinct);

    result = distinct.getValue();

    Assert.assertEquals(2, result);
  }
  
  private void countDistinctUpToAlgebraic(String amount, int expected) throws IOException
  {
    CountDistinctUpTo.Initial initial = new CountDistinctUpTo.Initial(amount);
    CountDistinctUpTo.Intermediate intermediate = new CountDistinctUpTo.Intermediate(amount);
    CountDistinctUpTo.Final finalFunc = new CountDistinctUpTo.Final(amount);

    DataBag intermediateBag = BagFactory.getInstance().newDefaultBag();
    
    for (int i=0; i<100; i++) {
        DataBag innerBag = BagFactory.getInstance().newDefaultBag();
        innerBag.add(TupleFactory.getInstance().newTuple((Object)(i % 20)));
    	Tuple initialInput = TupleFactory.getInstance().newTuple(innerBag);
    	intermediateBag.add(initial.exec(initialInput));
    }

    Tuple intermediateOutput = intermediate.exec(TupleFactory.getInstance().newTuple(intermediateBag));
    intermediateBag = BagFactory.getInstance().newDefaultBag(Arrays.asList(intermediateOutput));
    Integer result = finalFunc.exec(TupleFactory.getInstance().newTuple(intermediateBag));

    Assert.assertEquals(expected, result.intValue());
    
    intermediateBag = BagFactory.getInstance().newDefaultBag();
    
    for (int i=0; i<10; i++) {
        DataBag innerBag = BagFactory.getInstance().newDefaultBag();
        innerBag.add(TupleFactory.getInstance().newTuple((Object)(i % 3)));
    	Tuple initialInput = TupleFactory.getInstance().newTuple(innerBag);
    	intermediateBag.add(initial.exec(initialInput));
    }

    intermediateOutput = intermediate.exec(TupleFactory.getInstance().newTuple(intermediateBag));
    intermediateBag = BagFactory.getInstance().newDefaultBag(Arrays.asList(intermediateOutput));
    result = finalFunc.exec(TupleFactory.getInstance().newTuple(intermediateBag));
    
    Assert.assertEquals(3, result.intValue());

  }

  @Test
  public void countDistinctUpToAlgebraicTest() throws IOException
  {
	  // check flow in which Intermediate passes all the distinct tuples it finds
	  countDistinctUpToAlgebraic("50", 20); 
	  
	  // check flow in which Intermediate passes the max count as a null in its result
	  countDistinctUpToAlgebraic("5", 5);
  }

  /**



  define BagLeftOuterJoin datafu.pig.bags.BagLeftOuterJoin();

  data = LOAD 'input' AS (outer_key:chararray, bag1:bag{T:tuple(k:chararray,v:chararray)}, bag2:bag{T:tuple(k:chararray,v:chararray)}, bag3:bag{T:tuple(k3:chararray,v3:chararray)});
  describe data;

  data2 = FOREACH data GENERATE
    outer_key,
    BagLeftOuterJoin(bag1, 'k', bag2, 'k', bag3, 'k3') as joined1,
    BagLeftOuterJoin(bag1, 'k', bag3, 'k3', bag2, 'k') as joined2; --this will break without UDF signature and pig < 0.11
  describe data2;

  STORE data2 INTO 'output';

   */
  @Multiline
  private String bagLeftOuterJoinTest;

  @Test
  public void bagLeftOuterJoinTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagLeftOuterJoinTest);

    writeLinesToFile("input",
                     "1\t{(K1,A1),(K2,B1),(K3,C1)}\t{(K1,A2),(K2,B2),(K2,B22)}\t{(K1,A3),(K3,C3),(K4,D3)}");

    test.runScript();
    
    List<Tuple> tuples = getLinesForAlias(test, "data2");
    assertEquals(tuples.size(), 1);
    Tuple tuple = tuples.get(0);
    DataBag joined1 = (DataBag)tuple.get(1);
    DataBag joined2 = (DataBag)tuple.get(2);
    
    String joined1Schema = "{(bag1::k: chararray,bag1::v: chararray,bag2::k: chararray,bag2::v: chararray,bag3::k3: chararray,bag3::v3: chararray)}";
    String joined2Schema = "{(bag1::k: chararray,bag1::v: chararray,bag3::k3: chararray,bag3::v3: chararray,bag2::k: chararray,bag2::v: chararray)}";
    String expectedJoined1 = "{(K1,A1,K1,A2,K1,A3),(K2,B1,K2,B2,,),(K2,B1,K2,B22,,),(K3,C1,,,K3,C3)}";
    String expectedJoined2 = "{(K1,A1,K1,A3,K1,A2),(K2,B1,,,K2,B2),(K2,B1,,,K2,B22),(K3,C1,K3,C3,,)}";
    
    // compare sorted bags because there is no guarantee on the order
    assertEquals(getSortedBag(joined1).toString(),getSortedBag(expectedJoined1, joined1Schema).toString());
    assertEquals(getSortedBag(joined2).toString(),getSortedBag(expectedJoined2, joined2Schema).toString());
  }

    /**


     define BagFullOuterJoin datafu.pig.bags.BagJoin('full');

     data = LOAD 'input' AS (outer_key:chararray, bag1:bag{T:tuple(k:chararray,v:chararray)}, bag2:bag{T:tuple(k:chararray,v:chararray)}, bag3:bag{T:tuple(k3:chararray,v3:chararray)});
     describe data;

     data2 = FOREACH data GENERATE
     outer_key,
     BagFullOuterJoin(bag1, 'k', bag2, 'k', bag3, 'k3') as joined1,
     BagFullOuterJoin(bag1, 'k', bag3, 'k3', bag2, 'k') as joined2; --this will break without UDF signature and pig < 0.11
     describe data2;

     STORE data2 INTO 'output';

     */
    @Multiline
    private String bagJoinFullOuterTest;

    @Test
    public void bagJoinFullOuterTest() throws Exception {
        PigTest test = createPigTestFromString(bagJoinFullOuterTest);

        writeLinesToFile("input",
                "1\t{(K1,A1),(K2,B1),(K3,C1)}\t{(K1,A2),(K2,B2),(K2,B22)}\t{(K1,A3),(K3,C3),(K4,D3)}");

        try {
            test.runScript();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        List<Tuple> tuples = getLinesForAlias(test, "data2");
        assertEquals(tuples.size(), 1);
        Tuple tuple = tuples.get(0);
        DataBag joined1 = (DataBag)tuple.get(1);
        DataBag joined2 = (DataBag)tuple.get(2);
        
        String joined1Schema = "{(bag1::k: chararray,bag1::v: chararray,bag2::k: chararray,bag2::v: chararray,bag3::k3: chararray,bag3::v3: chararray)}";
        String joined2Schema = "{(bag1::k: chararray,bag1::v: chararray,bag3::k3: chararray,bag3::v3: chararray,bag2::k: chararray,bag2::v: chararray)}";
        String expectedJoined1 = "{(K1,A1,K1,A2,K1,A3),(K2,B1,K2,B2,,),(K2,B1,K2,B22,,),(K3,C1,,,K3,C3),(,,,,K4,D3)}";
        String expectedJoined2 = "{(K1,A1,K1,A3,K1,A2),(K2,B1,,,K2,B2),(K2,B1,,,K2,B22),(K3,C1,K3,C3,,),(,,K4,D3,,)}";
        
        // compare sorted bags because there is no guarantee on the order
        assertEquals(getSortedBag(joined1).toString(),getSortedBag(expectedJoined1, joined1Schema).toString());
        assertEquals(getSortedBag(joined2).toString(),getSortedBag(expectedJoined2, joined2Schema).toString());
    }
    
    private DataBag getSortedBag(String bagString, String schema) throws Exception {
        Utf8StorageConverter converter = new Utf8StorageConverter();
        ResourceFieldSchema parsedSchema = new ResourceFieldSchema(Utils.parseSchema("the_bag: " + schema).getField("the_bag"));
        DataBag bag = converter.bytesToBag(bagString.getBytes("UTF-8"), parsedSchema);
        return getSortedBag(bag);
    }
    
    private DataBag getSortedBag(DataBag bag) {
        DataBag sortedBag = new SortedDataBag(null);
        sortedBag.addAll(bag);
        return sortedBag;
    }
    
    /**


    define BagInnerJoin datafu.pig.bags.BagJoin();

    data = LOAD 'input' AS (outer_key:chararray, bag1:bag{T:tuple(k:chararray,v:chararray)}, bag2:bag{T:tuple(k:chararray,v:chararray)}, bag3:bag{T:tuple(k3:chararray,v3:chararray)});
    describe data;

    data2 = FOREACH data GENERATE
    outer_key,
    BagInnerJoin(bag1, 'k', bag2, 'k', bag3, 'k3') as joined1,
    BagInnerJoin(bag1, 'k', bag3, 'k3', bag2, 'k') as joined2; --this will break without UDF signature and pig < 0.11
    describe data2;

    STORE data2 INTO 'output';

    */
   @Multiline
   private String bagJoinInnerTest;

   @Test
   public void bagJoinInnerTest() throws Exception {
       PigTest test = createPigTestFromString(bagJoinInnerTest);

       writeLinesToFile("input",
               "1\t{(K1,A1),(K2,B1),(K3,C1)}\t{(K1,A2),(K2,B2),(K2,B22)}\t{(K1,A3),(K3,C3),(K4,D3)}");

       try {
           test.runScript();
       } catch (Exception e) {
           e.printStackTrace();
           throw e;
       }

       assertOutput(test, "data2",
               "(1,{(K1,A1,K1,A2,K1,A3)},{(K1,A1,K1,A3,K1,A2)})");
   }

  /**


  define BagUnion datafu.pig.bags.BagConcat();

  data = LOAD 'input' AS (input_bag: bag {T: tuple(inner_bag: bag {T2: tuple(k: int, v: chararray)})});
  describe data;

  data2 = FOREACH data GENERATE BagUnion(input_bag) as unioned;
  describe data2;

  STORE data INTO 'output';

   */
  @Multiline
  private String bagUnionTest;

  @Test
  public void bagUnionTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagUnionTest);
    writeLinesToFile("input", "({({(1,A),(1,B)}),({(2,A),(2,B),(2,C)}),({(3,A)})}");
    test.runScript();
    assertOutput(test, "data2", "({(1,A),(1,B),(2,A),(2,B),(2,C),(3,A)})");
  }

  /**


  define BagGroup datafu.pig.bags.BagGroup();

  data = LOAD 'input' AS (input_bag: bag {T: tuple(k: chararray, v: chararray)});
  describe data;

  data2 = FOREACH data GENERATE BagGroup(input_bag, input_bag.k) as grouped;
  describe data2;

  data3 = FOREACH data2 {
    ordered = ORDER grouped BY group;
    GENERATE
      ordered as grouped;
  }
  describe data3;

  STORE data INTO 'output';

   */
  @Multiline
  private String bagGroupSingleTest;

  @Test
  public void bagGroupSingleTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagGroupSingleTest);
    writeLinesToFile("input", "({(1,A),(1,B),(2,A),(2,B),(2,C),(3,A)})",
                     "({(A,1),(B,1),(A,2),(B,2),(C,2),(A,3)})");
    test.runScript();
    getLinesForAlias(test, "data2", true);
    assertOutput(test, "data3", "({(1,{(1,A),(1,B)}),(2,{(2,A),(2,B),(2,C)}),(3,{(3,A)})})",
                 "({(A,{(A,1),(A,2),(A,3)}),(B,{(B,1),(B,2)}),(C,{(C,2)})})");
  }

  /**


  define BagGroup datafu.pig.bags.BagGroup();

  data = LOAD 'input' AS (input_bag: bag {T: tuple(k: chararray, v: chararray)});
  describe data;

  data2 = FOREACH data GENERATE BagGroup(input_bag, input_bag.k) as grouped;
  describe data2;

  data3 = FOREACH data2 {
    ordered = ORDER grouped BY group;
    -- project only the value
    projected = FOREACH ordered GENERATE group, input_bag.(v);
    GENERATE
      projected as grouped;
  }
  describe data3;

  STORE data INTO 'output';

   */
  @Multiline
  private String bagGroupProjectTest;

  @Test(description="BagGroup with projection of the value from the bag")
  public void bagGroupProjectTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagGroupProjectTest);
    writeLinesToFile("input", "({(1,A),(1,B),(2,A),(2,B),(2,C),(3,A)})",
                     "({(A,1),(B,1),(A,2),(B,2),(C,2),(A,3)})");
    test.runScript();
    getLinesForAlias(test, "data2", true);
    assertOutput(test, "data3", "({(1,{(A),(B)}),(2,{(A),(B),(C)}),(3,{(A)})})",
                 "({(A,{(1),(2),(3)}),(B,{(1),(2)}),(C,{(2)})})");
  }

  /**


  define BagGroup datafu.pig.bags.BagGroup();

  data = LOAD 'input' AS (input_bag: bag {T: tuple(k: int, k2: chararray, v: int)});
  describe data;

  data2 = FOREACH data GENERATE BagGroup(input_bag, input_bag.(k, k2)) as grouped;
  describe data2;

  data3 = FOREACH data2 {
    ordered = ORDER grouped BY group;
    GENERATE
      ordered as grouped;
  }
  describe data3;

  STORE data INTO 'output';

   */
  @Multiline
  private String bagGroupMultipleTest;

  @Test
  public void bagGroupMultipleTest() throws Exception
  {
    PigTest test = createPigTestFromString(bagGroupMultipleTest);
    writeLinesToFile("input", "({(1,A,1),(1,B,1),(1,A,2),(2,A,1),(2,B,1),(2,C,1),(3,A,1)})");
    test.runScript();
    getLinesForAlias(test, "data2", true);
    assertOutput(test, "data3", "({((1,A),{(1,A,1),(1,A,2)}),((1,B),{(1,B,1)}),((2,A),{(2,A,1)}),((2,B),{(2,B,1)}),((2,C),{(2,C,1)}),((3,A),{(3,A,1)})})");
  }
}
