#include "generator.h"

int generate_data(void* start_region, void* end_region, const char* desc_file_name) {
  int range;
  int max_number;
  int number;
  DLL* current;
  DLL* previous;
  DLL* first;
  FILE *fp;

  fp = fopen(desc_file_name, "w");
  if (!fp) {
#ifdef DEBUG
    printf("Could not open file: %s\n", desc_file_name);
#endif
    return 0;
  }

  first = 0;
  current = 0;

  range = end_region - start_region;
  max_number = (range/sizeof(DLL))/PACKING_DENSITY;

  number = rand()%max_number + 1; // so as to have at least 1;

#ifdef DEBUG
  printf("Max number: %d\n", max_number);
  printf("Number: %d\n", number);
#endif
  fprintf(fp, "Region:\n0x%lux\n0x%lux\n\n", (uint64_t)start_region, (uint64_t)end_region);
  fprintf(fp, "Number of nodes:\n%d\nNodes:\n", number);

  for (int i = 0; i<number; i++) {
    current = start_region + rand()%range;
    if ((current->prev != 0) && (current->next != 0) && (current->value != 0)) {
      i--;
      continue;
    }
    if (!first) {
      first = current;
    } else {
      current->prev = previous;
      previous->next = current;
    }

#ifdef DEBUG
    current->value = (void*)((uint64_t)i);
#else
    current->value = rand();
#endif

    fprintf(fp, "0x%lux\n", (uint64_t)current);

    previous = current;
  }
  current->next = first;
  first->prev = current;
  fclose(fp);
  return 1;
}

int dump_to_file(void* start_of_region, uint32_t number_of_bytes, const char* file_name) {
  FILE *fp;
  uint8_t* memory = start_of_region;
  fp = fopen(file_name, "w");
  if (!fp) {
#ifdef DEBUG
    printf("Could not open %s\n", file_name);
#endif
    return 0;
  }
  fwrite(start_of_region, 1, number_of_bytes, fp);
  return 1;
  fclose(fp);
}

int main(int argv, char* argc[]) {
  char* output_file;
  char* output_file_desc;
  uint32_t size_of_memory = DEFAULT_SIZE;
  void* memory_region;
  void* end_of_memory_region;
  if (argv < 2) {
    printf("Usage: <output_file> [optional size (defaults to 8192 bytes)]\n");
    return 1;
  }
  if (argv > 1) {
    output_file = argc[1];
  }
  if (argv > 2) {
    size_of_memory = atoi(argc[2]);
  }
#ifdef DEBUG
  printf("Output file: %s\n Size: %d\n",output_file, size_of_memory);
#endif
  {
    int len = strlen(output_file);
    output_file_desc = malloc(sizeof(char)*(len+6));
    strcpy(output_file_desc, output_file);
    strcat(output_file_desc, ".desc");
#ifdef DEBUG
    printf("Output Description File: %s\n",output_file_desc);
#endif
  }

  memory_region = malloc(size_of_memory);

  // propogating region
  // clear everything
  memset(memory_region, 0, size_of_memory);

  // to know if we need to regen random value.
  end_of_memory_region = memory_region + size_of_memory - (sizeof(DLL));

#ifdef DEBUG
  printf("Memory region: %p\n", memory_region);
  printf("End of memory: %p\n", end_of_memory_region);
#endif

  if (generate_data(memory_region, end_of_memory_region, output_file_desc))
    if (dump_to_file(memory_region, size_of_memory, output_file)) {
      free(output_file_desc);
      free(memory_region);
      return 0;
    }
  free(output_file_desc);
  free(memory_region);
  return 1;
}
