import React, { useState } from 'react';
import { 
  MagnifyingGlassIcon, 
  PlusIcon,
  PencilSquareIcon,
  TrashIcon,
  EyeIcon
} from '@heroicons/react/24/outline';

const Careers = () => {
  const [careers, setCareers] = useState([
    { id: 1, name: "Công nghệ thông tin", code: "IT01", description: "Lập trình, quản trị hệ thống...", scale: "Holland" },
    { id: 2, name: "Quản trị kinh doanh", code: "BA02", description: "Quản lý doanh nghiệp, startup...", scale: "Holland" },
    { id: 3, name: "Thiết kế đồ họa", code: "GD03", description: "Thiết kế UI/UX, Banner, Video...", scale: "Big Five" },
  ]);

  return (
    <div className="flex-1 p-6 lg:p-10 bg-gray-50 min-h-screen">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 tracking-tight">Quản lý Ngành nghề</h1>
          <p className="text-gray-500 text-sm mt-1">Danh mục các ngành nghề phục vụ định hướng.</p>
        </div>
        <button className="flex items-center gap-2 px-6 py-3 bg-orange-500 text-white rounded-2xl text-sm font-bold hover:bg-orange-600 transition-all shadow-md shadow-orange-200">
          <PlusIcon className="w-5 h-5" />
          Thêm ngành mới
        </button>
      </div>

      <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm mb-8 flex flex-wrap gap-4 items-center">
        <div className="relative flex-1 min-w-[300px]">
          <MagnifyingGlassIcon className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
          <input 
            type="text" 
            placeholder="Tìm tên ngành hoặc mã ngành..." 
            className="w-full pl-12 pr-4 py-3 bg-gray-50 border-none rounded-2xl text-sm focus:ring-2 focus:ring-orange-500 transition-all"
          />
        </div>
      </div>

      {/* Bảng danh sách - Tách không gian (Spaced Table) */}
      <div className="overflow-x-auto">
        <table className="w-full text-left border-separate border-spacing-y-3">
          <thead>
            <tr className="text-gray-400 text-[11px] uppercase tracking-widest font-bold px-8">
              <th className="px-8 py-2">Mã ngành</th>
              <th className="px-4 py-2">Tên ngành nghề</th>
              <th className="px-4 py-2">Thang đo phù hợp</th>
              <th className="px-8 py-2 text-right">Thao tác</th>
            </tr>
          </thead>
          <tbody className="text-sm">
            {careers.map((career) => (
              <tr key={career.id} className="bg-white hover:bg-orange-50/30 transition-all shadow-sm">
                <td className="px-8 py-6 rounded-l-2xl font-bold text-orange-600">
                  {career.code}
                </td>
                <td className="px-4 py-6">
                  <div>
                    <p className="font-bold text-gray-800 text-base">{career.name}</p>
                    <p className="text-gray-400 text-xs mt-1 line-clamp-1 italic">{career.description}</p>
                  </div>
                </td>
                <td className="px-4 py-6">
                  <span className="px-3 py-1 bg-blue-50 text-blue-600 rounded-lg text-xs font-bold uppercase">
                    {career.scale}
                  </span>
                </td>
                <td className="px-8 py-6 text-right rounded-r-2xl">
                  <div className="flex justify-end gap-3">
                    <button className="p-2.5 text-blue-500 hover:bg-blue-50 rounded-xl transition-all" title="Xem chi tiết">
                      <EyeIcon className="w-5 h-5" />
                    </button>
                    <button className="p-2.5 text-green-500 hover:bg-green-50 rounded-xl transition-all" title="Chỉnh sửa">
                      <PencilSquareIcon className="w-5 h-5" />
                    </button>
                    <button className="p-2.5 text-red-500 hover:bg-red-50 rounded-xl transition-all" title="Xóa ngành">
                      <TrashIcon className="w-5 h-5" />
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

export default Careers;