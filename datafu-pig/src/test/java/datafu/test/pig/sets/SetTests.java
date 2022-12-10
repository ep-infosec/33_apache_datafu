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

package datafu.test.pig.sets;

import java.util.Arrays;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.pig.data.BagFactory;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.pigunit.PigTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import datafu.pig.sets.SetIntersect;
import datafu.test.pig.PigTests;

public class SetTests extends PigTests
{
  /**
  

  define SetIntersect datafu.pig.sets.SetIntersect();
  
  data = LOAD 'input' AS (B1:bag{T:tuple(val1:int,val2:int)},B2:bag{T:tuple(val1:int,val2:int)});
  
  data2 = FOREACH data GENERATE SetIntersect(B1,B2);
  
  STORE data2 INTO 'output';
   */
  @Multiline
  private String setIntersectTest;
  
  @Test
  public void setIntersectTest() throws Exception
  {    
    PigTest test = createPigTestFromString(setIntersectTest);    
    
    writeLinesToFile("input", 
                     "{(1,10),(2,20),(3,30),(4,40),(5,50),(6,60)}\t{(0,0),(2,20),(4,40),(8,80)}",
                     "{(1,10),(1,10),(2,20),(3,30),(3,30),(4,40),(4,40)}\t{(1,10),(3,30)}");
                  
    test.runScript();
            
    assertOutput(test, "data2",
                 "({(2,20),(4,40)})",
                 "({(1,10),(3,30)})");
  }
  
  /**
  

  define SetIntersect datafu.pig.sets.SetIntersect();
  
  docs = LOAD 'docs' USING PigStorage(',') AS (id:int, line:chararray);
  B = FOREACH docs GENERATE id, line;
  C = FOREACH B GENERATE id, TOKENIZE(line) as gu;
  
  filtered = FOREACH C {
    uniq = DISTINCT gu;
    GENERATE id, uniq;
  }
    
  query = LOAD 'query' AS (line_query:chararray);
  bag_query = FOREACH query GENERATE TOKENIZE(line_query) AS query;
  -- sort the bag of tokens, since SetIntersect requires it
  bag_query = FOREACH bag_query {
    query_sorted = ORDER query BY token;
    GENERATE query_sorted;
  }
    
  result = FOREACH filtered {
    -- sort the tokens, since SetIntersect requires it
    tokens_sorted = ORDER uniq BY token;
    GENERATE id, 
             SIZE(SetIntersect(tokens_sorted,bag_query.query_sorted)) as cnt;
  }
    
  DUMP result;
  
   */
  @Multiline
  private String setIntersectTestExample;
  
  @Test
  public void setIntersectTestExample() throws Exception
  {    
    PigTest test = createPigTestFromString(setIntersectTestExample);    
    
    // document with words to check for
    writeLinesToFile("docs", 
                     "1,word1 word4 word2 word1",
                     "2,word2 word6 word1 word5 word3 word7",
                     "3,word1 word3 word4 word5");
    
    // query to apply to document
    writeLinesToFile("query", 
                     "word2 word7 word5");
                  
    test.runScript();
    
    assertOutput(test, "result",
                 "(1,1)",
                 "(2,3)",
                 "(3,1)");
  }
  
  @Test
  public void setIntersectEmptyBagsTest() throws Exception
  { 
    PigTest test = createPigTestFromString(setIntersectTest);    
    
    writeLinesToFile("input", 
                     "{(1,10),(2,20),(3,30),(4,40),(5,50),(6,60)}\t{(0,0),(2,20),(4,40),(8,80)}",
                     "{(1,10),(1,10),(2,20),(3,30),(3,30),(4,40),(4,40)}\t{(100,10),(300,30)}",
                     "{(1,10),(2,20)}\t{(1,10),(2,20)}",
                     "{(1,10),(2,20)}\t{}",
                     "{}\t{(1,10),(2,20)}",
                     "{}\t{}");
                  
    test.runScript();
            
    assertOutput(test, "data2",
                 "({(2,20),(4,40)})",
                 "({})",
                 "({(1,10),(2,20)})",
                 "({})",
                 "({})",
                 "({})");
  }
  
  @Test
  public void testIntersectWithNullTuples() throws Exception {
     DataBag one = BagFactory.getInstance().newDefaultBag();
     DataBag two = BagFactory.getInstance().newDefaultBag();

     Tuple input = TupleFactory.getInstance().newTuple(Arrays.asList(one, two));
     DataBag output = new SetIntersect().exec(input);
     Assert.assertEquals(0, output.size());
  }

  @Test(expectedExceptions=org.apache.pig.impl.logicalLayer.FrontendException.class)
  public void setIntersectOutOfOrderTest() throws Exception
  {
    PigTest test = createPigTestFromString(setIntersectTest);
    
    this.writeLinesToFile("input", 
                          "{(1,10),(3,30),(2,20),(4,40),(5,50),(6,60)}\t{(0,0),(2,20),(4,40),(8,80)}");
        
    test.runScript();
    
    this.getLinesForAlias(test, "data2");
  }
  
