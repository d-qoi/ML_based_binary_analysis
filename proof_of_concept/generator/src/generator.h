#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>

#define DEFAULT_SIZE (8192)
// in percentage, it will divide by this number, so 10 is 10%, 100 is 1%
#define PACKING_DENSITY (10)
#define MINIMUM_NUMBER (10)

typedef struct DLL {
  void* prev;
  void* next;
  void* value1;
  void* value2;
} DLL;


int generate_data(void* start_region, void* end_region, FILE* fp);
