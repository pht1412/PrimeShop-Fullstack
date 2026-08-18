import React, { useEffect, useState } from 'react';
import '../../assets/css/admin.css';
import api from '../../api/api';
import { Revenue } from '../../types/revenue';
import Swal from 'sweetalert2';

const CategoryManager: React.FC = () => {
  const [revenues, setRevenues] = useState<Revenue[]>([]);
  const [newRevenue, setNewRevenue] = useState<string>('');
  const [editingRevenue, setEditingRevenue] = useState<Revenue | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);

  useEffect(() => {
    const fetchRevenues = async () => {
      const response = await api.get('/revenue/all');
      setRevenues(response.data);
    };
    fetchRevenues();
    console.log(revenues);
  }, []);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await api.post('/admin/revenue/add', { name: newRevenue , specs: []});
      Swal.fire({
        title: 'Thêm danh mục thành công',
        icon: 'success',
        confirmButtonText: 'OK',
      }).then(() => {
        window.location.reload();
      });
    } catch (error) {
      console.error("Lỗi khi thêm doanh thu:", error);
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
    if (editingRevenue) {
      setRevenues(revenues.map((rev) => (rev.id === editingRevenue.id ? editingRevenue : rev)));
      setEditingRevenue(null);
    }
  };

  const handleDeleteCategory = (id: number) => {
    setRevenues(revenues.filter((rev) => rev.id !== id));
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
                value={newRevenue}
                onChange={(e) =>
                  setNewRevenue(e.target.value)
                }
              required
            />
          </div>
          <div className="flex justify-start">
            <button type="submit" className="btn btn-primary">
              {editingRevenue ? 'Cập nhật' : 'Thêm danh mục'}
            </button>
          </div>
          {editingRevenue && (
            <button
              type="button"
              className="admin-btn admin-btn-secondary"
              onClick={() => setEditingRevenue(null)}
            >
              Hủy
            </button>
          )}
        </form>
      </div>
      )}

      {/* Danh sách danh mục */}
      <div className="table-container">
        {revenues.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Năm-Tháng</th>
                <th>Số đơn hàng bán ra</th>
                <th>Doanh thu</th>
                <th>Lợi nhuận</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {revenues.map((revenue) => (
                <tr key={revenue.id}>
                  <td>{revenue.id}</td>
                  <td>{revenue.period}</td>
                  <td>{revenue.orderCount}</td>
                  <td>{revenue.totalRevenue.toLocaleString('vi-VN')}</td>
                  <td>{revenue.totalProfit.toLocaleString('vi-VN')}</td>
                  <td>
                    <button
                      className="btn btn-primary btn-small"
                      onClick={() => handleEditRevenue(revenue.id, revenue.name)}
                    >
                      Xem chi tiết
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>Chưa có doanh thu nào.</p>
        )}
      </div>
    </div>
  );
};

export default CategoryManager;