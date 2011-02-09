/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.jjs;

import com.google.gwt.dev.jjs.Correlation.Axis;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Tracks file and line information for AST nodes.
 */
public interface SourceInfo extends Serializable {

  /**
   * Add a Correlation to the SourceInfo.
   */
  void addCorrelation(Correlation c);

  /**
   * Copy any Correlations from another SourceInfo node if there are no
   * Correlations on this SourceInfo with the same Axis.
   */
  void copyMissingCorrelationsFrom(SourceInfo other);

  /**
   * Returns all Correlations applied to this SourceInfo, its parent, additional
   * ancestor SourceInfo, and any supertype SourceInfos.
   */
  List<Correlation> getAllCorrelations();

  /**
   * Returns all Correlations along a given axis applied to this SourceInfo, its
   * parent, additional ancestor SourceInfo, and any supertype SourceInfos.
   */
  List<Correlation> getAllCorrelations(Axis axis);

  /**
   * Returns the correlation factory that created this node.
   */
  CorrelationFactory getCorrelationFactory();
  
  int getEndPos();

  String getFileName();

  SourceOrigin getOrigin();

  /**
   * Returns the first Correlation that had been set with a given Axis, or
   * <code>null</code> if no Correlation has been set on the given axis.
   */
  Correlation getPrimaryCorrelation(Axis axis);

  /**
   * Returns the first Correlations added along each Axis on which a Correlation
   * has been set.
   */
  Set<Correlation> getPrimaryCorrelations();

  /**
   * Returns the first Correlations added along each Axis on which a Correlation
   * has been set. Some entries may be null and should be ignored. The returned
   * array must not be modified.
   */
  Correlation[] getPrimaryCorrelationsArray();

  int getStartLine();

  int getStartPos();

  /**
   * Create a child node of the same type and Origin as this node. If data
   * accumulation is enabled, the derived node will inherit its Correlations
   * from this node.
   */
  SourceInfo makeChild();

  /**
   * Create a child node of the same type as this node, but with a new Origin.
   * If data accumulation is enabled, the derived node will inherit its
   * Correlations from this node.
   */
  SourceInfo makeChild(SourceOrigin origin);

  /**
   * Add additional ancestor SourceInfos. These SourceInfo objects indicate that
   * a merge-type operation took place or that the additional ancestors have a
   * containment relationship with the SourceInfo.
   */
  void merge(SourceInfo... sourceInfos);
}
