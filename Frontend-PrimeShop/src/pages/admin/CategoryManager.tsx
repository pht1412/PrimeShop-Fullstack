import React, { useEffect, useState } from 'react';
import '../../assets/css/admin.css';
import api from '../../api/api';
import { Category } from '../../types/category';
import Swal from 'sweetalert2';

const CategoryManager: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [newCategory, setNewCategory] = useState<string>('');
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);

  useEffect(() => {
    const fetchCategories = async () => {
      const response = await api.get('/admin/category/all');
      setCategories(response.data);
    };
    fetchCategories();
    console.log(categories);
  }, []);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await api.post('/admin/category/add', { name: newCategory , specs: []});
      Swal.fire({
        title: 'Thêm danh mục thành công',
        icon: 'success',
        confirmButtonText: 'OK',
      }).then(() => {
        window.location.reload();
      });
    } catch (error) {
      console.error("Lỗi khi thêm danh mục:", error);
    }
  };

  const handleEditCategory = async (currentSlug: string, newName: string) => {
    Swal.fire({
      title: 'Sửa danh mục',
      width: '1200px',
      showCancelButton: true,
      confirmButtonText: 'Cập nhật',
      showLoaderOnConfirm: true,
      html: `
      <div class="form-group" style="width: 500px, align-items: flex-start;">
      <p><strong>Tên danh mục:</strong><input id="name" class="swal2-input" placeholder="Tên danh mục" value="${newName}"></p>
      </div>
      `,
      preConfirm: () => {
        const name = document.getElementById('name')?.value;
        try {
          api.post(`/admin/category/update`, { slug: currentSlug, name: name });
          Swal.fire({
            title: 'Cập nhật danh mục thành công',
            icon: 'success',
            confirmButtonText: 'OK',
          }).then(() => {
            window.location.reload();
          });
        } catch (error) {
          console.error("Lỗi khi cập nhật danh mục:", error);
        }
      }
    });
  };

  const handleUpdateCategory = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingCategory) {
      setCategories(categories.map((cat) => (cat.id === editingCategory.id ? editingCategory : cat)));
      setEditingCategory(null);
    }
  };

  const handleDeleteCategory = (id: number) => {
    setCategories(categories.filter((cat) => cat.id !== id));
  };

  return (
    <div className="admin-form">
      <h1>Quản lý danh mục</h1>
      <button
        onClick={() => setShowAddForm(!showAddForm)}
        className={`btn ${showAddForm ? 'btn-secondary' : 'btn-success'}`}
      >
        {showAddForm ? 'Hủy thêm' : 'Thêm danh mục'}
      </button>

      {/* Form thêm/chỉnh sửa danh mục */}
      {showAddForm && (
        <div className="admin-form">
          <form onSubmit={handleAddCategory}>
            <div className="form-group">
              <label>Tên danh mục:</label>
              <input
                type="text"
                value={newCategory}
                onChange={(e) =>
                  setNewCategory(e.target.value)
                }
              required
            />
          </div>
          <div className="flex justify-start">
            <button type="submit" className="btn btn-primary">
              {editingCategory ? 'Cập nhật' : 'Thêm danh mục'}
            </button>
          </div>
          {editingCategory && (
            <button
              type="button"
              className="admin-btn admin-btn-secondary"
              onClick={() => setEditingCategory(null)}
            >
              Hủy
            </button>
          )}
        </form>
      </div>
      )}

      {/* Danh sách danh mục */}
      <div className="table-container">
        {categories.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Tên</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((category) => (
                <tr key={category.id}>
                  <td>{category.id}</td>
                  <td>{category.name}</td>
                  <td>
                    <button
                      className="btn btn-primary btn-small"
                      onClick={() => handleEditCategory(category.slug, category.name)}
                    >
                      Sửa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>Chưa có danh mục nào.</p>
        )}
      </div>
    </div>
  );
};

export default CategoryManager;