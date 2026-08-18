import React, { useEffect, useState } from 'react';
import '../../assets/css/admin.css';
import api from '../../api/api';
import { ImportTransaction } from '../../types/import-transaction';
import Swal from 'sweetalert2';

const ImportTransactionManager: React.FC = () => {
  const [importTransactions, setImportTransactions] = useState<ImportTransaction[]>([]);
  const [newImportTransaction, setNewImportTransaction] = useState<string>('');
  const [editingImportTransaction, setEditingImportTransaction] = useState<ImportTransaction | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);

  useEffect(() => {
    const fetchImportTransactions = async () => {
      const response = await api.get('/import-transaction/all');
      setImportTransactions(response.data);
    };
    fetchImportTransactions();
    console.log(importTransactions);
  }, []);

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await api.post('/admin/import-transaction/add', { productId: newImportTransaction , specs: []});
      Swal.fire({
        title: 'Thêm giao dịch nhập hàng thành công',
        icon: 'success',
        confirmButtonText: 'OK',
      }).then(() => {
        window.location.reload();
      });
    } catch (error) {
      console.error("Lỗi khi thêm giao dịch nhập hàng:", error);
    }
  };

  const handleEditCategory = async (currentSlug: string, newName: string) => {
    Swal.fire({
      title: 'Sửa giao dịch nhập hàng',
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
            title: 'Cập nhật giao dịch nhập hàng thành công',
            icon: 'success',
            confirmButtonText: 'OK',
          }).then(() => {
            window.location.reload();
          });
        } catch (error) {
          console.error("Lỗi khi cập nhật giao dịch nhập hàng:", error);
        }
      }
    });
  };

  const handleUpdateCategory = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingImportTransaction) {
      setImportTransactions(importTransactions.map((rev) => (rev.id === editingImportTransaction.id ? editingImportTransaction : rev)));
      setEditingImportTransaction(null);
    }
  };

  const handleDeleteCategory = (id: number) => {
    setImportTransactions(importTransactions.filter((rev) => rev.id !== id));
  };

  return (
    <div className="admin-form">
      <h1>Quản lý giao dịch nhập hàng</h1>
      <button
        onClick={() => setShowAddForm(!showAddForm)}
        className={`btn ${showAddForm ? 'btn-secondary' : 'btn-success'}`}
      >
        {showAddForm ? 'Hủy thêm' : 'Thêm giao dịch nhập hàng'}
      </button>

      {/* Form thêm/chỉnh sửa danh mục */}
      {showAddForm && (
        <div className="admin-form">
          <form onSubmit={handleAddCategory}>
            <div className="form-group">
                <label>Tên sản phẩm:</label>
              <input
                type="text"
                value={newImportTransaction}
                onChange={(e) =>
                  setNewImportTransaction(e.target.value)
                }
              required
            />
          </div>
          <div className="flex justify-start">
            <button type="submit" className="btn btn-primary">
              {editingImportTransaction ? 'Cập nhật' : 'Thêm giao dịch nhập hàng'}
            </button>
          </div>
          {editingImportTransaction && (
            <button
              type="button"
              className="admin-btn admin-btn-secondary"
              onClick={() => setEditingImportTransaction(null)}
            >
              Hủy
            </button>
          )}
        </form>
      </div>
      )}

      {/* Danh sách danh mục */}
      <div className="table-container">
        {importTransactions.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>Mã sản phẩm</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Tổng tiền</th>
                <th>Ngày nhập</th>
                <th>Nhà cung cấp</th>
                <th>Ghi chú</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {importTransactions.map((importTransaction) => (
                <tr key={importTransaction.id}>
                  <td>{importTransaction.productId}</td>
                  <td>{importTransaction.quantity}</td>
                  <td>{importTransaction.unitCost.toLocaleString('vi-VN')}</td>
                  <td>{(importTransaction.quantity * importTransaction.unitCost).toLocaleString('vi-VN')}</td>
                  <td>{importTransaction.importDate}</td>
                  <td>{importTransaction.supplierName}</td>
                  <td>{importTransaction.notes}</td>
                  <td>
                    <button
                      className="btn btn-primary btn-small"
                      onClick={() => handleEditImportTransaction(importTransaction.invoiceNumber, importTransaction.productId)}
                    >
                      Xem chi tiết
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>Chưa có giao dịch nhập hàng nào.</p>
        )}
      </div>
    </div>
  );
};

export default ImportTransactionManager;