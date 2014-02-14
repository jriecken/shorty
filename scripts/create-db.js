// Run this script against the "shorty" database
// $ mongo shorty create-db.js

// Drop the existing database
db.dropDatabase();
// Create the counter that is used to generate new short URLs
db.counters.insert({"_id": "urls", count: 0});
// Create indexes on the "urls" collection
db.urls.ensureIndex({"long_url" : 1});
