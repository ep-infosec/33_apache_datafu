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

/**
 *  Counts all the rows in a relation
 *
 *  relation - the relation to count
 */
DEFINE count_all_non_distinct(alias) returns res {
  grp_all = GROUP $alias ALL;
  $res = FOREACH grp_all GENERATE COUNT($alias);
};

/**
 *  Counts all the distinct keys in a relation
 *
 *  relation - the relation to count
 *  key - the field to check distinctness
 */
DEFINE count_distinct_keys(alias, key) returns res {
  just_key = FOREACH $alias GENERATE $key;
  dist_data = DISTINCT just_key;
  $res = count_all_non_distinct(dist_data);
};
