#!/bin/sh
# API test on News 
newman run integrationTest/postmanApi/collection/VERTICAL.json --folder NEWS -d integrationTest/postmanApi/data/news_data.json -e integrationTest/postmanApi/environment/VERTICAL_NIGHTLY.json
# API Test on Game 
newman run integrationTest/postmanApi/collection/VERTICAL.json --folder GAME -e integrationTest/postmanApi/environment/VERTICAL_NIGHTLY.json
# API Test on Travel
newman run integrationTest/postmanApi/collection/VERTICAL.json --folder TRAVEL -e integrationTest/postmanApi/environment/VERTICAL_NIGHTLY.json
# API Test on Shopping
newman run integrationTest/postmanApi/collection/VERTICAL.json --folder SHOPPING -e integrationTest/postmanApi/environment/VERTICAL_NIGHTLY.json