#!/bin/sh
# API test on News 
newman run postmanApi/collection/VERTICAL.json --folder NEWS -d postmanApi/data/news_data.json -e postmanApi/environment/VERTICAL_NIGHTLY.json
# API Test on Game 
newman run postmanApi/collection/VERTICAL.json --folder GAME -e postmanApi/environment/VERTICAL_NIGHTLY.json
# API Test on Travel
newman run postmanApi/collection/VERTICAL.json --folder TRAVEL -e postmanApi/environment/VERTICAL_NIGHTLY.json
# API Test on Shopping
newman run postmanApi/collection/VERTICAL.json --folder SHOPPING -e postmanApi/environment/VERTICAL_NIGHTLY.json