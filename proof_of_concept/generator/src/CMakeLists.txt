cmake_minimum_required(VERSION 3.5)

include_directories(${CMAKE_SOURCE_DIR}/src)
add_executable(Generator generator.c)
