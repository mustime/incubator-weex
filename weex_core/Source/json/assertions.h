/**
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
// Copyright 2007-2010 Baptiste Lepilleur
// Distributed under MIT license, or public domain if desired and
// recognized in your jurisdiction.
// See file LICENSE for detail or copy at http://jsoncpp.sourceforge.net/LICENSE

#ifndef CPPTL_JSON_ASSERTIONS_H_INCLUDED
#define CPPTL_JSON_ASSERTIONS_H_INCLUDED

#include <stdlib.h>
#include <sstream>

#if !defined(JSON_IS_AMALGAMATION)
#include "config.h"
#endif // if !defined(JSON_IS_AMALGAMATION)

/** It should not be possible for a maliciously designed file to
 *  cause an abort() or seg-fault, so these macros are used only
 *  for pre-condition violations and internal logic errors.
 */
#if JSON_USE_EXCEPTION

// @todo <= add detail about condition in exception
# define JSON_ASSERT(condition)                                                \
  {if (!(condition)) {Json::throwLogicError( "assert json failed" );}}

# define JSON_FAIL_MESSAGE(message)                                            \
  {                                                                            \
    std::ostringstream oss; oss << message;                                    \
    Json::throwLogicError(oss.str());                                          \
    abort();                                                                   \
  }

#else // JSON_USE_EXCEPTION

# define JSON_ASSERT(condition) assert(condition)

// The call to assert() will show the failure message in debug builds. In
// release builds we abort, for a core-dump or debugger.
# define JSON_FAIL_MESSAGE(message)                                            \
  {                                                                            \
    std::ostringstream oss; oss << message;                                    \
    assert(false && oss.str().c_str());                                        \
    abort();                                                                   \
  }


#endif

#define JSON_ASSERT_MESSAGE(condition, message)                                \
  if (!(condition)) {                                                          \
    JSON_FAIL_MESSAGE(message);                                                \
  }

#endif // CPPTL_JSON_ASSERTIONS_H_INCLUDED
