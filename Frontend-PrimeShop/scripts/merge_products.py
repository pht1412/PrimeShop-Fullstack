import os
import json
from pathlib import Path

def merge_products(category_path):
    # Get all brand directories
    brand_dirs = [d for d in os.listdir(category_path) if os.path.isdir(os.path.join(category_path, d))]
    
    for brand in brand_dirs:
        brand_path = os.path.join(category_path, brand)
        products = []
        
        # Get all JSON files except products.json
        json_files = [f for f in os.listdir(brand_path) if f.endswith('.json') and f != 'products.json']
        
        for json_file in json_files:
            file_path = os.path.join(brand_path, json_file)
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    product_data = json.load(f)
                    products.append(product_data)
            except Exception as e:
                print(f"Error reading {file_path}: {str(e)}")
                
        if products:
            # Write merged products to products.json
            output_path = os.path.join(brand_path, 'products.json')
            try:
                with open(output_path, 'w', encoding='utf-8') as f:
                    json.dump({"products": products}, f, ensure_ascii=False, indent=2)
                print(f"Successfully merged {len(products)} products for {brand}")
            except Exception as e:
                print(f"Error writing {output_path}: {str(e)}")

# Base path to product data
base_path = Path(r"d:\Code\DACS-main\DACS-main\FE-React\public\product_data\data")

# Categories to process
categories = ['dtdd', 'laptop', 'may-tinh-bang', 'dong-ho-thong-minh']

for category in categories:
    category_path = base_path / category
    if category_path.exists():
        print(f"\nProcessing category: {category}")
        merge_products(str(category_path))
    else:
        print(f"Category path not found: {category_path}")
