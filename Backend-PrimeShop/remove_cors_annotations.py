#!/usr/bin/env python3
"""
Script để xóa tất cả @CrossOrigin annotations từ các file Java
Chạy script này để sử dụng cấu hình CORS toàn cục thay vì annotations riêng lẻ
"""

import os
import re
import glob

def remove_cross_origin_annotations(file_path):
    """Xóa @CrossOrigin annotations từ file Java"""
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        # Pattern để tìm và xóa @CrossOrigin annotations
        # Bao gồm cả trường hợp có allowCredentials
        patterns = [
            r'@CrossOrigin\(origins\s*=\s*"[^"]*",\s*allowCredentials\s*=\s*(true|false)\)\s*\n?',
            r'@CrossOrigin\(origins\s*=\s*"[^"]*"\)\s*\n?',
            r'@CrossOrigin\(origins\s*=\s*\*\)\s*\n?',
            r'@CrossOrigin\s*\n?'
        ]
        
        original_content = content
        for pattern in patterns:
            content = re.sub(pattern, '', content, flags=re.MULTILINE)
        
        # Nếu có thay đổi, ghi lại file
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as file:
                file.write(content)
            print(f"✅ Đã xóa @CrossOrigin từ: {file_path}")
            return True
        else:
            print(f"ℹ️  Không có @CrossOrigin trong: {file_path}")
            return False
            
    except Exception as e:
        print(f"❌ Lỗi khi xử lý file {file_path}: {e}")
        return False

def main():
    """Main function"""
    print("🔄 Bắt đầu xóa @CrossOrigin annotations...")
    
    # Tìm tất cả file Java trong thư mục src
    java_files = glob.glob("src/main/java/com/primeshop/**/*.java", recursive=True)
    
    if not java_files:
        print("❌ Không tìm thấy file Java nào!")
        return
    
    print(f"📁 Tìm thấy {len(java_files)} file Java")
    
    removed_count = 0
    for file_path in java_files:
        if remove_cross_origin_annotations(file_path):
            removed_count += 1
    
    print(f"\n🎉 Hoàn thành! Đã xóa @CrossOrigin từ {removed_count}/{len(java_files)} file")
    print("💡 Bây giờ tất cả API sẽ sử dụng cấu hình CORS toàn cục từ CorsConfig.java")

if __name__ == "__main__":
    main() 