  /**
  

  define SetUnion datafu.pig.sets.SetUnion();
  
  data = LOAD 'input' AS (B1:bag{T:tuple(val1:int,val2:int)},B2:bag{T:tuple(val1:int,val2:int)});
  
  --dump data
  
  data2 = FOREACH data GENERATE SetUnion(B1,B2) AS C;
  data2 = FOREACH data2 {
    C = ORDER C BY val1 ASC, val2 ASC;
    generate C;
  }
  
  --dump data2
  
  STORE data2 INTO 'output';
   */
  @Multiline
  private String setUnionTest;
  
  @Test
  public void setUnionTest() throws Exception
  {    
    PigTest test = createPigTestFromString(setUnionTest);    
    
    writeLinesToFile("input", 
                     "{(1,10),(1,20),(1,30),(1,40),(1,50),(1,60),(1,80)}\t{(1,1),(1,20),(1,25),(1,25),(1,25),(1,40),(1,70),(1,80)}");
                  
    test.runScript();
            
    assertOutput(test, "data2",
                 "({(1,1),(1,10),(1,20),(1,25),(1,30),(1,40),(1,50),(1,60),(1,70),(1,80)})");
  }
  
  @Test
  public void setUnionEmptyBagsTest() throws Exception
  {
    PigTest test = createPigTestFromString(setUnionTest);    
    
    writeLinesToFile("input", 
                     "{(1,10),(1,20),(1,30),(1,40),(1,50),(1,60),(1,80)}\t{}",
                     "{}\t{(1,10),(1,20),(1,30),(1,40),(1,50)}",
                     "{}\t{}");
                  
    test.runScript();
            
    assertOutput(test, "data2",
                 "({(1,10),(1,20),(1,30),(1,40),(1,50),(1,60),(1,80)})",
                 "({(1,10),(1,20),(1,30),(1,40),(1,50)})",
                 "({})");
  }
  
  /**
  

  define SetDifference datafu.pig.sets.SetDifference();
  
  data = LOAD 'input' AS (B1:bag{T:tuple(val:int)},B2:bag{T:tuple(val:int)});
  
  data2 = FOREACH data GENERATE SetDifference(B1,B2);
  
  STORE data2 INTO 'output';
   */
  @Multiline
  private String setDifferenceTwoBagsTest;
  
  @Test
  public void setDifferenceTwoBagsTest() throws Exception
  {    
    PigTest test = createPigTestFromString(setDifferenceTwoBagsTest);    
    
    writeLinesToFile("input", 
                     "{(1),(2),(3)}\t",
                     "{(1),(2),(3)}\t{}",
                     "\t{(1),(2),(3)}",
                     "{}\t{(1),(2),(3)}",
                     "{(1),(2),(3)}\t{(1)}",
                     "{(1),(2),(3)}\t{(1),(2)}",
                     "{(1),(2),(3)}\t{(1),(2),(3)}",
                     "{(1),(2),(3)}\t{(1),(2),(3),(4)}",
                     "{(1),(2),(3),(4),(5),(6)}\t{(3),(4)}");
                  
    test.runScript();
            
    assertOutput(test, "data2",
                 "({(1),(2),(3)})",
                 "({(1),(2),(3)})",
                 "({})",
                 "({})",
                 "({(2),(3)})",
                 "({(3)})",
                 "({})",
                 "({})",
                 "({(1),(2),(5),(6)})");
  }
  
  /**
  

  define SetDifference datafu.pig.sets.SetDifference();
  
  data = LOAD 'input' AS (B1:bag{T:tuple(val:int)},B2:bag{T:tuple(val:int)},B3:bag{T:tuple(val:int)});
  
  data2 = FOREACH data GENERATE SetDifference(B1,B2,B3);
  
  STORE data2 INTO 'output';
   */
  @Multiline
  private String setDifferenceThreeBagsTest;
  
  @Test
  public void setDifferenceThreeBagsTest() throws Exception
  {    
    PigTest test = createPigTestFromString(setDifferenceThreeBagsTest);    
    
    writeLinesToFile("input", 
                     "{(1),(2),(3)}\t\t",
                     "{(1),(2),(3)}\t{}\t",
                     "{(1),(2),(3)}\t\t{}",
                     "{(1),(2),(3)}\t{}\t{}",
                     "{(1),(2),(2),(2),(3),(3)}\t{}\t{}",
                     
                     "{(1),(2),(3)}\t{(2)}\t{}",
                     "{(1),(2),(3)}\t{}\t{(2)}",
                     
                     "{(1),(2),(3)}\t{(2)}\t{(3)}",
                     
                     "{(1),(2),(3)}\t{(2),(2)}\t{(3),(3)}",
                     "{(1),(1),(1),(2),(2),(3),(3),(3)}\t{(2),(2)}\t{(3),(3)}",
                     "{(1),(2),(3)}\t{(0),(2)}\t{(3)}",
                     
                     "{(1),(2),(3)}\t{(0),(2)}\t{(1),(3)}",
                     "{(1),(2),(3)}\t{(0),(2)}\t{(1),(3),(4)}");
                  
    test.runScript();
            
    assertOutput(test, "data2",
                 "({(1),(2),(3)})",
                 "({(1),(2),(3)})",
                 "({(1),(2),(3)})",
                 "({(1),(2),(3)})",
                 "({(1),(2),(3)})",

                 "({(1),(3)})",
                 "({(1),(3)})",
                 
                 "({(1)})",
                 
                 "({(1)})",
                 "({(1)})",
                 "({(1)})",
                 
                 "({})",
                 "({})");
  }
}
