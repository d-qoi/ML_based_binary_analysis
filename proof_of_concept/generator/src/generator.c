#include "generator.h"

int DEBUG = 1; // May make this a config option later.

int generate_data(void* start_region, void* end_region, FILE *fp) {
  int range;
  int max_number;
  int number;
  DLL* current;
  DLL* previous;
  DLL* first;

  first = 0;
  current = 0;

  range = end_region - start_region;
  max_number = (range/sizeof(DLL))/PACKING_DENSITY;

  number = 0;
  while(number < MINIMUM_NUMBER) {
    number = (rand())%max_number + 1; // so as to have at least 1;
  }

  if (DEBUG) {
    printf("Max number: %d\n", max_number);
    printf("Min Number: %d\n", MINIMUM_NUMBER);
    printf("Number: %d\n", number);
  }

  fprintf(fp, "0x%lx, 0x%lx, ", (uint64_t)start_region, (uint64_t)end_region);
  fprintf(fp, "0x%x; ", number);

  for (int i = 0; i<number; i++) {
    current = start_region + rand()%range;
    if ((current->prev != 0) && (current->next != 0) && (current->value1 != 0) && (current->value2 != 0)) {
      i--;
      continue;
    }
    if (!first) {
      first = current;
    } else {
      current->prev = previous;
      previous->next = current;
    }

    current->value1 = rand();
    current->value2 = i;

    fprintf(fp, "0x%lx, ", (uint64_t)current);

    previous = current;
  }
  current->next = first;
  first->prev = current;
  fprintf(fp, "; ");
  return 1;
}


int main(int argv, char* argc[]) {
  char* output_file;
  //  char* output_file_desc;
  uint32_t size_of_memory = DEFAULT_SIZE;
  void* memory_region;
  void* end_of_memory_region;
  FILE* fp;
  struct timeval tv;

  gettimeofday(&tv, NULL);

  srand(tv.tv_sec*1000000 + tv.tv_usec);
  if (argv < 2) {
    printf("Usage: <output_file> [optional size (defaults to 8192 bytes)] [optional min-number\n");
    printf("Order of printing: start_of_region, end_of_region, number_of_nodes; comma_seperated_list_of_nodes; binary_dump\\n\n");
    return 1;
  }
  if (argv > 1) {
    output_file = argc[1];
  }
  if (argv > 2) {
    size_of_memory = atoi(argc[2]);
  }
  if (DEBUG)
  printf("Output file: %s\n Size: %d\n",output_file, size_of_memory);

  memory_region = malloc(size_of_memory);

  // propogating region
  // clear everything
  memset(memory_region, 0, size_of_memory);

  // to know if we need to regen random value.
  end_of_memory_region = memory_region + size_of_memory - (sizeof(DLL));

  if (DEBUG) {
    printf("Memory region: %p\n", memory_region);
    printf("End of memory: %p\n", end_of_memory_region);
  }

  fp = fopen(output_file, "w");
  if (!fp) {
    printf("Could not open file: %s\n", output_file);
    return 0;
  }
  if (generate_data(memory_region, end_of_memory_region, fp)) {
    //fprintf(fp, "\"");
    for (long i = 0; i<size_of_memory; i++) {
      fprintf(fp, "%x", ((char*)(memory_region))[i]);
    }
    //fprintf(fp, "\"");
    free(memory_region);
    return 0;
  }
  printf("Something went very wrong\n");
  free(memory_region);
  return 1;
}
