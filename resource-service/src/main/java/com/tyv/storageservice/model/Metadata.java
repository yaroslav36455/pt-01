package com.tyv.storageservice.model;

import com.tyv.storageservice.enums.Bucket;
import com.tyv.storageservice.enums.Category;

public record Metadata(
   Bucket bucket,
   Category category
) {}
