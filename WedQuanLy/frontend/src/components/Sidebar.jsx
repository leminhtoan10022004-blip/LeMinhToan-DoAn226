import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom'; // Thêm 2 hook này
import { 
  HomeIcon, 
  UserIcon, 
  ClipboardDocumentListIcon, 
  BriefcaseIcon, 
  ListBulletIcon,
  ArrowRightOnRectangleIcon,
  UserPlusIcon
} from '@heroicons/react/24/outline';

const menuGroups = [
  {
    title: "Phần chính",
    items: [
      { name: "Thống kê", icon: HomeIcon, path: "/" }, 
      { name: "Tài khoản", icon: UserIcon, path: "/users" },
      { name: "Lịch sử bài test", icon: ClipboardDocumentListIcon, path: "/history" },
      { name: "Ngành nghề", icon: BriefcaseIcon, path: "/careers" },
      { name: "Danh sách bài test", icon: ListBulletIcon, path: "/tests" },
    ]
  },
  {
    title: "Hệ thống",
    items: [
      { name: "Đăng xuất", icon: ArrowRightOnRectangleIcon, path: "/logout" },
    ]
  }
];

const Sidebar = () => {
  const navigate = useNavigate(); 
  const location = useLocation(); 
  return (
    <div className="w-64 h-screen bg-white border-r border-gray-200 flex flex-col py-6 shrink-0">
      <div className="px-6 mb-8">
        <h1 className="text-xl font-bold text-gray-800 tracking-tight">
          Admin <span className="text-orange-500">Panel</span>
        </h1>
      </div>

      <nav className="flex-1 px-4 space-y-8">
        {menuGroups.map((group, idx) => (
          <div key={idx}>
            <p className="px-2 mb-4 text-xs font-semibold text-gray-400 uppercase tracking-wider">
              {group.title}
            </p>
            <div className="space-y-1">
              {group.items.map((item) => {
                const isActive = location.pathname === item.path;

                return (
                  <button
                    key={item.name}
                    onClick={() => navigate(item.path)} // SỰ KIỆN CLICK Ở ĐÂY
                    className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${
                      isActive 
                        ? "bg-orange-50 text-orange-600 font-medium shadow-sm" 
                        : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                    }`}
                  >
                    <item.icon className={`w-5 h-5 ${isActive ? "text-orange-500" : "text-gray-400"}`} />
                    <span className="text-sm">{item.name}</span>
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </nav>
    </div>
  );
};

export default Sidebar;