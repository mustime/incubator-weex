cmake_minimum_required(VERSION 3.4.1)

set(JSON_LIBRARY_NAME json)

add_library(${JSON_LIBRARY_NAME} STATIC
  ./json_reader.cpp
  ./json_value.cpp
  ./json_writer.cpp
)

target_include_directories(${JSON_LIBRARY_NAME} PUBLIC .)
