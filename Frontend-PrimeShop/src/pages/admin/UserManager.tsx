import React, { useEffect, useState } from "react";
import "../../assets/css/admin.css";
import api from "../../api/api";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel
} from "@mui/material";

const UserManager = () => {
  const [users, setUsers] = useState<any[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<any[]>([]);
  const [filters, setFilters] = useState({
    role: "all",
    search: "",
  });
  const [showAddForm, setShowAddForm] = useState(false);
  const [newUser, setNewUser] = useState({
    name: "",
    email: "",
    role: "customer"
  });
  const [editUser, setEditUser] = useState<any>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);


  const fetchUsers = async () => {
    try {
      const response = await api.get("/auth/all-users", {
        params: {
          role: filters.role !== "all" ? filters.role : undefined,
          search: filters.search || undefined,
        }
      });
      setUsers(response.data);
      setFilteredUsers(response.data);
    } catch (error) {
      console.error("Error fetching users:", error);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [filters]);

  const handleFilterChange = (field: string, value: string) => {
    setFilters(prev => ({
      ...prev,
      [field]: value
    }));
  };

  // return (
  //   <div className="admin-container">
  //     <h1 className="admin-title">Quản lý người dùng</h1>

  //     <div className="filters-container" style={{ marginBottom: 20, display: 'flex', gap: 20 }}>
  //       <FormControl style={{ minWidth: 120 }}>
  //         <InputLabel>Vai trò</InputLabel>
  //         <Select
  //           value={filters.role}
  //           onChange={(e) => handleFilterChange("role", e.target.value)}
  //         >
  //           <MenuItem value="all">Tất cả</MenuItem>
  //           <MenuItem value="ADMIN">Admin</MenuItem>
  //           <MenuItem value="USER">User</MenuItem>
  //         </Select>
  //       </FormControl>

  //       <TextField
  //         label="Tìm kiếm"
  //         value={filters.search}
  //         onChange={(e) => handleFilterChange("search", e.target.value)}
  //       />
  //     </div>

  //     <TableContainer component={Paper}>
  //       <Table>
  //         <TableHead>
  //           <TableRow>
  //             <TableCell>ID</TableCell>
  //             <TableCell>Tên đăng nhập</TableCell>
  //             <TableCell>Họ tên</TableCell>
  //             <TableCell>Email</TableCell>
  //             <TableCell>Số điện thoại</TableCell>
  //             <TableCell>Địa chỉ</TableCell>
  //             <TableCell>Vai trò</TableCell>
  //             <TableCell>Ngày tạo</TableCell>
  //           </TableRow>
  //         </TableHead>
  //         <TableBody>
  //           {filteredUsers.map((user) => (
  //             <TableRow key={user.id}>
  //               <TableCell>{user.id}</TableCell>
  //               <TableCell>{user.username}</TableCell>
  //               <TableCell>{user.fullName}</TableCell>
  //               <TableCell>{user.email}</TableCell>
  //               <TableCell>{user.phoneNumber}</TableCell>
  //               <TableCell>{user.address}</TableCell>
  //               <TableCell>{user.role}</TableCell>
  //               <TableCell>{new Date(user.createdAt).toLocaleDateString()}</TableCell>
  //             </TableRow>
  //           ))}
  //         </TableBody>
  //       </Table>
  //     </TableContainer>
  //   </div>
  // );

  return (
    <div>
      <h1>Quản lý người dùng</h1>
      {/* <button
        onClick={() => setShowAddForm(!showAddForm)}
        className={`btn ${showAddForm ? 'btn-secondary' : 'btn-success'}`}
      >
        {showAddForm ? 'Hủy thêm' : 'Thêm người dùng'}
      </button> */}

      {/* Form thêm người dùng */}
      {showAddForm && (
        <div className="admin-form">
          <h2>Thêm người dùng mới</h2>
          <form onSubmit={() => {}}>
            <div className="form-group">
              <label>Tên</label>
              <input
                type="text"
                value={newUser.name}
                onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                value={newUser.email}
                onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Vai trò</label>
              <select
                value={newUser.role}
                onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                required
              >
                <option value="customer">Khách hàng</option>
                <option value="admin">Admin</option>
                <option value="business">Doanh nghiệp</option>
              </select>
            </div>
            <div className="flex justify-start">
              <button type="submit" className="btn btn-primary">
                Thêm
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="table-container">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Họ và tên</th>
              <th>Tên đăng nhập</th>
              <th>Email</th>
              <th>Số điện thoại</th>
              <th>Địa chỉ</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user: any) => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td>{user.fullName}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{user.phoneNumber}</td>
                <td>{user.address}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modal xác nhận xóa */}
      {deleteId && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Xác nhận xóa</h2>
            <p>Bạn có chắc chắn muốn xóa người dùng này?</p>
            <div className="flex justify-end">
              <button onClick={() => {}} className="btn btn-secondary">
                Hủy
              </button>
              <button onClick={() => {}} className="btn btn-danger">
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal chỉnh sửa người dùng */}
      {editUser && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>Chỉnh sửa người dùng</h2>
            <form onSubmit={() => {}}>
              <div className="form-group">
                <label>ID</label>
                <input type="text" value={""} disabled />
              </div>
              <div className="form-group">
                <label>Tên</label>
                <input
                  type="text"
                  value={""}
                  onChange={(e) => {}}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={""}
                  onChange={(e) => {}}
                  required
                />
              </div>
              <div className="form-group">
                <label>Vai trò</label>
                <select
                  value={""}
                  onChange={(e) => {}}
                  required
                >
                  <option value="customer">Khách hàng</option>
                  <option value="admin">Admin</option>
                  <option value="business">Doanh nghiệp</option>
                </select>
              </div>
              <div className="flex justify-end">
                <button type="button" onClick={() => {}} className="btn btn-secondary">
                  Hủy
                </button>
                <button type="submit" className="btn btn-primary">
                  Lưu
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManager;
