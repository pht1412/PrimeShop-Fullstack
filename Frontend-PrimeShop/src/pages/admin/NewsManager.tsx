import React, { useEffect, useState } from 'react';
import '../../assets/css/admin.css';
import api from '../../api/api';
import { News } from '../../types/news';
import Swal from 'sweetalert2';

const NewsPage: React.FC = () => {
  const [newsList, setNewsList] = useState<News[]>([]);
  const [newNews, setNewNews] = useState({
    title: '',
    excerpt: '',
    image_url: '',
    published_at: new Date().toISOString(),
    slug: '',
    status: 'DRAFT' as 'PUBLISHED' | 'DRAFT',
    category_id: 1,
    text_url: '',
  });
  const [editingNews, setEditingNews] = useState<News | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);

  const fetchNews = async () => {
    const response = await api.get('/news');
    setNewsList(response.data.content);
  };

  useEffect(() => {
    fetchNews();
    console.log(newsList);
    console.log(newsList.length);
  }, []);

  // Hàm tạo slug từ title
  const generateSlug = (title: string) => {
    return title
      .toLowerCase()
      .normalize('NFD') // Phân tách dấu tiếng Việt
      .replace(/[\u0300-\u036f]/g, '') // Loại bỏ dấu
      .replace(/[^a-z0-9\s-]/g, '') // Loại bỏ ký tự đặc biệt
      .trim()
      .replace(/\s+/g, '-') // Thay khoảng trắng bằng dấu gạch ngang
      .replace(/-+/g, '-'); // Loại bỏ nhiều dấu gạch ngang liên tiếp
  };

  const handleAddNews = (e: React.FormEvent) => {
    e.preventDefault();
    const id = newsList.length > 0 ? Math.max(...newsList.map((n) => n.id)) + 1 : 1;
    const now = new Date().toISOString();
    setNewsList([
      ...newsList,
      {
        ...newNews,
        id,
        created_at: now,
        updated_at: now,
        view_count: 0,
        author_id: null,
      },
    ]);
    setNewNews({
      title: '',
      excerpt: '',
      image_url: '',
      published_at: new Date().toISOString(),
      slug: '',
      status: 'DRAFT',
      category_id: 1,
      text_url: '',
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await api.post('/news', newNews);
      console.log(response);
      Swal.fire({
        title: 'Thêm tin tức thành công',
        icon: 'success',
        confirmButtonText: 'OK',
        allowOutsideClick: false,
        allowEscapeKey: false,
        allowEnterKey: false,
      }).then(() => {
        window.location.reload();
      });
    } catch (error) {
      console.error('Error submitting news:', error);
    }
  }; 
  

  const handleEditNews = (news: News) => {
    Swal.fire({
      title: 'Sửa tin tức',
      width: '1200px',
      showCancelButton: true,
      confirmButtonText: 'Cập nhật',
      showLoaderOnConfirm: true,
      html: `
      <div class="form-group" style="width: 500px, align-items: flex-start;">
      <p><strong>Tên tin tức:</strong><input id="title" class="swal2-input" placeholder="Tên tin tức" value="${news.title}"></p>
      <p><strong>Mô tả ngắn:</strong><input id="excerpt" class="swal2-input" type="text" placeholder="Mô tả ngắn" value="${news.excerpt}"></p>
      <p><strong>URL hình ảnh:</strong><input id="imageUrl" class="swal2-input" type="text" placeholder="URL hình ảnh" value="${news.imageUrl}"></p>
      <p><strong>URL nội dung chi tiết:</strong><input id="textUrl" class="swal2-input" type="text" placeholder="URL nội dung chi tiết" value="${news.textUrl}"></p>
      `,
      preConfirm: async () => {
        const title = document.getElementById('title')?.value;
        const excerpt = document.getElementById('excerpt')?.value;
        const imageUrl = document.getElementById('imageUrl')?.value;
        const status = "PUBLISHED";
        const categoryId = 1;
        const textUrl = document.getElementById('textUrl')?.value;

        

        try {
          const response = await api.put(`/news/${news.id}`, {
            title, excerpt, imageUrl, status, categoryId, textUrl
          });
          console.log("response", response);
        } catch (error) {
          console.error("Lỗi khi cập nhật tin tức:", error);
        } 
      },
      allowOutsideClick: () => !Swal.isLoading()
    }).then(result => {
      if (result.isConfirmed) {
        Swal.fire({
          title: 'Cập nhật tin tức thành công',
          icon: 'success',
          confirmButtonText: 'OK',
          allowOutsideClick: false,
          allowEscapeKey: false,
          allowEnterKey: false,
        }).then(() => {
          window.location.reload();
        });
        console.log(result.value);
      }
    });
  };

  const handleDeleteNews = async (id: number) => {
    Swal.fire({
      title: 'Xóa tin tức',
      text: 'Bạn có chắc chắn muốn xóa tin tức này?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Xóa',
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          const response = await api.delete(`/news/${id}`);
          console.log("response", response);
          Swal.fire({
            title: 'Xóa tin tức thành công',
            icon: 'success',
            confirmButtonText: 'OK',
          }).then(() => {
            window.location.reload();
          });
        } catch (error) {
          console.error("Lỗi khi xóa tin tức:", error);
        }
      }
    });
  };

  const handleUpdateNews = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingNews) {
      setNewsList(newsList.map((item) => (item.id === editingNews.id ? { ...editingNews, updated_at: new Date().toISOString() } : item)));
      setEditingNews(null);
    }
  };

  // const handleDeleteNews = (id: number) => {
  //   setNewsList(newsList.filter((item) => item.id !== id));
  // };

  // Danh sách danh mục giả lập
  const categories = [
    { id: 1, name: 'Công nghệ' },
    { id: 2, name: 'Điện thoại' },
    { id: 3, name: 'Máy tính' },
  ];

  return (
    <div>
      <h1>Quản lý tin tức</h1>
      <button
        onClick={() => setShowAddForm(!showAddForm)}
        className={`btn ${showAddForm ? 'btn-secondary' : 'btn-success'}`}
      >
        {showAddForm ? 'Hủy thêm' : 'Thêm tin tức'}
      </button>

      {/* Form thêm/chỉnh sửa tin tức */}
      {showAddForm && (
      <div className="admin-form">        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tiêu đề:</label>
            <input
              type="text"
              value={editingNews ? editingNews.title : newNews.title}
              onChange={(e) => {
                const title = e.target.value;
                editingNews
                  ? setEditingNews({
                      ...editingNews,
                      title,
                      slug: editingNews.slug || generateSlug(title), // Giữ slug nếu đã nhập tay, ngược lại sinh mới
                    })
                  : setNewNews({
                      ...newNews,
                      title,
                      slug: newNews.slug || generateSlug(title), // Giữ slug nếu đã nhập tay, ngược lại sinh mới
                    });
              }}
              required
            />
          </div>
          <div className="form-group">
            <label>Mô tả ngắn:</label>
            <textarea
              value={editingNews ? editingNews.excerpt : newNews.excerpt}
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, excerpt: e.target.value })
                  : setNewNews({ ...newNews, excerpt: e.target.value })
              }
              required
            />
          </div>
          <div className="form-group">
            <label>URL hình ảnh:</label>
            <input
              type="text"
              value={editingNews ? editingNews.imageUrl : newNews.imageUrl}
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, imageUrl: e.target.value })
                  : setNewNews({ ...newNews, imageUrl: e.target.value })
              }
              required
            />
          </div>
          <div className="form-group">
            <label>Ngày xuất bản:</label>
            <input
              type="datetime-local"
              value={
                editingNews
                  ? new Date(editingNews.published_at).toISOString().slice(0, 16)
                  : newNews.published_at.slice(0, 16)
              }
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, published_at: new Date(e.target.value).toISOString() })
                  : setNewNews({ ...newNews, published_at: new Date(e.target.value).toISOString() })
              }
              required
            />
          </div>
          <div className="form-group">
            <label>Slug:</label>
            <input
              type="text"
              value={editingNews ? editingNews.slug : newNews.slug}
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, slug: e.target.value })
                  : setNewNews({ ...newNews, slug: e.target.value })
              }
              placeholder="Tự động sinh từ tiêu đề (có thể chỉnh sửa)"
              readOnly
            />
          </div>
          <div className="form-group">
            <label>Trạng thái:</label>
            <select
              value={editingNews ? editingNews.status : newNews.status}
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, status: e.target.value as 'PUBLISHED' | 'DRAFT' })
                  : setNewNews({ ...newNews, status: e.target.value as 'PUBLISHED' | 'DRAFT' })
              }
            >
              <option value="DRAFT">Bản nháp</option>
              <option value="PUBLISHED">Đã xuất bản</option>
            </select>
          </div>
          {/* <div className="form-group">
            <label>Danh mục:</label>
            <select
              value={1}
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, categoryId: 1 })
                  : setNewNews({ ...newNews, category_id: 1 })
              }
            >
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
              readOnly
            </select>
          </div> */}
          <input type="hidden" name="category_id" value={1} />
          <div className="form-group">
            <label>URL nội dung chi tiết:</label>
            <input
              type="text"
              value={editingNews ? editingNews.textUrl : newNews.textUrl}
              onChange={(e) =>
                editingNews
                  ? setEditingNews({ ...editingNews, textUrl: e.target.value })
                  : setNewNews({ ...newNews, textUrl: e.target.value })
              }
              required
            />
          </div>
          <div className="flex justify-start">
            <button type="submit" className="btn btn-primary">
              {editingNews ? 'Cập nhật' : 'Thêm tin tức'}
            </button>
          </div>
          {editingNews && (
            <button
              type="button"
              className="admin-btn admin-btn-secondary"
              onClick={() => setEditingNews(null)}
            >
              Hủy
            </button>
          )}
        </form>
      </div>
      )}

      {/* Danh sách tin tức */}
      <div className="table-container">
        {newsList.length > 0 ? (
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Tiêu đề</th>
                <th>Ngày xuất bản</th>
                <th>Trạng thái</th>
                <th>Lượt xem</th>
                <th>Danh mục</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {newsList.map((news) => (
                <tr key={news.id}>
                  <td>{news.id}</td>
                  <td>{news.title}</td>
                  <td>{new Date(news.publishedAt).toLocaleDateString('vi-VN')}</td>
                  <td>{news.status === 'PUBLISHED' ? 'Đã xuất bản' : 'Bản nháp'}</td>
                  <td>{news.viewCount}</td>
                  <td>{categories.find((cat) => cat.id === news.categoryId)?.name || 'Không xác định'}</td>
                  <td>
                    <button
                      className="btn btn-primary btn-small"
                      onClick={() => handleEditNews(news)}
                    >
                      Sửa
                    </button>
                    <button
                      className="btn btn-danger btn-small"
                      onClick={() => handleDeleteNews(news.id)}
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p>Chưa có tin tức nào.</p>
        )}
      </div>
    </div>
  );
};

export default NewsPage;