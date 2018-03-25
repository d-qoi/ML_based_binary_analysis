#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#define DEFAULT_SIZE (8192)
// in percentage, it will divide by this number, so 10 is 10%, 100 is 1%
#define PACKING_DENSITY (10)


#define DEBUG

typedef struct DLL {
  void* prev;
  void* next;
  void* value;
} DLL;


int generate_data(void* start_region, void* end_region, const char* desc_file_name);
int dump_to_file(void* start_region, uint32_t number_of_bytes, const char* file_name);
