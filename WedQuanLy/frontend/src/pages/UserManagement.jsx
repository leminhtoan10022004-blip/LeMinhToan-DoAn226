import React, { useState } from 'react';
import { 
  MagnifyingGlassIcon, 
  FunnelIcon, 
  LockClosedIcon, 
  LockOpenIcon,
  NoSymbolIcon
} from '@heroicons/react/24/outline';

const UserManagement = () => {
  // Dữ liệu mẫu dựa trên bảng NguoiDung trong ERD
  const [users, setUsers] = useState([
    { id: 1, name: "Lê Minh Toàn", email: "toan@example.com", status: "Active", joined: "2026-03-20" },
    { id: 2, name: "Nguyễn Văn A", email: "vana@example.com", status: "Locked", joined: "2026-03-21" },
    { id: 3, name: "Trần Thị B", email: "thib@example.com", status: "Disabled", joined: "2026-03-22" },
  ]);

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden mt-8"> 
    <div className="p-8 border-b border-gray-50 flex flex-col md:flex-row md:items-center justify-between gap-4">
      <h3 className="text-xl font-bold text-gray-800">Quản lý tài khoản</h3>
      
      <div className="flex flex-wrap items-center gap-4"> {/* Tăng gap lên 4 */}
        <div className="relative">
          <MagnifyingGlassIcon className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
          <input 
            type="text" 
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
  
    <div className="overflow-x-auto">
      <table className="w-full text-left border-separate border-spacing-y-2 px-8"> {/* Sử dụng border-separate và px-8 để tạo khoảng cách đầu dòng */}
        <thead>
          <tr className="text-gray-400 text-[11px] uppercase tracking-widest font-bold">
            <th className="px-6 py-4">Người dùng</th>
            <th className="px-6 py-4">Ngày tham gia</th>
            <th className="px-6 py-4">Trạng thái</th>
            <th className="px-6 py-4 text-right">Thao tác</th>
          </tr>
        </thead>
        <tbody className="text-sm">
          {users.map((user) => (
            <tr key={user.id} className="group hover:bg-gray-50/80 transition-all">
              {/* Tăng py-6 để mỗi hàng cao hơn, thoáng hơn */}
              <td className="px-6 py-6 first:rounded-l-2xl"> 
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 font-bold text-lg">
                    {user.name.charAt(0)}
                  </div>
                  <div>
                    <p className="font-bold text-gray-800 text-base">{user.name}</p>
                    <p className="text-sm text-gray-400 font-medium">{user.email}</p>
                  </div>
                </div>
              </td>
              <td className="px-6 py-6 text-gray-500 font-medium">{user.joined}</td>
              <td className="px-6 py-6">
                <span className={`px-4 py-1.5 rounded-xl text-[10px] font-bold uppercase tracking-wider ${
                  user.status === 'Active' ? 'bg-green-100 text-green-600' :
                  user.status === 'Locked' ? 'bg-red-100 text-red-600' : 'bg-gray-100 text-gray-600'
                }`}>
                  {user.status}
                </span>
              </td>
              <td className="px-6 py-6 text-right last:rounded-r-2xl">
                <div className="flex justify-end gap-3">
                  <button className="p-2.5 text-gray-400 hover:text-orange-500 hover:bg-orange-50 rounded-xl transition-all shadow-sm bg-white border border-gray-100">
                    <LockClosedIcon className="w-5 h-5" />
                  </button>
                  <button className="p-2.5 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all shadow-sm bg-white border border-gray-100">
                    <NoSymbolIcon className="w-5 h-5" />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
  );
};

export default UserManagement;