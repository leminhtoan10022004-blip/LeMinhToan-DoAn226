import React, { useState, useEffect } from 'react';
import api from '../api/axios'; // Import file axios vừa tạo
import { 
  MagnifyingGlassIcon, 
  FunnelIcon, 
  LockClosedIcon, 
  LockOpenIcon,
  NoSymbolIcon
} from '@heroicons/react/24/outline';

const UserTable = ({ users, setUsers }) => {

  const handleToggle = async (userId) => {
    try {
      const response = await axios.post(`http://127.0.0.1:8000/api/users/toggle-status/${userId}`);
      
      if (response.data.success) {
        setUsers(prevUsers => 
          prevUsers.map(user => 
            user.id === userId ? { ...user, TrangThai: response.data.newStatus } : user
          )
        );
      }
    } catch (error) {
      console.error("Lỗi khi đổi trạng thái:", error);
    }
  };

  return (
    <table className="min-w-full bg-white rounded-lg overflow-hidden shadow">
      <thead className="bg-gray-100">
        <tr>
          <th className="px-6 py-3 text-left">Họ Tên</th>
          <th className="px-6 py-3 text-left">Trạng thái</th>
          <th className="px-6 py-3 text-center">Hành động</th>
        </tr>
      </thead>
      <tbody>
        {users.map((user) => (
          <tr key={user.id} className="border-b hover:bg-gray-50 transition">
            <td className="px-6 py-4">{user.Ho} {user.Ten}</td>
            <td className="px-6 py-4">
              <span className={`px-2 py-1 rounded-full text-xs ${
                user.TrangThai === 'active' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
              }`}>
                {user.TrangThai === 'active' ? 'Đang hoạt động' : 'Đã khóa'}
              </span>
            </td>
            <td className="px-6 py-4 text-center">
              <button
                onClick={() => handleToggle(user.id)}
                className={`px-4 py-2 rounded-md text-white font-medium transition ${
                  user.TrangThai === 'active' 
                    ? 'bg-red-500 hover:bg-red-600' 
                    : 'bg-blue-500 hover:bg-blue-600'
                }`}
              >
                {user.TrangThai === 'active' ? 'Khóa tài khoản' : 'Mở khóa'}
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);

  // 1. Hàm lấy danh sách người dùng từ Laravel
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/users?search=${searchTerm}`);
      setUsers(response.data.data);
    } catch (error) {
      console.error("Lỗi lấy dữ liệu:", error);
    } finally {
      setLoading(false);
    }
  };

  // Gọi fetchUsers khi component load hoặc khi gõ tìm kiếm
  useEffect(() => {
    fetchUsers();
  }, [searchTerm]);

  // 2. Hàm xử lý Khóa/Mở khóa (Toggle Lock)
  const handleToggleLock = async (userId, currentStatus) => {
    const newStatus = (currentStatus === 'LOCKED') ? 'ACTIVE' : 'LOCKED';
    
    try {
      const response = await api.post('/users/update-status', {
        id: userId,
        newStatus: newStatus
      });

      if (response.data.success) {
        // Cập nhật state tại chỗ để giao diện đổi màu ngay lập tức
        setUsers(users.map(u => u.id === userId ? { ...u, status: newStatus } : u));
      }
    } catch (error) {
      alert("Không thể cập nhật trạng thái!");
    }
  };

  // 3. Hàm xử lý Vô hiệu hóa (Disable)
  const handleDisable = async (userId) => {
    if (!window.confirm("Bạn có chắc chắn muốn vô hiệu hóa tài khoản này?")) return;

    try {
      const response = await api.post('/users/update-status', {
        id: userId,
        newStatus: 'DISABLED'
      });

      if (response.data.success) {
        setUsers(users.map(u => u.id === userId ? { ...u, status: 'DISABLED' } : u));
      }
    } catch (error) {
      alert("Lỗi khi vô hiệu hóa!");
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mt-8"> 
      {/* Header với ô tìm kiếm */}
      <div className="p-8 border-b border-gray-50 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <h3 className="text-xl font-bold text-gray-800">Quản lý tài khoản</h3>
        
        <div className="flex flex-wrap items-center gap-4">
          <div className="relative">
            <MagnifyingGlassIcon className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
            <input 
              type="text" 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Tìm tên, email..." 
              className="pl-12 pr-4 py-3 bg-gray-50 border-none rounded-2xl text-sm w-72 focus:ring-2 focus:ring-orange-500 transition-all"
            />
          </div>
          <button className="flex items-center gap-2 px-6 py-3 bg-gray-50 text-gray-600 rounded-2xl text-sm font-medium hover:bg-gray-100 transition-all">
            <FunnelIcon className="w-4 h-4" />
            Lọc trạng thái
          </button>
        </div>
      </div>

      {/* Table dữ liệu */}
      <div className="overflow-x-auto">
        {loading ? (
           <div className="p-10 text-center text-gray-400">Đang tải dữ liệu...</div>
        ) : (
          <table className="w-full text-left border-separate border-spacing-y-2 px-8">
            {/* ... THEAD giữ nguyên ... */}
            <tbody className="text-sm">
              {users.map((user) => (
                <tr key={user.id} className="group hover:bg-gray-50/80 transition-all">
                  <td className="px-6 py-6 first:rounded-l-2xl"> 
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 font-bold text-lg">
                        {user.Ten ? user.Ten.charAt(0) : 'U'}
                      </div>
                      <div>
                        <p className="font-bold text-gray-800 text-base">{user.Ho} {user.Ten}</p>
                        <p className="text-sm text-gray-400 font-medium">{user.Email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-6 text-gray-500 font-medium">{user.NgayTao}</td>
                  <td className="px-6 py-6">
                    <span className={`px-4 py-1.5 rounded-xl text-[10px] font-bold uppercase tracking-wider ${
                      user.TrangThai === 'ACTIVE' ? 'bg-green-100 text-green-600' :
                      user.TrangThai === 'LOCKED' ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-600'
                    }`}>
                      {user.TrangThai}
                    </span>
                  </td>
                  <td className="px-6 py-6 text-right last:rounded-r-2xl">
                    <div className="flex justify-end gap-3">
                      {/* Nút Khóa/Mở khóa */}
                      <button 
                        onClick={() => handleToggleLock(user.id, user.TrangThai)}
                        className={`p-2.5 rounded-xl transition-all shadow-sm bg-white border border-gray-100 ${
                          user.TrangThai === 'LOCKED' ? 'text-green-500 hover:bg-green-50' : 'text-gray-400 hover:text-orange-500 hover:bg-orange-50'
                        }`}
                      >
                        {user.TrangThai === 'LOCKED' ? <LockOpenIcon className="w-5 h-5" /> : <LockClosedIcon className="w-5 h-5" />}
                      </button>
                      
                      {/* Nút Vô hiệu hóa */}
                      <button 
                        onClick={() => handleDisable(user.id)}
                        disabled={user.TrangThai === 'DISABLED'}
                        className="p-2.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all shadow-sm bg-white border border-gray-100 disabled:opacity-20"
                      >
                        <NoSymbolIcon className="w-5 h-5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default UserManagement;