cmake_minimum_required(VERSION 3.5)
project (Generator)
subdirs(src)
# Setting up variables
set (Generator_VERSION_MAJOR 0)
set (Generator_VERSION_MINOR 1)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
