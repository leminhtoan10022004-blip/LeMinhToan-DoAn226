import React from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  BellIcon, 
  Bars3Icon, // Thay thế nút thu nhỏ Sidebar
  ArrowRightOnRectangleIcon // Thêm nút đăng xuất nhanh
} from '@heroicons/react/24/outline';

const Header = () => {
  const navigate = useNavigate();

  // Dữ liệu giả lập, sau này sẽ lấy từ bảng NguoiDung
  const adminName = "Lê Minh Toàn";
  const notificationCount = 2; // Số lượng thông báo chưa xem từ bảng ThongBao

  return (
    <header className="h-[72px] bg-white border-b border-gray-100 flex items-center justify-between px-6 shrink-0 sticky top-0 z-40">
      
      {/* 1. Bên Trái: Nút Menu/Thu nhỏ Sidebar */}
      <div className="flex items-center">
        <button className="p-2 text-gray-400 hover:text-orange-500 hover:bg-orange-50 rounded-xl transition-all" title="Thu nhỏ/Mở rộng Sidebar">
          <Bars3Icon className="w-6 h-6" />
        </button>
      </div>

      {/* 2. Bên Phải: Thông báo, Avatar, Đăng xuất */}
      <div className="flex items-center gap-4">
        
        {/* Nút Thông báo (Bell Icon) */}
        <button className="relative p-2.5 text-gray-400 hover:text-orange-500 hover:bg-orange-50 rounded-xl transition-all" title="Thông báo">
          <BellIcon className="w-6 h-6" />
          {notificationCount > 0 && (
            <span className="absolute top-1.5 right-1.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[9px] font-bold text-white shadow-sm">
              {notificationCount}
            </span>
          )}
        </button>

        {/* Thông tin Admin (Avatar & Tên) */}
        <div className="flex items-center gap-3 pl-3 border-l border-gray-100 h-10">
          <div className="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 font-bold text-lg">
            {adminName.charAt(0)} {/* Lấy chữ cái đầu (ví dụ: L) làm Avatar */}
          </div>
          <p className="text-sm font-semibold text-gray-800 tracking-tight">
            {adminName}
          </p>
        </div>
      </div>
    </header>
  );
};

export default Header;